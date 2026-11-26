package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorThemeMode
import com.example.data.model.LabLanguage
import com.example.engine.CodeHighlighter
import com.example.ui.theme.ClassicLightEditorPalette
import com.example.ui.theme.DarkSlateEditorPalette
import com.example.ui.theme.EditorColorPalette
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SolarizedWarmEditorPalette
import kotlinx.coroutines.delay

fun resolveEditorPalette(mode: EditorThemeMode): EditorColorPalette = when (mode) {
    EditorThemeMode.DARK_SLATE -> DarkSlateEditorPalette
    EditorThemeMode.CLASSIC_LIGHT -> ClassicLightEditorPalette
    EditorThemeMode.SOLARIZED_WARM -> SolarizedWarmEditorPalette
}

private class HighlightCacheEntry(
    var lastText: String = "",
    var lastBrackets: Pair<Int, Int>? = null,
    var lastResult: AnnotatedString = AnnotatedString("")
)

@Composable
fun CodeEditorPanel(
    language: LabLanguage,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onInsertSymbol: (String) -> Unit,
    fontSizeSp: Int,
    themeMode: EditorThemeMode,
    modifier: Modifier = Modifier,
    autoClosePair: Pair<String, String>? = null,
    onApplyAutoClose: (() -> Unit)? = null,
    readOnly: Boolean = false,
    showHeaderBar: Boolean = true,
    showSymbolToolbar: Boolean = true,
    placeholder: String = ""
) {
    val context = LocalContext.current
    val palette = remember(themeMode) { resolveEditorPalette(themeMode) }
    val symbols = remember(language) {
        if (language == LabLanguage.HTML) {
            listOf("<", ">", "/", "=", "\"", ";", "{", "}", ":", "  ")
        } else {
            listOf("#", "<", ">", ";", "{", "}", "(", ")", "\"", "%", "&", "=", "+", "-", "*", "  ")
        }
    }

    // Undo / Redo state per editor instance
    val undoStack = remember(language) { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember(language) { mutableStateListOf<TextFieldValue>() }
    var lastCommittedText by remember(language) { mutableStateOf(value.text) }
    var skipExternalReset by remember(language) { mutableStateOf(false) }

    LaunchedEffect(value.text) {
        if (skipExternalReset) {
            skipExternalReset = false
            lastCommittedText = value.text
        } else if (value.text != lastCommittedText) {
            // External text change (e.g., problem switch / reset / solution load)
            undoStack.clear()
            redoStack.clear()
            lastCommittedText = value.text
        }
    }

    var copiedFeedback by remember { mutableStateOf(false) }
    LaunchedEffect(copiedFeedback) {
        if (copiedFeedback) {
            delay(1400)
            copiedFeedback = false
        }
    }

    val emitEdit: (TextFieldValue) -> Unit = { nextValue ->
        if (nextValue.text != value.text) {
            undoStack.add(value)
            if (undoStack.size > 40) undoStack.removeAt(0)
            redoStack.clear()
            lastCommittedText = nextValue.text
            skipExternalReset = true
        }
        onValueChange(nextValue)
    }

    val (cursorLine, cursorCol) = remember(value.text, value.selection.start) {
        CodeHighlighter.computeCursorPosition(value.text, value.selection.start)
    }
    val matchingBrackets = remember(value.text, value.selection.start) {
        if (value.selection.collapsed) {
            CodeHighlighter.findMatchingBracket(value.text, value.selection.start)
        } else null
    }

    val cacheEntry = remember(language, palette) { HighlightCacheEntry() }
    val visualTransformation = remember(language, palette, matchingBrackets) {
        VisualTransformation { annotatedString ->
            val raw = annotatedString.text
            val highlighted = if (cacheEntry.lastText == raw && cacheEntry.lastBrackets == matchingBrackets) {
                cacheEntry.lastResult
            } else {
                val built = if (language == LabLanguage.HTML) {
                    CodeHighlighter.highlightHtml(raw, palette, matchingBrackets)
                } else {
                    CodeHighlighter.highlightC(raw, palette, matchingBrackets)
                }
                cacheEntry.lastText = raw
                cacheEntry.lastBrackets = matchingBrackets
                cacheEntry.lastResult = built
                built
            }
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }

    val lineCount = remember(value.text) { (value.text.count { it == '\n' } + 1).coerceAtLeast(1) }
    val lineHeightSp = (fontSizeSp + 6).sp
    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()

    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = palette.background,
        border = BorderStroke(1.dp, palette.gutterText.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Compact Editor Header Bar (Language tag, Ln/Col, Undo/Redo, Indent, Copy)
            if (showHeaderBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.gutterBackground)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.tagOrType.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (language == LabLanguage.HTML) Icons.Rounded.Html else Icons.Rounded.Code,
                                    contentDescription = null,
                                    tint = palette.tagOrType,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (language == LabLanguage.HTML) "HTML5" else "C99",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = palette.tagOrType
                                )
                            }
                        }
                        Text(
                            text = "Ln $cursorLine, Col $cursorCol",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = palette.gutterText,
                            modifier = Modifier.testTag("editor_cursor_indicator")
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (!readOnly) {
                            IconButton(
                                onClick = {
                                    if (undoStack.isNotEmpty()) {
                                        val prev = undoStack.removeAt(undoStack.lastIndex)
                                        redoStack.add(value)
                                        lastCommittedText = prev.text
                                        skipExternalReset = true
                                        onValueChange(prev)
                                    }
                                },
                                enabled = undoStack.isNotEmpty(),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("editor_undo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                                    contentDescription = "Undo",
                                    tint = if (undoStack.isNotEmpty()) palette.text else palette.gutterText.copy(alpha = 0.45f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (redoStack.isNotEmpty()) {
                                        val next = redoStack.removeAt(redoStack.lastIndex)
                                        undoStack.add(value)
                                        lastCommittedText = next.text
                                        skipExternalReset = true
                                        onValueChange(next)
                                    }
                                },
                                enabled = redoStack.isNotEmpty(),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("editor_redo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Redo,
                                    contentDescription = "Redo",
                                    tint = if (redoStack.isNotEmpty()) palette.text else palette.gutterText.copy(alpha = 0.45f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val formatted = CodeHighlighter.formatCode(value.text, language)
                                    if (formatted != value.text) {
                                        emitEdit(TextFieldValue(formatted, TextRange(formatted.length)))
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("editor_format_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.FormatIndentIncrease,
                                    contentDescription = "Format code indentation",
                                    tint = palette.keyword,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Code", value.text))
                                copiedFeedback = true
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("editor_copy_btn")
                        ) {
                            Icon(
                                imageVector = if (copiedFeedback) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                contentDescription = "Copy code",
                                tint = if (copiedFeedback) palette.tagOrType else palette.text,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = palette.gutterText.copy(alpha = 0.2f))
            }

            // Optional HTML Auto-Close Helper Strip
            AnimatedVisibility(visible = !readOnly && autoClosePair != null && onApplyAutoClose != null) {
                if (autoClosePair != null && onApplyAutoClose != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "অটো-ক্লোজ ট্যাগ: ${autoClosePair.second} যুক্ত করবে?",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        undoStack.add(value)
                                        if (undoStack.size > 40) undoStack.removeAt(0)
                                        redoStack.clear()
                                        skipExternalReset = true
                                        onApplyAutoClose()
                                    }
                                    .testTag("auto_close_tag_btn")
                            ) {
                                Text(
                                    text = "Add ${autoClosePair.second}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Programming Symbol Toolbar
            if (showSymbolToolbar && !readOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.symbolBarBg)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    symbols.forEach { sym ->
                        val label = if (sym == "  ") "Tab" else sym
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.background,
                            border = BorderStroke(1.dp, palette.gutterText.copy(alpha = 0.22f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    undoStack.add(value)
                                    if (undoStack.size > 40) undoStack.removeAt(0)
                                    redoStack.clear()
                                    skipExternalReset = true
                                    onInsertSymbol(sym)
                                }
                                .testTag("symbol_btn_$label")
                        ) {
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = palette.tagOrType
                                )
                            }
                        }
                    }
                }
            }

            // Line Number Gutter + Highlighted Editable Text Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 210.dp, max = 340.dp)
                    .verticalScroll(scrollVertical)
            ) {
                // Gutter with Active Line Indicator
                val gutterWidth = if (lineCount >= 100) 44.dp else 38.dp
                Column(
                    modifier = Modifier
                        .width(gutterWidth)
                        .fillMaxHeight()
                        .background(palette.gutterBackground)
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (line in 1..lineCount) {
                        val isActiveLine = line == cursorLine && !readOnly
                        Text(
                            text = line.toString(),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = lineHeightSp,
                            fontWeight = if (isActiveLine) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActiveLine) palette.tagOrType else palette.gutterText,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Horizontally Scrollable Editable Code Canvas (prevents soft-wrap line drift)
                BoxWithConstraints(
                    modifier = Modifier.weight(1f)
                ) {
                    val minCanvasWidth = (maxWidth - 20.dp).coerceAtLeast(120.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollHorizontal)
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = lineHeightSp,
                                color = palette.comment
                            )
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = { incoming ->
                                if (!readOnly) {
                                    val processed = CodeHighlighter.applyAutoIndent(value, incoming, language)
                                    emitEdit(processed)
                                }
                            },
                            readOnly = readOnly,
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = lineHeightSp,
                                color = palette.text
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Text
                            ),
                            cursorBrush = SolidColor(palette.tagOrType),
                            visualTransformation = visualTransformation,
                            modifier = Modifier
                                .widthIn(min = minCanvasWidth)
                                .defaultMinSize(minHeight = 190.dp)
                                .testTag(if (language == LabLanguage.HTML) "html_code_editor" else "c_code_editor")
                        )
                    }
                }
            }
        }
    }
}
