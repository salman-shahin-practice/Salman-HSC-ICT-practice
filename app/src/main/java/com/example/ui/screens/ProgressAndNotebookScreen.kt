package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.IctLabUiState
import com.example.ui.components.AccuracyTrendChart
import com.example.ui.components.CompletionRingCard
import com.example.ui.components.ModuleCompletionBarChart
import com.example.ui.components.ModuleCompletionStat
import com.example.ui.components.PracticeActivityBarChart
import com.example.ui.components.SegmentedMasteryBar
import com.example.ui.components.TopicMasteryBar
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary

@Composable
fun ProgressAndNotebookScreen(
    state: IctLabUiState,
    onLaunchExam: () -> Unit,
    onOpenInEditor: (String, String) -> Unit,
    onToggleMistakeResolved: (String, Boolean) -> Unit,
    onDeleteMistake: (String) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var moduleFilter by remember { mutableStateOf("ALL") } // "ALL", "HTML", "C"
    val tabs = listOf("অগ্রগতি", "মিস্টেক (${state.mistakes.size})", "বুকমার্ক (${state.bookmarks.size})")

    val htmlProblems = state.problems.filter { it.language == "HTML" }
    val cProblems = state.problems.filter { it.language == "C" }
    val htmlSolved = htmlProblems.count { state.progressMap[it.id]?.completed == true }
    val cSolved = cProblems.count { state.progressMap[it.id]?.completed == true }
    val totalProblems = state.problems.size
    val totalSolved = htmlSolved + cSolved

    val overallPct = if (totalProblems > 0) (totalSolved * 100) / totalProblems else 0
    val htmlPct = if (htmlProblems.isNotEmpty()) (htmlSolved * 100) / htmlProblems.size else 0
    val cPct = if (cProblems.isNotEmpty()) (cSolved * 100) / cProblems.size else 0

    val streakDays = state.activityLogs.count { it.runsCount > 0 || it.problemsSolved > 0 }
    val attemptedAccuracies = state.progressMap.values.filter { it.attempts > 0 || it.bestAccuracy > 0 }
        .map { it.bestAccuracy }
    val avgAccuracy = if (attemptedAccuracies.isNotEmpty()) attemptedAccuracies.average().toInt() else 0

    val moduleStats = remember(state.problems, state.progressMap) {
        state.problems
            .groupBy { it.language to it.moduleKey }
            .map { (keyPair, probs) ->
                val (lang, moduleKey) = keyPair
                val first = probs.first()
                val done = probs.count { state.progressMap[it.id]?.completed == true }
                val total = probs.size
                val pct = if (total > 0) (done * 100) / total else 0
                val moduleAccs = probs.mapNotNull { prob ->
                    val prog = state.progressMap[prob.id]
                    if (prog != null && (prog.attempts > 0 || prog.bestAccuracy > 0)) {
                        prog.bestAccuracy
                    } else {
                        null
                    }
                }
                val avgBestAcc = if (moduleAccs.isNotEmpty()) moduleAccs.average().toInt() else 0
                ModuleCompletionStat(
                    moduleKey = "${lang}_$moduleKey",
                    titleBn = first.moduleTitleBn,
                    shortLabel = shortModuleLabel(moduleKey, first.moduleTitleBn),
                    language = lang,
                    solved = done,
                    total = total,
                    completionPercent = pct,
                    avgBestAccuracy = avgBestAcc,
                    sortOrder = (if (lang == "HTML") 0 else 10_000) + (probs.minOfOrNull { it.sortOrder } ?: 0)
                )
            }
            .sortedBy { it.sortOrder }
    }

    val filteredModules = remember(moduleStats, moduleFilter) {
        when (moduleFilter) {
            "HTML" -> moduleStats.filter { it.language == "HTML" }
            "C" -> moduleStats.filter { it.language == "C" }
            else -> moduleStats
        }
    }

    val accuracySeries = remember(state.activityLogs, state.quizAttempts) {
        val logAcc = state.activityLogs.reversed().mapNotNull { log ->
            if (log.accuracySamples > 0) log.accuracySum / log.accuracySamples else null
        }
        val quizAcc = state.quizAttempts.reversed().map { it.accuracyPercent }
        (logAcc + quizAcc).takeLast(8)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("progress_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Overall & Language Completion Rings + KPI Summary
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "সামগ্রিক প্রোগ্রেস ওভারভিউ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = EmeraldSecondary.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "$totalSolved/$totalProblems সমাধান",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CompletionRingCard(
                                        title = "মোট অগ্রগতি",
                                        subtitle = "$totalSolved/$totalProblems টি",
                                        percent = overallPct,
                                        ringColor = EmeraldSecondary,
                                        modifier = Modifier.weight(1f),
                                        ringSize = 84.dp
                                    )
                                    CompletionRingCard(
                                        title = "HTML5",
                                        subtitle = "$htmlSolved/${htmlProblems.size} টি",
                                        percent = htmlPct,
                                        ringColor = TealPrimary,
                                        modifier = Modifier.weight(1f),
                                        ringSize = 84.dp
                                    )
                                    CompletionRingCard(
                                        title = "C প্রোগ্রামিং",
                                        subtitle = "$cSolved/${cProblems.size} টি",
                                        percent = cPct,
                                        ringColor = AmberTertiary,
                                        modifier = Modifier.weight(1f),
                                        ringSize = 84.dp
                                    )
                                }

                                SegmentedMasteryBar(
                                    htmlSolved = htmlSolved,
                                    cSolved = cSolved,
                                    totalProblems = totalProblems
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProgressMetricPill(
                                        icon = Icons.Rounded.LocalFireDepartment,
                                        value = "$streakDays দিন",
                                        label = "অ্যাক্টিভ স্ট্রিক",
                                        tint = AmberTertiary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ProgressMetricPill(
                                        icon = Icons.Rounded.CheckCircle,
                                        value = "$totalSolved টি",
                                        label = "মোট সমাধান",
                                        tint = SuccessGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ProgressMetricPill(
                                        icon = Icons.Rounded.Speed,
                                        value = "$avgAccuracy%",
                                        label = "গড় নির্ভুলতা",
                                        tint = TealPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = onLaunchExam,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("launch_exam_from_progress_btn")
                                ) {
                                    Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("HSC বোর্ড মিনি-টেস্ট (Exam Mode) দাও")
                                }
                            }
                        }
                    }

                    // Module Completion Rate Visual Bar Chart + Filter Chips
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.BarChart,
                                            contentDescription = null,
                                            tint = TealPrimary
                                        )
                                        Text(
                                            text = "মডিউল কমপ্লিশন রেট চার্ট (%)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    val completedModulesCount = filteredModules.count { it.completionPercent >= 100 }
                                    Text(
                                        text = "$completedModulesCount/${filteredModules.size} সম্পূর্ণ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = moduleFilter == "ALL",
                                        onClick = { moduleFilter = "ALL" },
                                        label = { Text("সব মডিউল (${moduleStats.size})") },
                                        modifier = Modifier.testTag("module_filter_all")
                                    )
                                    FilterChip(
                                        selected = moduleFilter == "HTML",
                                        onClick = { moduleFilter = "HTML" },
                                        label = { Text("HTML5 (${moduleStats.count { it.language == "HTML" }})") },
                                        modifier = Modifier.testTag("module_filter_html")
                                    )
                                    FilterChip(
                                        selected = moduleFilter == "C",
                                        onClick = { moduleFilter = "C" },
                                        label = { Text("C প্রোগ্রামিং (${moduleStats.count { it.language == "C" }})") },
                                        modifier = Modifier.testTag("module_filter_c")
                                    )
                                }

                                ModuleCompletionBarChart(modules = filteredModules)
                            }
                        }
                    }

                    // Topic-Wise Mastery Bars (synchronized with filter)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "মডিউলভিত্তিক দক্ষতা (Topic Mastery)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (moduleFilter) {
                                            "HTML" -> "HTML5"
                                            "C" -> "C ল্যাব"
                                            else -> "সবগুলো"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                                filteredModules.forEach { stat ->
                                    TopicMasteryBar(
                                        topicName = stat.titleBn,
                                        solved = stat.solved,
                                        total = stat.total,
                                        barColor = if (stat.language == "HTML") TealPrimary else AmberTertiary,
                                        language = stat.language,
                                        avgBestAccuracy = stat.avgBestAccuracy
                                    )
                                }
                            }
                        }
                    }

                    // 7-Day Practice Activity Chart
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "দৈনিক প্র্যাকটিস অ্যাক্টিভিটি (৭ দিন)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                PracticeActivityBarChart(
                                    logs = state.activityLogs,
                                    barColor = TealPrimary
                                )
                            }
                        }
                    }

                    // Accuracy & Debugging Improvement Trend
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "নির্ভুলতা ও ডিবাগিং উন্নতির গ্রাফ (%)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                AccuracyTrendChart(
                                    accuracies = accuracySeries,
                                    lineColor = EmeraldSecondary
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                if (state.mistakes.isEmpty()) {
                    EmptyNotebookPlaceholder(
                        icon = Icons.Rounded.BugReport,
                        title = "মিস্টেক নোটবুক পরিষ্কার!",
                        subtitle = "HTML বা C কোডে কোনো এরর হলে এখানে সংরক্ষণ করা হবে যেন পরীক্ষার আগে রিভিশন দিতে পারো।"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(state.mistakes, key = { it.id }) { mistake ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 760.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    1.dp,
                                    if (mistake.resolved) SuccessGreen.copy(alpha = 0.35f)
                                    else ErrorCrimson.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Checkbox(
                                                checked = mistake.resolved,
                                                onCheckedChange = { onToggleMistakeResolved(mistake.id, it) }
                                            )
                                            Column {
                                                Text(
                                                    text = "${mistake.language} • ${mistake.errorTitleBn}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (mistake.resolved) SuccessGreen else ErrorCrimson
                                                )
                                                Text(
                                                    text = mistake.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(onClick = { onDeleteMistake(mistake.id) }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete mistake")
                                        }
                                    }
                                    Text(
                                        text = mistake.explanationBn,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "সমাধান: ${mistake.fixTipBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    OutlinedButton(
                                        onClick = { onOpenInEditor(mistake.language, mistake.brokenCode) }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("ল্যাবে ওপেন করে ঠিক করো")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                if (state.bookmarks.isEmpty()) {
                    EmptyNotebookPlaceholder(
                        icon = Icons.Rounded.Bookmark,
                        title = "কোনো বুকমার্ক নেই",
                        subtitle = "HTML বা C ল্যাবে বুকমার্ক আইকনে চাপলে গুরুত্বপূর্ণ কোড এখানে জমা হবে।"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(state.bookmarks, key = { it.id }) { bm ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 760.dp),
                                shape = RoundedCornerShape(16.dp),
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
                                                text = bm.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = bm.language,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { onDeleteBookmark(bm.id) }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete bookmark")
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = bm.code.lines().take(5).joinToString("\n"),
                                            fontFamily = JetBrainsMonoFontFamily,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { onOpenInEditor(bm.language, bm.code) }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("এডিটরে লোড করো")
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

@Composable
private fun ProgressMetricPill(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun shortModuleLabel(moduleKey: String, fallbackTitleBn: String): String {
    return when (moduleKey) {
        "html_basic" -> "বেসিক"
        "html_formatting" -> "ফরম্যাট"
        "html_lists" -> "লিস্ট"
        "html_tables" -> "টেবিল"
        "html_links" -> "লিংক"
        "html_images" -> "ইমেজ"
        "html_forms" -> "ফর্ম"
        "html_layout" -> "লেআউট"
        "c_io" -> "I/O"
        "c_operators" -> "অপারেটর"
        "c_conditions" -> "if-else"
        "c_switch" -> "switch"
        "c_loops" -> "লুপ"
        "c_arrays" -> "অ্যারে"
        "c_functions" -> "ফাংশন"
        "c_board" -> "বোর্ড"
        else -> fallbackTitleBn.replace(Regex("^[০-৯0-9]+\\.\\s*"), "").take(7)
    }
}

@Composable
private fun EmptyNotebookPlaceholder(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
