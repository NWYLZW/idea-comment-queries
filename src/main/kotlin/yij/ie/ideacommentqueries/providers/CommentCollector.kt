package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.startOffset

typealias Position = Pair<Int, Int>

typealias MatcherResult = Pair<Pair<Position, Position>, String?>

typealias MatcherFunc = (endPos: Position, MatchResult.Destructured) -> MatcherResult

typealias Matcher = Pair<Regex, MatcherFunc>

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

val defineRelativeMatcherRegExp = fun (prefix: String) =
    "(?<!${prefix}\\s{0,10000})${prefix}\\s*(${relativeRule})\\?".toRegex()

fun resolveRelativeMatchResult(matchResult: MatchResult.Destructured): Pair<Int, Int> {
    val (all, offset, lineOffset, direction, charOffset) = matchResult
    val offsetInt = when (offset) {
        "^" -> -1
        "_" -> +1
        "⌄" -> +1
        "v" -> +1
        "V" -> +1
        else -> 0
    }
    val lineOffsetInt = lineOffset.toIntOrNull() ?: 1
    val charOffsetInt = charOffset.toIntOrNull() ?: 1
    val directionInt = when (direction) {
        ">" -> 1
        "<" -> -1
        else -> 0
    }
    return lineOffsetInt * offsetInt to charOffsetInt * directionInt
}

fun defineRelativeMatcher(prefix: String): Matcher {
    val regExp = defineRelativeMatcherRegExp(prefix)
    return regExp to fun (endPos: Position, matchResult: MatchResult.Destructured): MatcherResult {
        val (line, char) = resolveRelativeMatchResult(matchResult)
        return Pair(
            Pair(
                endPos,
                Pair(line + endPos.first, char + endPos.second)
            ),
            null
        )
    }
}

val matchers = mapOf(
    "twoSlashRelative" to defineRelativeMatcher("//"),
)

@Suppress("UnstableApiUsage")
open class CommentCollector(
    private val matchers: Array<Matcher?>,
    private val file: PsiFile,
    private val editor: Editor,
) : FactoryInlayHintsCollector(editor) {
    private fun insertHint(
        sink: InlayHintsSink,
        type: String,
        offset: Int,
        text: String,
        factory: PresentationFactory
    ) {
        sink.addInlineElement(
            offset,
            false,
            HintsInlayUtil.tag(
                factory,
                text
                    .replace("\n *".toRegex(), "␊")
                    .replace("[\u0000-\u001F\u007F-\u009F]".toRegex(), "")
            ),
            false
        )
    }

    private fun getPsiElementByPosition(line: Int, char: Int): PsiElement? {
        val lineStartOffset = editor.document.getLineStartOffset(line - 1) - 2
        val offset = lineStartOffset + char
        return file.findElementAt(offset)
    }

    private fun getQuickInfoBy(line: Int, char: Int): String? {
        val ele = getPsiElementByPosition(line + 1, char) ?: return null
        val tss = TypeScriptService.getForFile(file.project, file.virtualFile) ?: return null
        val quickInfo = tss.getQuickInfoAt(
            ele,
            ele.originalElement,
            file.originalFile.virtualFile
        )
        return quickInfo?.get()
    }

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        val text = element.text
        for (matcher in matchers) {
            if (matcher == null) continue

            for (match in twoSlashRelative.findAll(text)) {
                val endOffset = element.startOffset + match.range.last + 1
                val endPosition = editor.offsetToLogicalPosition(
                    element.startOffset + match.range.last + 1
                )
                val (positions, file) = matcher.second(
                    endPosition.line to endPosition.column,
                    match.destructured
                )
                val queryPos= positions.second
                getQuickInfoBy(queryPos.first, queryPos.second)?.let {
                    insertHint(
                        sink,
                        "inlay",
                        endOffset,
                        it,
                        factory
                    )
                }
            }
        }
        return false
    }
}