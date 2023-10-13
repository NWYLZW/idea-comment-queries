package yij.ie.ideacommentqueries.providers

import kotlin.test.Test
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
            if (cases.size == 0) break;
            val (lineCase, charCase) = cases.removeAt(0)
            assertEquals(lineCase, line)
            assertEquals(charCase, char)
        }
        assertEquals(true, cases.isEmpty())
    }
    @Test
    fun testDefineAbsoluteMatcherRegExp() {
        val code = """
            // &1,1?
            // &[1,1]?
            // &1:1?
            // &./a:1,1?
            // &./a/b:1,1?
            // &./a.b.c-d_e:1,1?
            // &./a/b/c d/e:1,1?
            // &/codes/a/b/c d/e:1,1?
            // &D:/codes/a/b/c d/e:1,1?
            // ignore next line
            // // &1,1?
        """.trimIndent()
        val cases = arrayListOf(
            "" to (+1 to +1),
            "" to (+1 to +1),
            "" to (+1 to +1),
            "./a" to (+1 to +1),
            "./a/b" to (+1 to +1),
            "./a.b.c-d_e" to (+1 to +1),
            "./a/b/c d/e" to (+1 to +1),
            "/codes/a/b/c d/e" to (+1 to +1),
            "D:/codes/a/b/c d/e" to (+1 to +1),
        )
        val (regexp) = defineAbsoluteMatcher("//")
        for (matchResult in regexp.findAll(code)) {
            val destruct = matchResult.destructured
            val (all, file, position) = destruct
            val (line, char) = "(\\d+)[,:]\\s*(\\d+)".toRegex().find(position)!!.destructured.let {
                it.component1().toInt() to it.component2().toInt()
            }
            val (fileCase, pos) = cases.removeAt(0)
            assertEquals("$fileCase${
                if (fileCase.isEmpty()) "" else ":"
            }", file)
            assertEquals(pos.first, line)
            assertEquals(pos.second, char)
        }
        assertEquals(true, cases.isEmpty())
    }
}