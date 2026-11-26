package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.model.LabLanguage
import com.example.engine.CodeHighlighter
import com.example.ui.theme.DarkSlateEditorPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeHighlighterTest {

    @Test
    fun highlightHtml_appliesTagAttrStringAndCommentStyles() {
        val code = """
            <!DOCTYPE html>
            <html>
            <!-- <div class="ignored"> -->
            <p class="intro">Hello &nbsp;</p>
            </html>
        """.trimIndent()

        val annotated = CodeHighlighter.highlightHtml(code, DarkSlateEditorPalette)
        assertEquals(code, annotated.text)

        // Comment span must override inner tag color at <!-- <div ...> -->
        val commentStart = code.indexOf("<!--")
        val innerDivIndex = code.indexOf("<div")
        val commentSpan = annotated.spanStyles.lastOrNull {
            it.start <= innerDivIndex && it.end > innerDivIndex
        }
        assertNotNull(commentSpan)
        assertEquals(DarkSlateEditorPalette.comment, commentSpan!!.item.color)
        assertTrue(commentStart >= 0)

        // <p tag should receive tagOrType color
        val pTagIndex = code.indexOf("<p")
        val pTagSpan = annotated.spanStyles.lastOrNull {
            it.start <= pTagIndex && it.end > pTagIndex
        }
        assertEquals(DarkSlateEditorPalette.tagOrType, pTagSpan?.item?.color)
    }

    @Test
    fun highlightC_highlightsKeywordsStringsFormatSpecifiersAndComments() {
        val code = """
            #include <stdio.h>
            int main() {
                int x = 10;
                printf("Val=%d\n", x); // int comment
                return 0;
            }
        """.trimIndent()

        val annotated = CodeHighlighter.highlightC(code, DarkSlateEditorPalette)
        assertEquals(code, annotated.text)

        val formatIdx = code.indexOf("%d")
        val formatSpan = annotated.spanStyles.lastOrNull {
            it.start <= formatIdx && it.end > formatIdx
        }
        assertEquals(DarkSlateEditorPalette.number, formatSpan?.item?.color)

        val commentKeywordIdx = code.indexOf("int comment")
        val commentSpan = annotated.spanStyles.lastOrNull {
            it.start <= commentKeywordIdx && it.end > commentKeywordIdx
        }
        assertEquals(DarkSlateEditorPalette.comment, commentSpan?.item?.color)
    }

    @Test
    fun cursorPosition_and_matchingBracket_workAccurately() {
        val code = "int main() {\n  printf(\"hi\");\n}"
        val pos = CodeHighlighter.computeCursorPosition(code, code.indexOf("printf"))
        assertEquals(2 to 3, pos)

        val openBraceIdx = code.indexOf('{')
        val match = CodeHighlighter.findMatchingBracket(code, openBraceIdx + 1)
        assertEquals(openBraceIdx to code.lastIndexOf('}'), match)
    }

    @Test
    fun applyAutoIndent_expandsBracesCleanlyInC() {
        val oldText = "int main() {}"
        val oldVal = TextFieldValue(oldText, TextRange(oldText.indexOf('{') + 1))
        val typedText = "int main() {\n}"
        val newVal = TextFieldValue(typedText, TextRange(typedText.indexOf('\n') + 1))

        val result = CodeHighlighter.applyAutoIndent(oldVal, newVal, LabLanguage.C)
        assertEquals("int main() {\n  \n}", result.text)
        assertEquals("int main() {\n  ".length, result.selection.start)
    }

    @Test
    fun formatCode_indentsHtmlAndCStructures() {
        val uglyC = "int main() {\nif (1) {\nprintf(\"ok\");\n}\n}"
        val formattedC = CodeHighlighter.formatCode(uglyC, LabLanguage.C)
        assertTrue(formattedC.contains("  if (1) {"))
        assertTrue(formattedC.contains("    printf(\"ok\");"))
    }
}
