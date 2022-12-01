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
        return object : CommentCollector(
            editor, arrayOf(
                matchers["twoSlashRelative"]
            ),
            fun (line: Int, char: Int, filePath: String?): String? {
                val lineStartOffset = editor.document.getLineStartOffset(line) - 2
                val offset = lineStartOffset + char
                val ele = file.findElementAt(offset) ?: return null

                val tss = TypeScriptService.getForFile(file.project, file.virtualFile) ?: return null
                val quickInfo = tss.getQuickInfoAt(
                    ele,
                    ele.originalElement,
                    file.originalFile.virtualFile
                )
                return try {
                    // sleep 0.3 second to wait for tss.getQuickInfoAt
                    TimeUnit.MILLISECONDS.sleep(300)
                    quickInfo?.get(1, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    logger<TS>().warn("getQuickInfoAt failed", e)
                    null
                }
            }
        ) {}
    }
}
