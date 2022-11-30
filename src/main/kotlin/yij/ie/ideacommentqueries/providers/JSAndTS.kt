package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.*
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
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

val defineRelativeMatcherRegExp = fun (prefix: String, rule: String) =
    "(?<!${prefix}\\s{0,10000})${prefix}\\s*(${rule})\\?".toRegex()

val twoSlashRelative = defineRelativeMatcherRegExp("//", relativeRule)

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
        fun insertHint(
            type: String,
            offset: Int,
            text: String,
            factory: HintsInlayPresentationFactory
        ) {
            sink.addInlineElement(
                offset,
                false,
                factory.simpleText(
                    text
                        .replace("\n *".toRegex(), "␊")
                        .replace("[\u0000-\u001F\u007F-\u009F]".toRegex(), "")
                ),
                false
            )
        }
        fun getPsiElementByPosition(line: Int, char: Int): PsiElement? {
            val lineStartOffset = editor.document.getLineStartOffset(line - 1) - 2
//            logger<JSAndTS>().info("getPsiElementByOffset $lineStartOffset")
            val offset = lineStartOffset + char
//            logger<JSAndTS>().info("getPsiElementByOffset $line $char $offset")
            return file.findElementAt(offset)
        }
        fun getQuickInfoBy(line: Int, char: Int): String? {
            val ele = getPsiElementByPosition(line, char) ?: return null
            val tss = TypeScriptService.getForFile(file.project, file.virtualFile) ?: return null
            val quickInfo = tss.getQuickInfoAt(
                ele,
                ele.originalElement,
                file.originalFile.virtualFile
            )
            return quickInfo?.get()
        }
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                val text = element.text
                for (match in twoSlashRelative.findAll(text)) {
                    val (all, offset, lineOffset, direction, charOffset) = match.destructured
                    val matchOffset = element.startOffset + match.range.last - (
                        lineOffset.length - direction.length - charOffset.length - (1 /* ? length */)
                    )
                    val pointPosition = editor.offsetToLogicalPosition(matchOffset)
//                    logger<JSAndTS>().info(
//                        "match: $match, all: $all," +
//                        "offset: $offset, lineOffset: $lineOffset, direction: $direction, charOffset: $charOffset"
//                    )
                    val offsetInt = when (offset) {
                        "^" -> -1
                        "_" -> +1
                        "⌄" -> +1
                        "v" -> +1
                        "V" -> +1
                        else -> 0
                    }
                    val lineOffsetInt = lineOffset.toIntOrNull() ?: 0
                    val charOffsetInt = charOffset.toIntOrNull() ?: 0
                    val directionInt = when (direction) {
                        ">" -> 1
                        "<" -> -1
                        else -> 0
                    }
//                    logger<JSAndTS>().info("line: ${pointPosition.line}, column: ${pointPosition.column}")
                    val targetLine = lineOffsetInt * offsetInt    + pointPosition.line
                    val targetChar = charOffsetInt * directionInt + pointPosition.column
                    // get hover text
                    val hoverText = getQuickInfoBy(targetLine, targetChar)
                    hoverText?.let {
                        insertHint(
                            "inlay",
                            element.startOffset + match.range.last + 1,
                            it,
                            object : HintsInlayPresentationFactory(factory) {}
                        )
                    }
                }
                return false
            }
        }
    }
}
