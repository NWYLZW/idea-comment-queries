package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.startOffset
import javax.swing.JComponent

val twoSlashRelative = defineRelativeMatcherRegExp("//")

@Suppress("UnstableApiUsage")
class JSAndTS: InlayHintsProvider<JSAndTS.Setting> {
    override val key: SettingsKey<Setting>
        get() = SettingsKey("yij.ie.ideacommentqueries.jsandts")
    override val name: String
        get() = "JS and TS"
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
            arrayOf(
                matchers["twoSlashRelative"]
            ),
            file, editor
        ) {}
    }
}
