package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.IctLabUiState
import com.example.ui.components.AccuracyTrendChart
import com.example.ui.components.CompletionRingCard
import com.example.ui.components.PracticeActivityBarChart
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
    val tabs = listOf("অগ্রগতি", "মিস্টেক (${state.mistakes.size})", "বুকমার্ক (${state.bookmarks.size})")

    val htmlProblems = state.problems.filter { it.language == "HTML" }
    val cProblems = state.problems.filter { it.language == "C" }
    val htmlSolved = htmlProblems.count { state.progressMap[it.id]?.completed == true }
    val cSolved = cProblems.count { state.progressMap[it.id]?.completed == true }
    val htmlPct = if (htmlProblems.isNotEmpty()) (htmlSolved * 100) / htmlProblems.size else 0
    val cPct = if (cProblems.isNotEmpty()) (cSolved * 100) / cProblems.size else 0

    val streakDays = state.activityLogs.count { it.runsCount > 0 || it.problemsSolved > 0 }
    val totalSolved = htmlSolved + cSolved

    val topicGroups = remember(state.problems, state.progressMap) {
        state.problems.groupBy { it.moduleTitleBn }.map { (module, probs) ->
            val done = probs.count { state.progressMap[it.id]?.completed == true }
            Triple(module, done, probs.size)
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
                    text = { Text(label, maxLines = 1) },
                    modifier = Modifier.testTag("progress_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Completion Rings + Streak Banner
                    item {
                        Card(
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
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CompletionRingCard(
                                        title = "HTML5",
                                        subtitle = "$htmlSolved/${htmlProblems.size} সমস্যা",
                                        percent = htmlPct,
                                        ringColor = TealPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CompletionRingCard(
                                        title = "C Programming",
                                        subtitle = "$cSolved/${cProblems.size} সমস্যা",
                                        percent = cPct,
                                        ringColor = AmberTertiary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AmberTertiary.copy(alpha = 0.14f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = AmberTertiary
                                            )
                                            Column {
                                                Text("$streakDays দিন", fontWeight = FontWeight.Bold)
                                                Text("অ্যাক্টিভ স্ট্রিক", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SuccessGreen.copy(alpha = 0.14f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen
                                            )
                                            Column {
                                                Text("$totalSolved টি", fontWeight = FontWeight.Bold)
                                                Text("মোট সমাধান", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
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

                    // Topic-Wise Mastery Bars
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "বিষয়ভিত্তিক দক্ষতা (Topic Mastery)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                topicGroups.forEachIndexed { idx, (topic, done, total) ->
                                    TopicMasteryBar(
                                        topicName = topic,
                                        solved = done,
                                        total = total,
                                        barColor = if (idx % 2 == 0) TealPrimary else EmeraldSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 7-Day Practice Activity Chart
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "দৈনিক প্র্যাকটিস অ্যাক্টিভিটি",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.mistakes, key = { it.id }) { mistake ->
                            Card(
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
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.bookmarks, key = { it.id }) { bm ->
                            Card(
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
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
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
private fun EmptyNotebookPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
