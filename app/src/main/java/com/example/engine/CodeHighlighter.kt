package com.example.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.EditorColorPalette

object CodeHighlighter {

    private val cKeywords = setOf(
        "int", "float", "double", "char", "void", "long", "short",
        "if", "else", "switch", "case", "default", "break", "continue",
        "for", "while", "do", "return", "const", "struct", "sizeof"
    )

    private val cBuiltins = setOf(
        "printf", "scanf", "main", "sqrt", "pow", "abs", "fabs", "strlen", "gets", "puts"
    )

    fun highlightHtml(code: String, palette: EditorColorPalette): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = palette.text), 0, code.length)

            // Comments <!-- ... -->
            Regex("<!--([\\s\\S]*?)-->").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Tags </?tagName ...>
            Regex("</?[a-zA-Z0-9]+").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.tagOrType, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Closing brackets > or />
            Regex("/?>").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.tagOrType, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Attribute names
            Regex("\\s([a-zA-Z-]+)=").findAll(code).forEach { match ->
                val group = match.groups[1] ?: return@forEach
                addStyle(
                    SpanStyle(color = palette.keyword),
                    group.range.first,
                    group.range.last + 1
                )
            }

            // Strings "..." or '...'
            Regex("\"[^\"]*\"|'[^']*'").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.stringOrAttr),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }

    fun highlightC(code: String, palette: EditorColorPalette): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = palette.text), 0, code.length)

            // Preprocessor directives #include <...> or #define ...
            Regex("#[a-zA-Z_]+[^\\n]*").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.tagOrType, fontWeight = FontWeight.SemiBold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Numbers
            Regex("\\b\\d+(\\.\\d+)?\\b").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.number),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Identifiers -> keywords / builtins
            Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b").findAll(code).forEach { match ->
                val word = match.value
                when {
                    word in cKeywords -> addStyle(
                        SpanStyle(color = palette.keyword, fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1
                    )
                    word in cBuiltins -> addStyle(
                        SpanStyle(color = palette.tagOrType, fontWeight = FontWeight.Medium),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

            // Strings "..." and chars '.'
            Regex("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.stringOrAttr),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Single-line and block comments (applied last so they override inner tokens)
            Regex("//[^\\n]*|/\\*[\\s\\S]*?\\*/").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }
}
