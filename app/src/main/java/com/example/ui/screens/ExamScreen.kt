package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CurriculumSeed
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun ExamScreen(
    onSaveAttempt: (String, Int, Int, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedCategory by remember { mutableStateOf("ALL") }
    val questions = remember(selectedCategory) {
        if (selectedCategory == "ALL") CurriculumSeed.examQuestions
        else CurriculumSeed.examQuestions.filter { it.category == selectedCategory }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val chosenAnswers = remember { mutableStateMapOf<String, Int>() }
    var isFinished by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(300) }
    var elapsedSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedCategory, isFinished) {
        if (!isFinished) {
            while (secondsRemaining > 0 && !isFinished) {
                delay(1000L)
                secondsRemaining--
                elapsedSec++
            }
            if (secondsRemaining == 0 && !isFinished) {
                val score = questions.count { chosenAnswers[it.id] == it.correctIndex }
                onSaveAttempt(selectedCategory, score, questions.size, elapsedSec)
                isFinished = true
            }
        }
    }

    val restartExam: (String) -> Unit = { cat ->
        selectedCategory = cat
        currentIndex = 0
        chosenAnswers.clear()
        secondsRemaining = 300
        elapsedSec = 0
        isFinished = false
    }

    val currentQ = questions.getOrNull(currentIndex)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Exam Readiness Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "সব বিষয়", "HTML" to "HTML5", "C" to "C প্রোগ্রামিং").forEach { (cat, label) ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { restartExam(cat) },
                        label = { Text(label) }
                    )
                }
            }
        }

        if (isFinished) {
            val score = questions.count { chosenAnswers[it.id] == it.correctIndex }
            val pct = if (questions.isNotEmpty()) (score * 100) / questions.size else 0
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "পরীক্ষা সম্পন্ন! স্কোর: $score / ${questions.size} ($pct%)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pct >= 70) SuccessGreen else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (pct >= 80) "অসাধারণ প্রস্তুতি! A+ পাওয়ার পথে তুমি অনেক এগিয়ে।"
                            else "ভালো চেষ্টা! ভুল উত্তরগুলোর ব্যাখ্যা দেখে আবার পরীক্ষা দাও।",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { restartExam(selectedCategory) }) {
                            Text("আবার পরীক্ষা দাও")
                        }
                    }
                }
            }
        } else if (currentQ != null) {
            item {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                val chosen = chosenAnswers[currentQ.id]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "প্রশ্ন ${currentIndex + 1}/${questions.size} • ${currentQ.category}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentQ.questionBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (currentQ.codeSnippet != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = currentQ.codeSnippet,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }

                        currentQ.options.forEachIndexed { idx, opt ->
                            val isSelected = chosen == idx
                            val isCorrect = idx == currentQ.correctIndex
                            val borderColor = when {
                                chosen == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                isCorrect -> SuccessGreen
                                isSelected -> ErrorCrimson
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, borderColor),
                                color = when {
                                    chosen != null && isCorrect -> SuccessGreen.copy(alpha = 0.1f)
                                    chosen != null && isSelected -> ErrorCrimson.copy(alpha = 0.1f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = chosen == null) {
                                        chosenAnswers[currentQ.id] = idx
                                    }
                                    .testTag("exam_option_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = opt, style = MaterialTheme.typography.bodyLarge)
                                    if (chosen != null && isCorrect) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = SuccessGreen)
                                    } else if (chosen != null && isSelected) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Incorrect", tint = ErrorCrimson)
                                    }
                                }
                            }
                        }

                        if (chosen != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "ব্যাখ্যা: ${currentQ.explanationBn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { if (currentIndex > 0) currentIndex-- },
                                enabled = currentIndex > 0
                            ) {
                                Text("পূর্ববর্তী")
                            }
                            if (currentIndex < questions.lastIndex) {
                                Button(onClick = { currentIndex++ }) {
                                    Text("পরবর্তী প্রশ্ন")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val score = questions.count { chosenAnswers[it.id] == it.correctIndex }
                                        onSaveAttempt(selectedCategory, score, questions.size, elapsedSec)
                                        isFinished = true
                                    },
                                    modifier = Modifier.testTag("exam_finish_btn")
                                ) {
                                    Text("ফলাফল দেখো")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
