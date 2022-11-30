package yij.ie.ideacommentqueries.test.providers

import org.junit.Test
import yij.ie.ideacommentqueries.providers.defineRelativeMatcherRegExp
import yij.ie.ideacommentqueries.providers.resolveRelativeMatchResult
import kotlin.test.assertEquals

internal class CommentCollectorKtTest {
    @Test
    fun testDefineRelativeMatcherRegExp() {
        val code = """
            type T0 = string | number;
            //   _?
            type T1 = T0;//<5?
              // ^?
            //   ^2?
            // ignore next line
            // // ^?
            // ignore next line
            // ^?wqedfwqeqw
        """.trimIndent()
        val cases = arrayListOf(
            +1 to +0,
            +0 to -5,
            -1 to +0,
            -2 to +0,
        )
        val regexp = defineRelativeMatcherRegExp("//")
        for (matchResult in regexp.findAll(code)) {
            val destruct = matchResult.destructured
            val (line, char) = resolveRelativeMatchResult(destruct)
            val (lineCase, charCase) = cases.removeAt(0)
            assertEquals(lineCase, line)
            assertEquals(charCase, char)
        }
        assertEquals(true, cases.isEmpty())
    }
}