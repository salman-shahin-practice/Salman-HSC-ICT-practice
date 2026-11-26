package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

fun resolveEditorPalette(mode: EditorThemeMode): EditorColorPalette = when (mode) {
    EditorThemeMode.DARK_SLATE -> DarkSlateEditorPalette
    EditorThemeMode.CLASSIC_LIGHT -> ClassicLightEditorPalette
    EditorThemeMode.SOLARIZED_WARM -> SolarizedWarmEditorPalette
}

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
    onApplyAutoClose: (() -> Unit)? = null
) {
    val palette = remember(themeMode) { resolveEditorPalette(themeMode) }
    val symbols = remember(language) {
        if (language == LabLanguage.HTML) {
            listOf("<", ">", "/", "=", "\"", ";", "{", "}", ":", "  ")
        } else {
            listOf("#", "<", ">", ";", "{", "}", "(", ")", "\"", "%", "&", "=", "+", "-", "*", "  ")
        }
    }

    val visualTransformation = remember(language, palette) {
        VisualTransformation { annotatedString ->
            val highlighted = if (language == LabLanguage.HTML) {
                CodeHighlighter.highlightHtml(annotatedString.text, palette)
            } else {
                CodeHighlighter.highlightC(annotatedString.text, palette)
            }
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }

    val lineCount = remember(value.text) { value.text.count { it == '\n' } + 1 }
    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.background)
    ) {
        // Optional HTML Auto-Close Helper Strip
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onApplyAutoClose)
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

        // Quick Programming Symbol Toolbar
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
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onInsertSymbol(sym) }
                        .testTag("symbol_btn_$label")
                ) {
                    Text(
                        text = label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = palette.tagOrType,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Line Number Gutter + Highlighted Text Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 210.dp, max = 330.dp)
                .verticalScroll(scrollVertical)
        ) {
            // Gutter
            Column(
                modifier = Modifier
                    .width(38.dp)
                    .fillMaxHeight()
                    .background(palette.gutterBackground)
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (line in 1..lineCount) {
                    Text(
                        text = line.toString(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp + 6).sp,
                        color = palette.gutterText,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Editor Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollHorizontal)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp + 6).sp,
                        color = palette.text
                    ),
                    cursorBrush = SolidColor(palette.tagOrType),
                    visualTransformation = visualTransformation,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(if (language == LabLanguage.HTML) "html_code_editor" else "c_code_editor")
                )
            }
        }
    }
}
