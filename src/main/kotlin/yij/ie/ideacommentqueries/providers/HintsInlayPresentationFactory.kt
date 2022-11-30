package yij.ie.ideacommentqueries.providers

import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.PresentationFactory

@Suppress("UnstableApiUsage")
open class HintsInlayPresentationFactory(private val factory: PresentationFactory) {
    private val commonPadding = InlayPresentationFactory.Padding(5, 5, 5, 2)
    fun simpleText(text: String) = factory.run {
        container(
            text(text),
            padding = commonPadding
        )
    }

}