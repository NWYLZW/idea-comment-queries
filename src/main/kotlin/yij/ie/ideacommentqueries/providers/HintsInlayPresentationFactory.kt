package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors

@Suppress("UnstableApiUsage")
open class HintsInlayUtil {
    companion object {
        private val commonPadding = InlayPresentationFactory.Padding(5, 5, 4, 4)
        private val commonRoundedCorners = InlayPresentationFactory.RoundedCorners(8, 8)
        fun tag(factory: PresentationFactory, text: String) = factory.run {
            container(
                smallText(text),
                padding = commonPadding,
                background = DefaultLanguageHighlighterColors.INLAY_DEFAULT.defaultAttributes.backgroundColor,
                roundedCorners = commonRoundedCorners
            )
        }
    }
}