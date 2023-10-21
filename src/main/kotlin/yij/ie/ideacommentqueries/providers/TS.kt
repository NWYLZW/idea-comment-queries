package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.*
import com.intellij.lang.Language
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import yij.ie.ideacommentqueries.ConfigService
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.JPanel

data class Settings(
    var relative: Boolean = true,
    var absolute: Boolean = true
)

@Suppress("UnstableApiUsage")
class TS: InlayHintsProvider<Settings> {
    override val key: SettingsKey<Settings>
        get() = SettingsKey("yij.ie.ideacommentqueries.providers.TS")
    override val name: String
        get() = "TypeScript comment queries"
    override val previewText: String
        get() = """
            No Content
        """.trimIndent()
    override fun isLanguageSupported(language: Language): Boolean {
        return language.id == "TypeScript" || language.id == "TypeScript JSX"
    }
    override val isVisibleInSettings: Boolean
        get() = true

    override fun createSettings(): Settings = Settings()
    override fun createConfigurable(settings: Settings): ImmediateConfigurable = object : ImmediateConfigurable {
        override val mainCheckboxText: String
            get() = "TypeScript comment queries"
        override val cases: List<ImmediateConfigurable.Case>
            get() = listOf(
                ImmediateConfigurable.Case("Relative", "relative", settings::relative, """
                    Query TypeScript type information relative to the file.
                """),
                ImmediateConfigurable.Case("Absolute", "absolute", settings::absolute, """
                    Query TypeScript type information relative to the project.
                """),
            )

        override fun createComponent(listener: ChangeListener): JComponent = JPanel()
    }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: Settings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        val service = ConfigService.getInstance(editor.project!!)
        if (service.disable) return object : InlayHintsCollector {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                return false
            }
        }

        if (!settings.relative) return object : InlayHintsCollector {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                return false
            }
        }
        fun getPsiFile(filePath: String): PsiFile {
//            val vFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")
//            return PsiDocumentManager.getInstance(file.project).getPsiFile(vFile)
            return file
        }
        val innerMatchers = ArrayList<Matcher>()
        if (settings.relative) {
            innerMatchers.add(matchers["twoSlashRelative"]!!)
        }
        if (settings.absolute) {
            innerMatchers.add(matchers["twoSlashAbsolute"]!!)
        }
        return object : CommentCollector(
            editor, innerMatchers.toTypedArray(),
            fun (line: Int, char: Int, filePath: String?): String? {
                val nFile = filePath?.let { getPsiFile(it) } ?: file
                val lineStartOffset = editor.document.getLineStartOffset(line) - 2
                val offset = lineStartOffset + char
                val ele = nFile.findElementAt(offset) ?: return null

                val tss = TypeScriptService.getForFile(nFile.project, nFile.virtualFile) ?: return null
                val quickInfo = tss.getQuickInfoAt(
                    ele,
                    ele.originalElement,
                    nFile.originalFile.virtualFile
                )
                return try {
                    quickInfo?.get(100, TimeUnit.MILLISECONDS)?.displayString
                } catch (e: Exception) {
                    logger<TS>().warn("getQuickInfoAt failed", e)
                    null
                }
            }
        ) {}
    }
}
