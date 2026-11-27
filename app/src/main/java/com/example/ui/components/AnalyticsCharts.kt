package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLogEntity
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary

data class ModuleCompletionStat(
    val moduleKey: String,
    val titleBn: String,
    val shortLabel: String,
    val language: String, // "HTML" or "C"
    val solved: Int,
    val total: Int,
    val completionPercent: Int,
    val avgBestAccuracy: Int,
    val sortOrder: Int
)

@Composable
fun CompletionRingCard(
    title: String,
    subtitle: String,
    percent: Int,
    ringColor: Color,
    modifier: Modifier = Modifier,
    ringSize: Dp = 86.dp
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (percent.coerceIn(0, 100) / 100f) * 360f,
        animationSpec = tween(700),
        label = "ring_sweep"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(ringSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.minDimension * 0.13f
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
                if (animatedSweep > 0f) {
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
            }
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SegmentedMasteryBar(
    htmlSolved: Int,
    cSolved: Int,
    totalProblems: Int,
    modifier: Modifier = Modifier
) {
    val safeTotal = totalProblems.coerceAtLeast(1)
    val htmlFraction by animateFloatAsState(
        targetValue = (htmlSolved.toFloat() / safeTotal).coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "html_segment"
    )
    val cFraction by animateFloatAsState(
        targetValue = (cSolved.toFloat() / safeTotal).coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "c_segment"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            drawRect(color = trackColor, size = size)
            val htmlWidth = size.width * htmlFraction
            val cWidth = size.width * cFraction
            if (htmlWidth > 0f) {
                drawRect(
                    color = TealPrimary,
                    topLeft = Offset.Zero,
                    size = Size(htmlWidth, size.height)
                )
            }
            if (cWidth > 0f) {
                drawRect(
                    color = AmberTertiary,
                    topLeft = Offset(htmlWidth, 0f),
                    size = Size(cWidth, size.height)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartLegendDot(color = TealPrimary, label = "HTML5 ($htmlSolved)")
            ChartLegendDot(color = AmberTertiary, label = "C ল্যাব ($cSolved)")
            ChartLegendDot(
                color = MaterialTheme.colorScheme.outline,
                label = "বাকি (${(totalProblems - htmlSolved - cSolved).coerceAtLeast(0)})"
            )
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModuleCompletionBarChart(
    modules: List<ModuleCompletionStat>,
    modifier: Modifier = Modifier
) {
    if (modules.isEmpty()) {
        Text(
            text = "এই ক্যাটাগরিতে এখনো কোনো মডিউল পাওয়া যায়নি।",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    var selectedModuleKey by remember(modules) {
        mutableStateOf(
            modules.maxByOrNull { it.completionPercent }?.moduleKey ?: modules.first().moduleKey
        )
    }
    LaunchedEffect(modules) {
        if (modules.none { it.moduleKey == selectedModuleKey }) {
            selectedModuleKey = modules.first().moduleKey
        }
    }

    val selectedStat = modules.firstOrNull { it.moduleKey == selectedModuleKey } ?: modules.first()
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelTextColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("module_completion_chart"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChartLegendDot(color = TealPrimary, label = "HTML5")
                ChartLegendDot(color = AmberTertiary, label = "C প্রোগ্রামিং")
                ChartLegendDot(color = SuccessGreen, label = "১০০% সম্পন্ন")
            }
            Text(
                text = "বিস্তারিত দেখতে বারে ট্যাপ করো",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val yAxisWidth = 36.dp
            val chartAreaWidth = (maxWidth - yAxisWidth).coerceAtLeast(180.dp)
            val minSlotWidth = 54.dp
            val slotWidth = if (modules.size * minSlotWidth.value <= chartAreaWidth.value) {
                chartAreaWidth / modules.size
            } else {
                minSlotWidth
            }
            val totalBarsWidth = slotWidth * modules.size
            val barAreaHeight = 148.dp

            Row(modifier = Modifier.fillMaxWidth()) {
                // Fixed Y-axis labels
                Column(
                    modifier = Modifier
                        .width(yAxisWidth)
                        .height(barAreaHeight + 24.dp)
                        .padding(top = 18.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.labelSmall,
                        color = axisTextColor,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "50%",
                        style = MaterialTheme.typography.labelSmall,
                        color = axisTextColor,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.labelSmall,
                        color = axisTextColor,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                // Scrollable bar chart plot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier.width(totalBarsWidth)) {
                        // Background horizontal grid lines matching the bar plot region
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barAreaHeight)
                                .padding(top = 20.dp, bottom = 4.dp)
                        ) {
                            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                                val y = size.height * frac
                                drawLine(
                                    color = outlineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = if (frac == 1f) 2.5f else 1.5f,
                                    pathEffect = if (frac == 1f) null else dash
                                )
                            }
                        }

                        // Individual clickable module columns
                        Row(modifier = Modifier.fillMaxWidth()) {
                            modules.forEach { stat ->
                                val isSelected = stat.moduleKey == selectedStat.moduleKey
                                val barColor = when {
                                    stat.completionPercent >= 100 -> SuccessGreen
                                    stat.language == "HTML" -> TealPrimary
                                    else -> AmberTertiary
                                }
                                val animatedRatio by animateFloatAsState(
                                    targetValue = (stat.completionPercent.coerceIn(0, 100) / 100f),
                                    animationSpec = tween(650),
                                    label = "module_bar_${stat.moduleKey}"
                                )

                                Column(
                                    modifier = Modifier
                                        .width(slotWidth)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) barColor.copy(alpha = 0.09f)
                                            else Color.Transparent
                                        )
                                        .clickable { selectedModuleKey = stat.moduleKey }
                                        .semantics {
                                            contentDescription =
                                                "${stat.titleBn}: ${stat.completionPercent}% (${stat.solved}/${stat.total})"
                                        }
                                        .testTag("module_bar_${stat.moduleKey}")
                                        .padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top % label + bar track area
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barAreaHeight),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(top = 20.dp, bottom = 4.dp)
                                        ) {
                                            val bw = (size.width * 0.58f).coerceIn(14f, 44f)
                                            val left = (size.width - bw) / 2f
                                            drawRoundRect(
                                                color = trackColor,
                                                topLeft = Offset(left, 0f),
                                                size = Size(bw, size.height),
                                                cornerRadius = CornerRadius(10f, 10f)
                                            )
                                            val fillHeight = (size.height * animatedRatio)
                                                .let { if (stat.completionPercent > 0) it.coerceAtLeast(8f) else 0f }
                                            if (fillHeight > 0f) {
                                                drawRoundRect(
                                                    color = barColor,
                                                    topLeft = Offset(left, size.height - fillHeight),
                                                    size = Size(bw, fillHeight),
                                                    cornerRadius = CornerRadius(10f, 10f)
                                                )
                                            }
                                            if (isSelected) {
                                                drawRoundRect(
                                                    color = barColor,
                                                    topLeft = Offset(left - 2f, 0f),
                                                    size = Size(bw + 4f, size.height),
                                                    cornerRadius = CornerRadius(12f, 12f),
                                                    style = Stroke(width = 3f)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${stat.completionPercent}%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                            fontWeight = if (isSelected || stat.completionPercent > 0) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            color = if (stat.completionPercent > 0) barColor else axisTextColor,
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(top = 2.dp),
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stat.shortLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) labelTextColor else axisTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected module inspector pill
        val inspectColor = when {
            selectedStat.completionPercent >= 100 -> SuccessGreen
            selectedStat.language == "HTML" -> TealPrimary
            else -> AmberTertiary
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, inspectColor.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (selectedStat.language == "HTML") "HTML5" else "C"} • ${selectedStat.titleBn}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val accText = if (selectedStat.avgBestAccuracy > 0) {
                        " • সেরা নির্ভুলতা: ${selectedStat.avgBestAccuracy}%"
                    } else {
                        " • এখনো শুরু হয়নি"
                    }
                    Text(
                        text = "${selectedStat.solved}/${selectedStat.total} সমস্যা সমাধান$accText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = inspectColor
                ) {
                    Text(
                        text = "${selectedStat.completionPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopicMasteryBar(
    topicName: String,
    solved: Int,
    total: Int,
    barColor: Color,
    language: String? = null,
    avgBestAccuracy: Int = 0
) {
    val ratioTarget = if (total > 0) (solved.toFloat() / total).coerceIn(0f, 1f) else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = ratioTarget,
        animationSpec = tween(600),
        label = "topic_bar_ratio"
    )
    val percent = if (total > 0) (solved * 100) / total else 0
    val effectiveBarColor = if (percent >= 100) SuccessGreen else barColor

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (language != null) {
                    val badgeColor = if (language == "HTML") TealPrimary else AmberTertiary
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = language,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = topicName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (avgBestAccuracy > 0) {
                    Text(
                        text = "$avgBestAccuracy% নির্ভুল",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$solved/$total ($percent%)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = effectiveBarColor
                )
            }
        }
        LinearProgressIndicator(
            progress = { animatedRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = effectiveBarColor,
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
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Column(modifier = modifier.fillMaxWidth()) {
        if (displayLogs.isEmpty()) {
            Text(
                text = "কোড রান করলে এখানে তোমার ৭ দিনের প্র্যাকটিস গ্রাফ দেখা যাবে!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val totalRuns = displayLogs.sumOf { it.runsCount }
            val totalSolved = displayLogs.sumOf { it.problemsSolved }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
            ) {
                val count = displayLogs.size.coerceAtLeast(1)
                val slotWidth = size.width / count
                val barWidth = (slotWidth * 0.48f).coerceIn(14f, 40f)
                val topPadding = 22f
                val bottomPadding = 8f
                val chartHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(24f)

                // Reference baseline and mid-line
                listOf(0f, 0.5f, 1f).forEach { frac ->
                    val y = topPadding + chartHeight * frac
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (frac == 1f) 2f else 1f
                    )
                }

                displayLogs.forEachIndexed { idx, log ->
                    val metric = maxOf(log.runsCount, log.problemsSolved)
                    val ratio = metric.toFloat() / maxVal.toFloat()
                    val h = (chartHeight * ratio).coerceAtLeast(8f)
                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                    val top = topPadding + chartHeight - h

                    drawRoundRect(
                        color = barColor.copy(alpha = 0.14f),
                        topLeft = Offset(left, topPadding),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    val measured = textMeasurer.measure("$metric", labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            x = idx * slotWidth + (slotWidth - measured.size.width) / 2f,
                            y = (top - measured.size.height - 2f).coerceAtLeast(0f)
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                displayLogs.forEach { log ->
                    Text(
                        text = log.dateKey.takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChartLegendDot(color = barColor, label = "সাম্প্রতিক রান: $totalRuns বার")
                ChartLegendDot(color = SuccessGreen, label = "সমাধান: $totalSolved টি")
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
    val textMeasurer = rememberTextMeasurer()
    val pointLabelStyle = TextStyle(
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = lineColor
    )
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(38.dp)
                    .height(132.dp)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = axisColor)
                Text("50%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = axisColor)
                Text("0%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = axisColor)
            }
            Spacer(Modifier.width(6.dp))
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(132.dp)
            ) {
                val topPad = 20f
                val bottomPad = 14f
                val sidePad = 18f
                val w = (size.width - sidePad * 2).coerceAtLeast(20f)
                val h = (size.height - topPad - bottomPad).coerceAtLeast(20f)
                val stepX = if (points.size > 1) w / (points.size - 1) else 0f

                val coords = points.mapIndexed { idx, acc ->
                    val x = if (points.size == 1) size.width / 2f else sidePad + idx * stepX
                    val y = topPad + h * (1f - (acc.coerceIn(0, 100) / 100f))
                    Offset(x, y)
                }

                // Baseline grid lines
                listOf(0f, 0.5f, 1f).forEach { frac ->
                    val gy = topPad + h * frac
                    drawLine(
                        color = lineColor.copy(alpha = 0.16f),
                        start = Offset(sidePad, gy),
                        end = Offset(size.width - sidePad, gy),
                        strokeWidth = if (frac == 1f) 2.5f else 1.5f
                    )
                }

                if (coords.size > 1) {
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (k in 1 until coords.size) lineTo(coords[k].x, coords[k].y)
                    }
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, topPad + h)
                        lineTo(coords.first().x, topPad + h)
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

                coords.forEachIndexed { idx, pt ->
                    drawCircle(color = Color.White, radius = 9f, center = pt)
                    drawCircle(color = lineColor, radius = 6f, center = pt)

                    // Draw label for first, last, and peak points (or all if <= 5) so values don't overlap
                    val showLabel = points.size <= 5 || idx == 0 || idx == points.lastIndex ||
                        points[idx] == points.maxOrNull()
                    if (showLabel) {
                        val measured = textMeasurer.measure("${points[idx]}%", pointLabelStyle)
                        val labelX = (pt.x - measured.size.width / 2f)
                            .coerceIn(0f, (size.width - measured.size.width).coerceAtLeast(0f))
                        val labelY = (pt.y - measured.size.height - 8f).coerceAtLeast(0f)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(labelX, labelY)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "সর্বশেষ: ${points.last()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = lineColor
            )
            Text(
                text = "সর্বোচ্চ: ${points.maxOrNull() ?: 0}% • গড়: ${points.average().toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
