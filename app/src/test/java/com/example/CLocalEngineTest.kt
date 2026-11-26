package com.example

import com.example.engine.CLocalEngine
import com.example.engine.HtmlAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CLocalEngineTest {

    @Test
    fun `executes for loop series sum and records dry run steps`() {
        val code = """
            #include <stdio.h>
            int main() {
                int n, i, sum = 0;
                scanf("%d", &n);
                for (i = 1; i <= n; i++) {
                    sum = sum + i;
                }
                printf("Sum = %d\n", sum);
                return 0;
            }
        """.trimIndent()

        val result = CLocalEngine.execute(code, stdin = "5", expectedOutput = "Sum = 15")
        assertTrue(result.success)
        assertEquals("Sum = 15", result.output)
        assertEquals(true, result.passedTestCase)
        assertTrue(result.dryRunSteps.isNotEmpty())
    }

    @Test
    fun `executes float triangle area with scanf`() {
        val code = """
            #include <stdio.h>
            int main() {
                float base, height, area;
                scanf("%f %f", &base, &height);
                area = 0.5 * base * height;
                printf("Area = %.2f\n", area);
                return 0;
            }
        """.trimIndent()

        val result = CLocalEngine.execute(code, stdin = "10 5", expectedOutput = "Area = 25.00")
        assertTrue(result.success)
        assertEquals("Area = 25.00", result.output)
        assertEquals(true, result.passedTestCase)
    }

    @Test
    fun `executes array max and recursion`() {
        val code = """
            #include <stdio.h>
            int fact(int n) {
                if (n <= 1) return 1;
                return n * fact(n - 1);
            }
            int main() {
                int arr[4] = {10, 42, 7, 19};
                int max = arr[0];
                for (int i = 1; i < 4; i++) {
                    if (arr[i] > max) max = arr[i];
                }
                printf("%d %d", max, fact(5));
                return 0;
            }
        """.trimIndent()

        val result = CLocalEngine.execute(code)
        assertTrue(result.success)
        assertEquals("42 120", result.output)
    }

    @Test
    fun `translates missing semicolon into beginner friendly Bengali error`() {
        val code = """
            #include <stdio.h>
            int main() {
                printf("Hello")
                return 0;
            }
        """.trimIndent()

        val result = CLocalEngine.execute(code)
        assertFalse(result.success)
        assertNotNull(result.error)
        assertTrue(result.error!!.titleBn.contains("সেমিকোলন"))
    }

    @Test
    fun `html analyzer validates table tags and flags common mistakes`() {
        val goodHtml = "<table border=\"1\"><tr><th colspan=\"2\">Result</th></tr><tr><td>A</td><td>B</td></tr></table>"
        val goodRes = HtmlAnalyzer.analyze(goodHtml, "table,tr,th,td,colspan")
        assertTrue(goodRes.passed)

        val badHtml = "<img src=\"pic.jpg\"></img>"
        val badRes = HtmlAnalyzer.analyze(badHtml, "img,src,alt")
        assertFalse(badRes.passed)
        assertTrue(badRes.warningsBn.any { it.contains("Empty") })
    }
}
