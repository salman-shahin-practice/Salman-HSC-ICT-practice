package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActivityLogEntity

@Composable
fun CompletionRingCard(
    title: String,
    subtitle: String,
    percent: Int,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (percent.coerceIn(0, 100) / 100f) * 360f,
        animationSpec = tween(700),
        label = "ring_sweep"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(98.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.minDimension * 0.12f
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TopicMasteryBar(
    topicName: String,
    solved: Int,
    total: Int,
    barColor: Color
) {
    val ratio = if (total > 0) (solved.toFloat() / total).coerceIn(0f, 1f) else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = topicName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$solved / $total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun PracticeActivityBarChart(
    logs: List<ActivityLogEntity>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val displayLogs = logs.take(7).reversed()
    val maxVal = (displayLogs.maxOfOrNull { maxOf(it.problemsSolved, it.runsCount) } ?: 1).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        if (displayLogs.isEmpty()) {
            Text(
                text = "কোড রান করলে এখানে তোমার ৭ দিনের প্র্যাকটিস গ্রাফ দেখা যাবে!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val count = displayLogs.size.coerceAtLeast(1)
                val slotWidth = size.width / count
                val barWidth = (slotWidth * 0.48f).coerceAtMost(36f)
                val chartHeight = size.height - 12f

                displayLogs.forEachIndexed { idx, log ->
                    val metric = maxOf(log.runsCount, log.problemsSolved)
                    val ratio = metric.toFloat() / maxVal.toFloat()
                    val h = (chartHeight * ratio).coerceAtLeast(8f)
                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                    val top = chartHeight - h

                    drawRoundRect(
                        color = barColor.copy(alpha = 0.16f),
                        topLeft = Offset(left, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                displayLogs.forEach { log ->
                    Text(
                        text = log.dateKey.takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AccuracyTrendChart(
    accuracies: List<Int>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (accuracies.isEmpty()) {
        Text(
            text = "কোড ও মিনি-টেস্ট সম্পন্ন করলে নির্ভুলতার (Accuracy) গ্রাফ তৈরি হবে।",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val points = accuracies.takeLast(8)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val pad = 16f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val stepX = if (points.size > 1) w / (points.size - 1) else 0f

        val coords = points.mapIndexed { idx, acc ->
            val x = if (points.size == 1) size.width / 2f else pad + idx * stepX
            val y = pad + h * (1f - (acc.coerceIn(0, 100) / 100f))
            Offset(x, y)
        }

        // Baseline grid lines
        listOf(0f, 0.5f, 1f).forEach { frac ->
            val gy = pad + h * frac
            drawLine(
                color = lineColor.copy(alpha = 0.15f),
                start = Offset(pad, gy),
                end = Offset(size.width - pad, gy),
                strokeWidth = 2f
            )
        }

        if (coords.size > 1) {
            val linePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                for (k in 1 until coords.size) lineTo(coords[k].x, coords[k].y)
            }
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(coords.last().x, pad + h)
                lineTo(coords.first().x, pad + h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.28f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }

        coords.forEach { pt ->
            drawCircle(color = Color.White, radius = 9f, center = pt)
            drawCircle(color = lineColor, radius = 6f, center = pt)
        }
    }
}
