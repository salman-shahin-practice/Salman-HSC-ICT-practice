package com.example.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.model.LabLanguage
import com.example.ui.theme.EditorColorPalette

object CodeHighlighter {

    private val cKeywords = setOf(
        "int", "float", "double", "char", "void", "long", "short", "unsigned", "signed",
        "if", "else", "switch", "case", "default", "break", "continue",
        "for", "while", "do", "return", "const", "struct", "sizeof", "typedef", "enum"
    )

    private val cBuiltins = setOf(
        "printf", "scanf", "main", "sqrt", "pow", "abs", "fabs",
        "strlen", "strcpy", "strcmp", "strcat", "gets", "puts", "getchar", "putchar"
    )

    private val htmlVoidTags = setOf(
        "br", "hr", "img", "input", "meta", "link", "source", "wbr"
    )

    fun highlightHtml(
        code: String,
        palette: EditorColorPalette,
        matchingBrackets: Pair<Int, Int>? = null
    ): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            if (code.isEmpty()) return@buildAnnotatedString
            addStyle(SpanStyle(color = palette.text), 0, code.length)

            // DOCTYPE declaration <!DOCTYPE html>
            Regex("<!DOCTYPE[^>]*>", RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.keyword, fontWeight = FontWeight.SemiBold),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Internal <style>...</style> CSS properties & values
            Regex("<style[^>]*>([\\s\\S]*?)</style>", RegexOption.IGNORE_CASE).findAll(code).forEach { styleBlock ->
                val innerGroup = styleBlock.groups[1] ?: return@forEach
                val base = innerGroup.range.first
                val css = innerGroup.value
                Regex("([a-zA-Z-]+)\\s*:").findAll(css).forEach { prop ->
                    val g = prop.groups[1] ?: return@forEach
                    addStyle(
                        SpanStyle(color = palette.keyword),
                        base + g.range.first,
                        base + g.range.last + 1
                    )
                }
                Regex(":\\s*([^;\\n}]+)").findAll(css).forEach { valMatch ->
                    val g = valMatch.groups[1] ?: return@forEach
                    addStyle(
                        SpanStyle(color = palette.stringOrAttr),
                        base + g.range.first,
                        base + g.range.last + 1
                    )
                }
            }

            // Tags </?tagName
            Regex("</?[a-zA-Z][a-zA-Z0-9-]*").findAll(code).forEach { match ->
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
            Regex("\\s([a-zA-Z_:.-]+)(?=\\s*=)").findAll(code).forEach { match ->
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

            // HTML Entities (&nbsp;, &lt;, &copy;, &#123;)
            Regex("&[a-zA-Z0-9#]+;").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.number, fontWeight = FontWeight.Medium),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Comments <!-- ... --> applied LAST so inner tags/attributes remain muted
            Regex("<!--([\\s\\S]*?)-->").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }

            applyBracketHighlight(matchingBrackets, code.length, palette)
        }
    }

    fun highlightC(
        code: String,
        palette: EditorColorPalette,
        matchingBrackets: Pair<Int, Int>? = null
    ): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            if (code.isEmpty()) return@buildAnnotatedString
            addStyle(SpanStyle(color = palette.text), 0, code.length)

            // Numbers (integers, floats, hex)
            Regex("\\b0[xX][0-9a-fA-F]+\\b|\\b\\d+(\\.\\d+)?[fFlLuU]*\\b").findAll(code).forEach { match ->
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

            // Preprocessor directives #include <stdio.h>, #define MAX 100
            Regex("^[ \\t]*#([a-zA-Z_]+)(?:[ \\t]+(<[^\\n>]+>|\"[^\\n\"]+\"))?", RegexOption.MULTILINE)
                .findAll(code)
                .forEach { match ->
                    val directiveEnd = match.groups[1]?.range?.last?.plus(1) ?: match.range.last + 1
                    val hashStart = code.indexOf('#', match.range.first).coerceAtLeast(match.range.first)
                    addStyle(
                        SpanStyle(color = palette.tagOrType, fontWeight = FontWeight.SemiBold),
                        hashStart,
                        directiveEnd
                    )
                    match.groups[2]?.let { headerGroup ->
                        addStyle(
                            SpanStyle(color = palette.stringOrAttr),
                            headerGroup.range.first,
                            headerGroup.range.last + 1
                        )
                    }
                }

            // Strings "..." and chars '.' + printf/scanf format specifiers & escapes inside strings
            Regex("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'").findAll(code).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                addStyle(
                    SpanStyle(color = palette.stringOrAttr),
                    start,
                    end
                )
                if (match.value.startsWith("\"")) {
                    val inner = match.value
                    Regex("%[-+0 #]*\\d*(\\.\\d+)?(ll|l|h)?[diufFeEgGxXoscpaAn]|\\\\[ntr0\\\\\"']").findAll(inner)
                        .forEach { spec ->
                            addStyle(
                                SpanStyle(color = palette.number, fontWeight = FontWeight.Bold),
                                start + spec.range.first,
                                start + spec.range.last + 1
                            )
                        }
                }
            }

            // Single-line and block comments applied LAST so inner tokens stay muted
            Regex("//[^\\n]*|/\\*[\\s\\S]*?\\*/").findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = palette.comment, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }

            applyBracketHighlight(matchingBrackets, code.length, palette)
        }
    }

    private fun AnnotatedString.Builder.applyBracketHighlight(
        matchingBrackets: Pair<Int, Int>?,
        length: Int,
        palette: EditorColorPalette
    ) {
        if (matchingBrackets == null) return
        val (first, second) = matchingBrackets
        val style = SpanStyle(
            background = palette.tagOrType.copy(alpha = 0.25f),
            color = palette.tagOrType,
            fontWeight = FontWeight.Bold
        )
        if (first in 0 until length) addStyle(style, first, first + 1)
        if (second in 0 until length) addStyle(style, second, second + 1)
    }

    fun computeCursorPosition(text: String, offset: Int): Pair<Int, Int> {
        val safeOffset = offset.coerceIn(0, text.length)
        var line = 1
        var lastNewline = -1
        for (i in 0 until safeOffset) {
            if (text[i] == '\n') {
                line++
                lastNewline = i
            }
        }
        val column = safeOffset - lastNewline
        return line to column
    }

    fun findMatchingBracket(text: String, cursorOffset: Int): Pair<Int, Int>? {
        if (text.isEmpty()) return null
        val candidates = listOf(cursorOffset - 1, cursorOffset).filter { it in text.indices }
        val pairs = mapOf('(' to ')', '{' to '}', '[' to ']', '<' to '>')
        val reversePairs = pairs.entries.associate { (k, v) -> v to k }

        for (idx in candidates) {
            val ch = text[idx]
            val close = pairs[ch]
            if (close != null) {
                var depth = 0
                for (i in idx until text.length) {
                    if (text[i] == ch) depth++
                    else if (text[i] == close) {
                        depth--
                        if (depth == 0) return idx to i
                    }
                }
            }
            val open = reversePairs[ch]
            if (open != null) {
                var depth = 0
                for (i in idx downTo 0) {
                    if (text[i] == ch) depth++
                    else if (text[i] == open) {
                        depth--
                        if (depth == 0) return i to idx
                    }
                }
            }
        }
        return null
    }

    fun applyAutoIndent(
        oldValue: TextFieldValue,
        newValue: TextFieldValue,
        language: LabLanguage
    ): TextFieldValue {
        // Only apply smart newline indentation when user inserts a single '\n'
        val insertedLen = newValue.text.length - oldValue.text.length
        val cursor = newValue.selection.start
        if (insertedLen != 1 || !newValue.selection.collapsed || cursor <= 0) return newValue
        if (newValue.text.getOrNull(cursor - 1) != '\n') return newValue

        val beforeNewline = newValue.text.substring(0, cursor - 1)
        val afterNewline = newValue.text.substring(cursor)
        val prevLineStart = beforeNewline.lastIndexOf('\n') + 1
        val prevLine = beforeNewline.substring(prevLineStart)
        val baseIndent = prevLine.takeWhile { it == ' ' || it == '\t' }
        val trimmedPrev = prevLine.trimEnd()

        val shouldIncreaseIndent = when (language) {
            LabLanguage.C -> trimmedPrev.endsWith("{")
            LabLanguage.HTML -> {
                val openTagMatch = Regex("<([a-zA-Z][a-zA-Z0-9-]*)(\\s[^>]*)?>$").find(trimmedPrev)
                val tagName = openTagMatch?.groupValues?.getOrNull(1)?.lowercase()
                tagName != null && tagName !in htmlVoidTags && !trimmedPrev.endsWith("/>")
            }
        }

        val nextChar = afterNewline.firstOrNull()
        if (language == LabLanguage.C && trimmedPrev.endsWith("{") && nextChar == '}') {
            val innerIndent = "$baseIndent  "
            val updated = "$beforeNewline\n$innerIndent\n$baseIndent$afterNewline"
            val newCursor = beforeNewline.length + 1 + innerIndent.length
            return TextFieldValue(updated, TextRange(newCursor))
        }

        val targetIndent = if (shouldIncreaseIndent) "$baseIndent  " else baseIndent
        if (targetIndent.isEmpty()) return newValue
        val updated = "$beforeNewline\n$targetIndent$afterNewline"
        val newCursor = cursor + targetIndent.length
        return TextFieldValue(updated, TextRange(newCursor))
    }

    fun formatCode(code: String, language: LabLanguage): String {
        val lines = code.replace("\r\n", "\n").lines()
        val formatted = mutableListOf<String>()
        var indentLevel = 0

        if (language == LabLanguage.C) {
            for (rawLine in lines) {
                val trimmed = rawLine.trim()
                if (trimmed.isEmpty()) {
                    formatted.add("")
                    continue
                }
                if (trimmed.startsWith("}")) {
                    indentLevel = (indentLevel - 1).coerceAtLeast(0)
                }
                formatted.add("  ".repeat(indentLevel) + trimmed)
                val openCount = trimmed.count { it == '{' }
                val closeCount = trimmed.count { it == '}' }
                val net = openCount - closeCount + if (trimmed.startsWith("}")) 1 else 0
                indentLevel = (indentLevel + net).coerceAtLeast(0)
            }
        } else {
            val closingTagRegex = Regex("^</([a-zA-Z][a-zA-Z0-9-]*)\\s*>")
            val openingTagRegex = Regex("^<([a-zA-Z][a-zA-Z0-9-]*)(\\s[^>]*)?>")
            for (rawLine in lines) {
                val trimmed = rawLine.trim()
                if (trimmed.isEmpty()) {
                    formatted.add("")
                    continue
                }
                if (closingTagRegex.containsMatchIn(trimmed)) {
                    indentLevel = (indentLevel - 1).coerceAtLeast(0)
                }
                formatted.add("  ".repeat(indentLevel) + trimmed)
                val openMatch = openingTagRegex.find(trimmed)
                val tag = openMatch?.groupValues?.getOrNull(1)?.lowercase()
                val closesOnSameLine = tag != null && trimmed.contains("</$tag>", ignoreCase = true)
                if (tag != null &&
                    tag !in htmlVoidTags &&
                    !trimmed.endsWith("/>") &&
                    !closesOnSameLine &&
                    !trimmed.startsWith("<!")
                ) {
                    indentLevel++
                }
            }
        }
        return formatted.joinToString("\n")
    }
}
