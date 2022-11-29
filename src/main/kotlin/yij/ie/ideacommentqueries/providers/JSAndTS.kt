package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import javax.swing.JComponent

/**
 * relative rule
 * like:
 * * ^
 * * _
 * * ^3
 * * ^3<
 * * ^3<2
 */
const val relativeRule = "(\\^|_)?(\\d*)(<|>)?(\\d*)?"

val defineRelativeMatcherRegExp = fun (prefix: String, rule: String) = Regex(
    "(?<!${prefix}\\s{0,10000})${prefix}\\s*(${rule})\\?$"
)

val twoSlashRelative = defineRelativeMatcherRegExp("//", relativeRule)

@Suppress("UnstableApiUsage")
class JSAndTS: InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings>
        get() = SettingsKey("yij.ie.ideacommentqueries.jsandts")
    override val name: String
        get() = "JS and TS"
    override val previewText: String
        get() = """
            No Content
        """.trimIndent()

    override fun createSettings(): NoSettings {
        return NoSettings()
    }
    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
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
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                val text = element.text
                logger<JSAndTS>().info(text)
                if (element.startOffset != element.endOffset) {
                    return true
                }
                val matchResult = twoSlashRelative.find(text)
                if (matchResult != null) {
                    val (offset, lineOffset, direction, charOffset) = twoSlashRelative.find(text)?.destructured ?: return true
                    val offsetInt = when (offset) {
                        "^" -> 1
                        "_" -> -1
                        "v" -> -1
                        "V" -> -1
                        else -> 0
                    }
                    val lineOffsetInt = lineOffset.toIntOrNull() ?: 0
                    val charOffsetInt = charOffset.toIntOrNull() ?: 0
                    val directionInt = when (direction) {
                        ">" -> 1
                        "<" -> -1
                        else -> 0
                    }
                    insertHint(
                        "line",
                        sink,
                        element.endOffset,
                        "targetLine: ${lineOffsetInt + offsetInt} char: ${charOffsetInt + directionInt}"
                    )
                }
                if (element.startOffset != editor.document.getLineStartOffset(editor.document.getLineNumber(element.startOffset))) {
                    val (offset, lineOffset, direction, charOffset) = twoSlashRelative.find(text)?.destructured ?: return true
                    logger<JSAndTS>().info("offset: $offset, lineOffset: $lineOffset, direction: $direction, charOffset: $charOffset")
                }
                return false
            }
            fun insertHint(type: String, sink: InlayHintsSink, offset: Int, text: String) {
                sink.addInlineElement(offset, true, factory.run {
                    container(
                        text(text),
                        padding = InlayPresentationFactory.Padding(2, 2, 2, 2),
                    )
                }, false)
            }
        }
    }
}