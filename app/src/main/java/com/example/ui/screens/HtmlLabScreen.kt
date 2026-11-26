package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.LabLanguage
import com.example.data.model.PracticeProblemEntity
import com.example.engine.HtmlCheckResult
import com.example.ui.IctLabUiState
import com.example.ui.components.CodeEditorPanel
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.SuccessGreen

@Composable
fun HtmlLabScreen(
    state: IctLabUiState,
    selectedProblemId: String,
    codeValue: TextFieldValue,
    checkResult: HtmlCheckResult?,
    autoCloseSuggestion: Pair<String, String>?,
    onSelectProblem: (PracticeProblemEntity) -> Unit,
    onCodeChange: (TextFieldValue) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onApplyAutoClose: () -> Unit,
    onCheckSolution: (PracticeProblemEntity) -> Unit,
    onResetStarter: (PracticeProblemEntity) -> Unit,
    onLoadSolution: (PracticeProblemEntity) -> Unit,
    onBookmark: (PracticeProblemEntity, String) -> Unit,
    onAskDoubt: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val htmlProblems = remember(state.problems) { state.problems.filter { it.language == "HTML" } }
    val activeProblem = htmlProblems.firstOrNull { it.id == selectedProblemId } ?: htmlProblems.firstOrNull()
    var viewMode by remember { mutableIntStateOf(0) } // 0: Split, 1: Code, 2: Preview
    var showHint by remember { mutableStateOf(false) }

    if (activeProblem == null) return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Syllabus Module / Problem Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "HTML5 প্র্যাকটিস মডিউল",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    htmlProblems.forEach { prob ->
                        val isSolved = state.progressMap[prob.id]?.completed == true
                        FilterChip(
                            selected = prob.id == activeProblem.id,
                            onClick = { onSelectProblem(prob) },
                            leadingIcon = if (isSolved) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Solved",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            label = { Text(prob.moduleTitleBn) },
                            modifier = Modifier.testTag("html_module_chip_${prob.id}")
                        )
                    }
                }
            }
        }

        // Problem Brief & Common Exam Mistake Alert
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeProblem.titleBn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeProblem.moduleTitleEn} • ${activeProblem.difficulty}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onBookmark(activeProblem, codeValue.text) },
                            modifier = Modifier.testTag("html_bookmark_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = "Bookmark code"
                            )
                        }
                    }

                    Text(
                        text = activeProblem.instructionsBn,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showHint = !showHint }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (showHint) "হিন্ট লুকাও" else "হিন্ট ও সাধারণ ভুল দেখো",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (showHint) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberTertiary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "টিপস: ${activeProblem.hintBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = AmberTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "বোর্ড পরীক্ষায় সাধারণ ভুল: ${activeProblem.commonMistakeBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // View Mode Switcher (Split / Code / Live Preview)
        item {
            val labels = listOf("Split", "Code", "Preview")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                labels.forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = viewMode == idx,
                        onClick = { viewMode = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = labels.size)
                    ) {
                        Text(label)
                    }
                }
            }
        }

        // Responsive Code Editor & Live HTML5 Preview Pane
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 660.dp
                if (isWide && viewMode == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CodeEditorPanel(
                            language = LabLanguage.HTML,
                            value = codeValue,
                            onValueChange = onCodeChange,
                            onInsertSymbol = onInsertSymbol,
                            fontSizeSp = state.settings.editorFontSizeSp,
                            themeMode = state.settings.editorTheme,
                            autoClosePair = autoCloseSuggestion,
                            onApplyAutoClose = onApplyAutoClose,
                            modifier = Modifier.weight(1f)
                        )
                        HtmlLivePreviewCard(
                            htmlCode = codeValue.text,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (viewMode == 0 || viewMode == 1) {
                            CodeEditorPanel(
                                language = LabLanguage.HTML,
                                value = codeValue,
                                onValueChange = onCodeChange,
                                onInsertSymbol = onInsertSymbol,
                                fontSizeSp = state.settings.editorFontSizeSp,
                                themeMode = state.settings.editorTheme,
                                autoClosePair = autoCloseSuggestion,
                                onApplyAutoClose = onApplyAutoClose,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (viewMode == 0 || viewMode == 2) {
                            HtmlLivePreviewCard(
                                htmlCode = codeValue.text,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onCheckSolution(activeProblem) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("html_check_btn")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("যাচাই করো")
                }
                OutlinedButton(
                    onClick = { onResetStarter(activeProblem) },
                    modifier = Modifier.testTag("html_reset_btn")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("রিসেট")
                }
                OutlinedButton(
                    onClick = { onLoadSolution(activeProblem) },
                    modifier = Modifier.testTag("html_sample_btn")
                ) {
                    Icon(Icons.Rounded.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("উত্তর")
                }
            }
        }

        // Verification Result Feedback
        if (checkResult != null) {
            item {
                val borderCol = if (checkResult.passed) SuccessGreen else ErrorCrimson
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = borderCol.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, borderCol.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = checkResult.summaryBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = borderCol,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${checkResult.accuracyPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = borderCol
                            )
                        }
                        checkResult.warningsBn.forEach { warn ->
                            Text(
                                text = "• $warn",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!checkResult.passed) {
                            OutlinedButton(
                                onClick = {
                                    onAskDoubt(
                                        codeValue.text,
                                        checkResult.warningsBn.joinToString("\n")
                                    )
                                }
                            ) {
                                Icon(Icons.Rounded.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("টিউটরের সাহায্য নাও")
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlLivePreviewCard(
    htmlCode: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Live HTML5 Output Preview",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.domStorageEnabled = false
                        settings.defaultTextEncodingName = "utf-8"
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .testTag("html_live_webview")
            )
        }
    }
}
