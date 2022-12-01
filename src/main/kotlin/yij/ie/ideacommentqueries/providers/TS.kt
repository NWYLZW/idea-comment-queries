package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.*
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.util.concurrent.TimeUnit
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class TS: InlayHintsProvider<TS.Setting> {
    override val key: SettingsKey<Setting>
        get() = SettingsKey("yij.ie.ideacommentqueries.ts")
    override val name: String
        get() = "TS"
    override val previewText: String
        get() = """
            No Content
        """.trimIndent()

    data class Setting(internal val enable: Boolean)

    override fun createSettings(): Setting {
        return Setting(true)
    }
    override fun createConfigurable(settings: Setting): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent {
                return object : JComponent() {
                }
            }
        }
    }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: Setting,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        if (!settings.enable) return object : InlayHintsCollector {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                return false
            }
        }
        fun getPsiFile(filePath: String): PsiFile {
//            val vFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")
//            return PsiDocumentManager.getInstance(file.project).getPsiFile(vFile)
            return file
        }
        return object : CommentCollector(
            editor, arrayOf(
                matchers["twoSlashRelative"],
                matchers["twoSlashAbsolute"],
            ),
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
                    quickInfo?.get(1, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    logger<TS>().warn("getQuickInfoAt failed", e)
                    null
                }
            }
        ) {}
    }
}
