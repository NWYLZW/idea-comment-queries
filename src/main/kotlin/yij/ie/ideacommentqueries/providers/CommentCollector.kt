package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
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
const val relativeRule = "(\\^|_|v|V|⌄)?(\\d*)(<|>)?(\\d*)?"

fun defineRelativeMatcherRegExp(prefix: String) =
    "(?<!${prefix}\\s{0,10000})${prefix}\\s*(${relativeRule})\\?\\n".toRegex()

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

/**
 * file rule
 * like:
 * * [x] ./a
 * * [x] ./a/b
 * * [x] ./a/b/c d/e
 * * [x] ./a.b.c-d_e
 * * [x] /codes/a/b/c d/e
 * * [x] D:/codes/a/b/c d/e
 */
const val fileRule = "(?:[a-z|A-Z]\\:)?\\.?/(?:[\\w|_\\-\\. ]+/)*[\\w|_\\-\\. ]+(?:\\.\\w+)*"
/**
 * position rule
 * like:
 * * [x] 1,2
 * * [x] [1,2]
 * * [x] [1, 2]
 * * [x] 1:2
 */
const val positionRule = "(\\d+[,|:]\\d+|\\[\\d+[,|:]\\s*\\d+\\])"
/**
 * absolute rule
 * like:
 * * [x] positionRule
 * * [x] fileRule:potionRule
 */
const val absoluteRule = "(${fileRule}:)?${positionRule}"

fun defineAbsoluteMatcherRegExp(prefix: String, rule: String) =
    "(?<!${prefix}\\s{0,10000})${prefix}\\s*&(${rule})\\?\n".toRegex()

fun resolveAbsoluteMatchResult(matchResult: MatchResult.Destructured): Pair<String, Position> {
    val (all, file, position) = matchResult
    val (line, char) = position.split(Regex("[,|:]")).map { it.toInt() }
    return file to Pair(line, char)
}

fun defineAbsoluteMatcher(prefix: String): Matcher {
    val regExp = defineAbsoluteMatcherRegExp(prefix, absoluteRule)
    return regExp to fun (endPos: Position, matchResult: MatchResult.Destructured): MatcherResult {
        val (file, position) = resolveAbsoluteMatchResult(matchResult)
        return endPos to position to file
    }
}

val matchers = mapOf(
    "twoSlashRelative" to defineRelativeMatcher("//"),
    "twoSlashAbsolute" to defineAbsoluteMatcher("//"),
)

typealias WhatHints = (line: Int, char: Int, file: String?) -> String?

@Suppress("UnstableApiUsage")
open class CommentCollector(
    private val editor: Editor,
    private val matchers: Array<Matcher?>,
    private val whatHints: WhatHints = fun (line: Int, char: Int, file: String?): String {
        return "line: $line, char: $char, file: $file"
    }
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

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        val text = element.text
        for (matcher in matchers) {
            if (matcher == null) continue
            val (regExp, matchFunc) = matcher

            for (match in regExp.findAll(text)) {
                val endOffset = element.startOffset + match.range.last
                val endPosition = editor.offsetToLogicalPosition(endOffset)
                val (positions, file) = matchFunc(
                    endPosition.line to endPosition.column,
                    match.destructured
                )
                val queryPos= positions.second
                whatHints(queryPos.first, queryPos.second, file)?.let {
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