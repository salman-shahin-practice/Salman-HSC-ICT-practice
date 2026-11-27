package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.data.model.LabLanguage
import com.example.data.model.PracticeProblemEntity
import com.example.engine.CExecutionResult
import com.example.ui.IctLabUiState
import com.example.ui.components.CodeEditorPanel
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun CLabScreen(
    state: IctLabUiState,
    selectedProblemId: String,
    codeValue: TextFieldValue,
    stdinValue: String,
    execResult: CExecutionResult?,
    isRunning: Boolean,
    preferCloudCompiler: Boolean,
    onSelectProblem: (PracticeProblemEntity) -> Unit,
    onCodeChange: (TextFieldValue) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onStdinChange: (String) -> Unit,
    onToggleCloudCompiler: (Boolean) -> Unit,
    onRunProgram: (PracticeProblemEntity) -> Unit,
    onResetStarter: (PracticeProblemEntity) -> Unit,
    onLoadSolution: (PracticeProblemEntity) -> Unit,
    onBookmark: (PracticeProblemEntity, String) -> Unit,
    onAskDoubt: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onClearOutput: () -> Unit = {},
    onOpenAlgoFlow: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val cProblems = remember(state.problems) { state.problems.filter { it.language == "C" } }
    val activeProblem = cProblems.firstOrNull { it.id == selectedProblemId } ?: cProblems.firstOrNull()
    var showHint by remember { mutableStateOf(false) }
    var showDryRun by remember { mutableStateOf(false) }

    if (activeProblem == null) return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Syllabus Module Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "C প্রোগ্রামিং প্র্যাকটিস মডিউল",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (onOpenAlgoFlow != null) {
                        OutlinedButton(
                            onClick = onOpenAlgoFlow,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("c_lab_open_algo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text("অ্যালগরিদম ও ম্যাথ", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cProblems.forEach { prob ->
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
                            modifier = Modifier.testTag("c_module_chip_${prob.id}")
                        )
                    }
                }
            }
        }

        // Problem Description Card
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
                            modifier = Modifier.testTag("c_bookmark_btn")
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Bookmark C code")
                        }
                    }

                    Text(
                        text = activeProblem.instructionsBn,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (activeProblem.expectedOutput.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "প্রত্যাশিত আউটপুট: ${activeProblem.expectedOutput}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

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
                                        text = "সাধারণ ভুল: ${activeProblem.commonMistakeBn}",
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

        // Execution Mode Toggle (Offline Engine vs Online Cloud GCC)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (preferCloudCompiler && state.isOnline) {
                                Icons.Rounded.Cloud
                            } else {
                                Icons.Rounded.Memory
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = if (preferCloudCompiler && state.isOnline) {
                                    "Cloud GCC Compiler (Online)"
                                } else {
                                    "Offline C Engine + Dry-Run"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.isOnline) {
                                    "অনলাইনে ক্লাউড GCC অথবা অফলাইনে তাৎক্ষণিক রান করুন"
                                } else {
                                    "অফলাইন মোডে সম্পূর্ণ C প্র্যাকটিস চালু আছে"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = preferCloudCompiler,
                        onCheckedChange = onToggleCloudCompiler,
                        enabled = state.isOnline
                    )
                }
            }
        }

        // C Code Editor
        item {
            CodeEditorPanel(
                language = LabLanguage.C,
                value = codeValue,
                onValueChange = onCodeChange,
                onInsertSymbol = onInsertSymbol,
                fontSizeSp = state.settings.editorFontSizeSp,
                themeMode = state.settings.editorTheme,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Stdin Input Box for scanf (supports multi-line or space-separated inputs)
        item {
            OutlinedTextField(
                value = stdinValue,
                onValueChange = onStdinChange,
                label = { Text("ইনপুট / Stdin (scanf-এর জন্য, স্পেস বা নতুন লাইনে লিখুন)") },
                placeholder = { Text("যেমন: 10 5") },
                minLines = 1,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("c_stdin_input")
            )
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onRunProgram(activeProblem) },
                    enabled = !isRunning,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("c_run_btn")
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (isRunning) "কম্পাইল ও রান চলছে..." else "কম্পাইল ও রান")
                }
                OutlinedButton(
                    onClick = { onResetStarter(activeProblem) },
                    modifier = Modifier.testTag("c_reset_btn")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("রিসেট")
                }
                OutlinedButton(
                    onClick = { onLoadSolution(activeProblem) },
                    modifier = Modifier.testTag("c_sample_btn")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("উত্তর")
                }
            }
        }

        // Execution Output Console, Error Helper & Dry-Run Table
        if (execResult != null) {
            item {
                var copiedOutput by remember { mutableStateOf(false) }
                LaunchedEffect(copiedOutput) {
                    if (copiedOutput) {
                        delay(1400)
                        copiedOutput = false
                    }
                }
                val consoleText = execResult.output.ifBlank {
                    if (execResult.success) {
                        "(কোনো আউটপুট প্রিন্ট হয়নি — printf() ব্যবহার করে দেখো)"
                    } else {
                        execResult.error?.titleBn ?: "Compilation / execution failed"
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Output Console
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(
                            1.dp,
                            if (execResult.success) SuccessGreen.copy(alpha = 0.55f) else ErrorCrimson.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("c_output_console")
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Terminal,
                                        contentDescription = null,
                                        tint = Color(0xFF2DD4BF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (execResult.success) "Exit 0 • ${execResult.executionMode}" else "Error • ${execResult.executionMode}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (execResult.passedTestCase != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (execResult.passedTestCase) {
                                                SuccessGreen.copy(alpha = 0.22f)
                                            } else {
                                                AmberTertiary.copy(alpha = 0.22f)
                                            }
                                        ) {
                                            Text(
                                                text = if (execResult.passedTestCase) "Test Passed ✓" else "Check Output",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (execResult.passedTestCase) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            clipboard?.setPrimaryClip(ClipData.newPlainText("C Output", consoleText))
                                            copiedOutput = true
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("c_copy_output_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (copiedOutput) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy console output",
                                            tint = if (copiedOutput) Color(0xFF2DD4BF) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onClearOutput,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("c_clear_output_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ClearAll,
                                            contentDescription = "Clear console output",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF1E293B))

                            SelectionContainer {
                                Text(
                                    text = consoleText,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (execResult.success) Color(0xFFF8FAFC) else Color(0xFFFCA5A5)
                                )
                            }

                            if (execResult.success && execResult.passedTestCase == false && activeProblem.expectedOutput.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "প্রত্যাশিত আউটপুট (Expected):",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFBBF24)
                                        )
                                        Text(
                                            text = activeProblem.expectedOutput,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bengali/English Friendly Error Explanation Helper
                    if (execResult.error != null) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorCrimson.copy(alpha = 0.09f)
                            ),
                            border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ErrorOutline,
                                        contentDescription = null,
                                        tint = ErrorCrimson
                                    )
                                    Text(
                                        text = "ভুল বিশ্লেষণ: ${execResult.error.titleBn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorCrimson
                                    )
                                }
                                Text(
                                    text = execResult.error.explanationBn,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "সমাধানের উপায়: ${execResult.error.fixTipBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedButton(
                                    onClick = {
                                        onAskDoubt(
                                            codeValue.text,
                                            "${execResult.error.titleBn}: ${execResult.error.explanationBn}"
                                        )
                                    }
                                ) {
                                    Icon(Icons.Rounded.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("এই কোডটি সাপোর্টে পাঠাও")
                                }
                            }
                        }
                    }

                    // Compiler Warnings
                    if (execResult.warningsBn.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AmberTertiary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                execResult.warningsBn.forEach { warn ->
                                    Text(
                                        text = "• $warn",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Step-by-Step Dry-Run Trace Table
                    if (execResult.dryRunSteps.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showDryRun = !showDryRun },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dry_run_toggle_btn")
                        ) {
                            Icon(Icons.Rounded.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (showDryRun) "Dry-Run ট্রেস বন্ধ করো"
                                else "ধাপে ধাপে Dry-Run ট্রেস দেখো (${execResult.dryRunSteps.size} ধাপ)"
                            )
                        }

                        if (showDryRun) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ভেরিয়েবল মান পরিবর্তনের ধাপ (Dry-Run):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    execResult.dryRunSteps.take(15).forEach { step ->
                                        val varsText = step.variablesSnapshot.entries
                                            .joinToString(", ") { "${it.key}=${it.value}" }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "#${step.stepNumber} (${step.description})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = varsText,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
