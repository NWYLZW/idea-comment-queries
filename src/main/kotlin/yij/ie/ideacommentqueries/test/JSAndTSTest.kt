package yij.ie.ideacommentqueries.test

import org.junit.Test
import yij.ie.ideacommentqueries.providers.twoSlashRelative
import kotlin.test.DefaultAsserter.assertEquals

internal class JSAndTSTest {
    @Test
    fun testTwoSlashRelativeRegex() {
        val code = """
            type T0 = string | number;
            //   _?
            type T1 = T0;//<5?
              // ^?
            //   ^2?
            // ignore next line
            // // ^?
        """.trimIndent()
        val results = twoSlashRelative.findAll(code)
        assertEquals("results size must equal 4", 4, results.count())
    }
}