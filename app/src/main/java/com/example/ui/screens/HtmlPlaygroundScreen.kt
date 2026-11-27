package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.data.model.EditorThemeMode
import com.example.data.model.LabLanguage
import com.example.ui.components.CodeEditorPanel

data class HtmlSnippetTemplate(
    val id: String,
    val label: String,
    val html: String
)

val htmlPlaygroundTemplates = listOf(
    HtmlSnippetTemplate(
        id = "basic",
        label = "বেসিক পেজ",
        html = """<!DOCTYPE html>
<html>
<head>
  <title>My HTML Page</title>
  <style>
    body { font-family: sans-serif; padding: 16px; color: #0f172a; }
    h1 { color: #0f625b; margin-bottom: 8px; }
    .card { background: #f1f5f9; padding: 12px; border-radius: 10px; }
  </style>
</head>
<body>
  <h1>স্বাগতম HSC ICT!</h1>
  <div class="card">
    <p>এখানে HTML কোড লিখুন এবং সাথে সাথে লাইভ আউটপুট দেখুন।</p>
  </div>
</body>
</html>"""
    ),
    HtmlSnippetTemplate(
        id = "table",
        label = "টেবিল",
        html = """<!DOCTYPE html>
<html>
<head>
  <title>Student Table</title>
  <style>
    body { font-family: sans-serif; padding: 14px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border: 1px solid #cbd5e1; padding: 8px; text-align: left; }
    th { background: #0f625b; color: white; }
  </style>
</head>
<body>
  <h3>ফলাফল তালিকা</h3>
  <table>
    <tr><th>রোল</th><th>নাম</th><th>জিপিএ</th></tr>
    <tr><td>১০১</td><td>রাফি</td><td>5.00</td></tr>
    <tr><td>১০২</td><td>তাসনিম</td><td>4.92</td></tr>
  </table>
</body>
</html>"""
    ),
    HtmlSnippetTemplate(
        id = "list",
        label = "লিস্ট",
        html = """<!DOCTYPE html>
<html>
<head>
  <title>HTML Lists</title>
  <style>
    body { font-family: sans-serif; padding: 16px; }
    h2 { color: #0f625b; }
  </style>
</head>
<body>
  <h2>ICT অধ্যায়সমূহ</h2>
  <ol>
    <li>তথ্য ও যোগাযোগ প্রযুক্তি</li>
    <li>সংখ্যা পদ্ধতি ও ডিজিটাল ডিভাইস</li>
    <li>ওয়েব ডিজাইন ও HTML</li>
    <li>প্রোগ্রামিং ভাষা (C)</li>
  </ol>
</body>
</html>"""
    ),
    HtmlSnippetTemplate(
        id = "form",
        label = "ফর্ম",
        html = """<!DOCTYPE html>
<html>
<head>
  <title>Registration Form</title>
  <style>
    body { font-family: sans-serif; padding: 16px; }
    input, button { display: block; width: 100%; margin: 8px 0; padding: 8px; box-sizing: border-box; }
    button { background: #0f625b; color: white; border: none; border-radius: 6px; }
  </style>
</head>
<body>
  <h3>শিক্ষার্থী নিবন্ধন</h3>
  <form>
    <label>নাম:</label>
    <input type="text" placeholder="আপনার নাম লিখুন">
    <label>ইমেইল:</label>
    <input type="email" placeholder="name@example.com">
    <button type="button">জমা দিন</button>
  </form>
</body>
</html>"""
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlPlaygroundScreen(
    codeValue: TextFieldValue,
    fontSizeSp: Int,
    themeMode: EditorThemeMode,
    autoCloseSuggestion: Pair<String, String>?,
    onCodeChange: (TextFieldValue) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onApplyAutoClose: () -> Unit,
    onLoadTemplate: (String) -> Unit,
    onClearCode: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableIntStateOf(0) } // 0: Split, 1: Editor, 2: Preview
    var selectedTemplateId by remember { mutableStateOf(htmlPlaygroundTemplates.first().id) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HTML লাইভ এডিটর",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "কোড লিখুন ও সাথে সাথে আউটপুট দেখুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("playground_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedTemplateId = htmlPlaygroundTemplates.first().id
                            onLoadTemplate(htmlPlaygroundTemplates.first().html)
                        },
                        modifier = Modifier.testTag("playground_reset_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reset starter HTML"
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedTemplateId = ""
                            onClearCode()
                        },
                        modifier = Modifier.testTag("playground_clear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Clear editor"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quick Template Chips + Layout Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                htmlPlaygroundTemplates.forEach { template ->
                    FilterChip(
                        selected = selectedTemplateId == template.id,
                        onClick = {
                            selectedTemplateId = template.id
                            onLoadTemplate(template.html)
                        },
                        label = { Text(template.label) },
                        modifier = Modifier.testTag("playground_tpl_${template.id}")
                    )
                }
            }

            val modes = listOf(
                Pair("Split", null),
                Pair("Editor", Icons.Rounded.Code),
                Pair("Preview", Icons.Rounded.Visibility)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                modes.forEachIndexed { index, (label, icon) ->
                    SegmentedButton(
                        selected = viewMode == index,
                        onClick = { viewMode = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        icon = {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        modifier = Modifier.testTag("playground_mode_$index")
                    ) {
                        Text(label)
                    }
                }
            }

            // Responsive Editor + Live Output Preview Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val isWide = maxWidth >= 600.dp

                when (viewMode) {
                    1 -> {
                        CodeEditorPanel(
                            language = LabLanguage.HTML,
                            value = codeValue,
                            onValueChange = onCodeChange,
                            onInsertSymbol = onInsertSymbol,
                            fontSizeSp = fontSizeSp,
                            themeMode = themeMode,
                            autoClosePair = autoCloseSuggestion,
                            onApplyAutoClose = onApplyAutoClose,
                            placeholder = "<h1>Hello World!</h1>",
                            fillAvailableHeight = true,
                            editorTestTag = "playground_html_editor",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    2 -> {
                        HtmlLivePreviewCard(
                            htmlCode = codeValue.text,
                            fillPreviewHeight = true,
                            previewTestTag = "playground_html_preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CodeEditorPanel(
                                    language = LabLanguage.HTML,
                                    value = codeValue,
                                    onValueChange = onCodeChange,
                                    onInsertSymbol = onInsertSymbol,
                                    fontSizeSp = fontSizeSp,
                                    themeMode = themeMode,
                                    autoClosePair = autoCloseSuggestion,
                                    onApplyAutoClose = onApplyAutoClose,
                                    placeholder = "<h1>Hello World!</h1>",
                                    fillAvailableHeight = true,
                                    editorTestTag = "playground_html_editor",
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                                HtmlLivePreviewCard(
                                    htmlCode = codeValue.text,
                                    fillPreviewHeight = true,
                                    previewTestTag = "playground_html_preview",
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CodeEditorPanel(
                                    language = LabLanguage.HTML,
                                    value = codeValue,
                                    onValueChange = onCodeChange,
                                    onInsertSymbol = onInsertSymbol,
                                    fontSizeSp = fontSizeSp,
                                    themeMode = themeMode,
                                    autoClosePair = autoCloseSuggestion,
                                    onApplyAutoClose = onApplyAutoClose,
                                    placeholder = "<h1>Hello World!</h1>",
                                    fillAvailableHeight = true,
                                    editorTestTag = "playground_html_editor",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                HtmlLivePreviewCard(
                                    htmlCode = codeValue.text,
                                    fillPreviewHeight = true,
                                    previewTestTag = "playground_html_preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
