package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    modifier: Modifier = Modifier,
    onOpenPlayground: (() -> Unit)? = null
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HTML5 প্র্যাকটিস মডিউল",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (onOpenPlayground != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenPlayground)
                                .testTag("open_html_playground_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ফ্রি এডিটর",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
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
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "টিপস: ${activeProblem.hintBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        color = MaterialTheme.colorScheme.onSurface
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
                    Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, borderCol.copy(alpha = 0.65f)),
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

private fun extractHtmlTitle(html: String): String {
    val match = Regex("<title[^>]*>([\\s\\S]*?)</title>", RegexOption.IGNORE_CASE).find(html)
    val title = match?.groupValues?.getOrNull(1)?.replace(Regex("\\s+"), " ")?.trim()
    return if (!title.isNullOrBlank()) title else "index.html • Web Preview"
}

@Composable
fun HtmlLivePreviewCard(
    htmlCode: String,
    modifier: Modifier = Modifier,
    fillPreviewHeight: Boolean = false,
    previewTestTag: String = "html_live_webview"
) {
    var reloadNonce by remember { mutableIntStateOf(0) }
    var desktopMode by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }
    var clickedLinkUrl by remember { mutableStateOf<String?>(null) }
    val pageTitle = remember(htmlCode) { extractHtmlTitle(htmlCode) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(modifier = if (fillPreviewHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth()) {
            // Browser Chrome Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = pageTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { desktopMode = !desktopMode },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("html_viewport_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (desktopMode) Icons.Rounded.DesktopWindows else Icons.Rounded.PhoneAndroid,
                                contentDescription = if (desktopMode) "Desktop viewport" else "Mobile viewport",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                clickedLinkUrl = null
                                reloadNonce++
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("html_preview_refresh_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh preview",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { showFullscreen = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("html_preview_fullscreen_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fullscreen,
                                contentDescription = "Open fullscreen preview",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (clickedLinkUrl != null) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "লিঙ্ক টেস্ট: $clickedLinkUrl",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { clickedLinkUrl = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            val previewAreaModifier = if (fillPreviewHeight) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            }

            Box(modifier = previewAreaModifier) {
                if (htmlCode.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "HTML কোড লিখুন — সাথে সাথে এখানে আউটপুট দেখা যাবে",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                    }
                } else {
                    SandboxedHtmlWebView(
                        htmlCode = htmlCode,
                        reloadNonce = reloadNonce,
                        desktopMode = desktopMode,
                        onLinkClick = { clickedLinkUrl = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(previewTestTag)
                    )
                }
            }
        }
    }

    if (showFullscreen) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.88f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pageTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { desktopMode = !desktopMode }) {
                                Icon(
                                    imageVector = if (desktopMode) Icons.Rounded.DesktopWindows else Icons.Rounded.PhoneAndroid,
                                    contentDescription = "Toggle viewport",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { reloadNonce++ }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Reload")
                            }
                            IconButton(
                                onClick = { showFullscreen = false },
                                modifier = Modifier.testTag("html_fullscreen_close_btn")
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close preview")
                            }
                        }
                    }
                    HorizontalDivider()
                    SandboxedHtmlWebView(
                        htmlCode = htmlCode,
                        reloadNonce = reloadNonce,
                        desktopMode = desktopMode,
                        onLinkClick = { clickedLinkUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SandboxedHtmlWebView(
    htmlCode: String,
    reloadNonce: Int,
    desktopMode: Boolean,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lastRenderedHash by remember { mutableStateOf<Int?>(null) }
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.WHITE)
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.domStorageEnabled = false
                settings.defaultTextEncodingName = "utf-8"
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString()
                        if (!url.isNullOrBlank()) {
                            onLinkClick(url)
                        }
                        return true
                    }
                }
            }
        },
        update = { webView ->
            webView.settings.useWideViewPort = desktopMode
            webView.settings.loadWithOverviewMode = desktopMode
            val currentHash = 31 * htmlCode.hashCode() + 17 * reloadNonce + if (desktopMode) 1 else 0
            if (currentHash != lastRenderedHash) {
                lastRenderedHash = currentHash
                webView.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}
