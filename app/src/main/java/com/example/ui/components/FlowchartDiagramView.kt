package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlowNodeShape
import com.example.data.local.FlowchartDiagramSpec
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.InfoSky
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary

@Composable
fun FlowchartNodeBadge(
    shape: FlowNodeShape,
    label: String,
    modifier: Modifier = Modifier,
    minWidth: Dp = 170.dp
) {
    val lineColor = when (shape) {
        FlowNodeShape.TERMINAL -> TealPrimary
        FlowNodeShape.INPUT_OUTPUT -> InfoSky
        FlowNodeShape.PROCESS -> EmeraldSecondary
        FlowNodeShape.DECISION -> AmberTertiary
    }
    val fillColor = lineColor.copy(alpha = 0.12f)
    val textColor = MaterialTheme.colorScheme.onSurface

    when (shape) {
        FlowNodeShape.TERMINAL -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .widthIn(min = minWidth, max = 260.dp)
                    .background(fillColor, RoundedCornerShape(50))
                    .border(2.dp, lineColor, RoundedCornerShape(50))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        FlowNodeShape.PROCESS -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .widthIn(min = minWidth, max = 280.dp)
                    .background(fillColor, RoundedCornerShape(6.dp))
                    .border(2.dp, lineColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        FlowNodeShape.INPUT_OUTPUT -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .widthIn(min = minWidth, max = 280.dp)
                    .drawBehind {
                        val skew = 14.dp.toPx()
                        val path = Path().apply {
                            moveTo(skew, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width - skew, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path = path, color = fillColor)
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
                        )
                    }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        FlowNodeShape.DECISION -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .widthIn(min = 190.dp, max = 280.dp)
                    .drawBehind {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val path = Path().apply {
                            moveTo(cx, 0f)
                            lineTo(size.width, cy)
                            lineTo(cx, size.height)
                            lineTo(0f, cy)
                            close()
                        }
                        drawPath(path = path, color = fillColor)
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
                        )
                    }
                    .padding(horizontal = 34.dp, vertical = 22.dp)
            ) {
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FlowDownArrow(
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    height: Dp = 20.dp,
    tagLabel: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(16.dp)
                .height(height)
        ) {
            val cx = size.width / 2f
            val stroke = 2.dp.toPx()
            drawLine(
                color = color,
                start = Offset(cx, 0f),
                end = Offset(cx, size.height - 4.dp.toPx()),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            val head = Path().apply {
                moveTo(cx - 5.dp.toPx(), size.height - 7.dp.toPx())
                lineTo(cx, size.height)
                lineTo(cx + 5.dp.toPx(), size.height - 7.dp.toPx())
                close()
            }
            drawPath(head, color = color)
        }
        if (tagLabel != null) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = tagLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FlowchartDiagramView(
    spec: FlowchartDiagramSpec,
    modifier: Modifier = Modifier
) {
    val arrowColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (spec) {
            is FlowchartDiagramSpec.Linear -> {
                spec.steps.forEachIndexed { idx, (shape, label) ->
                    FlowchartNodeBadge(shape = shape, label = label)
                    if (idx < spec.steps.lastIndex) {
                        FlowDownArrow(color = arrowColor)
                    }
                }
            }

            is FlowchartDiagramSpec.TwoBranchDecision -> {
                FlowchartNodeBadge(FlowNodeShape.TERMINAL, spec.startLabel)
                FlowDownArrow(color = arrowColor)
                FlowchartNodeBadge(FlowNodeShape.INPUT_OUTPUT, spec.inputLabel)
                FlowDownArrow(color = arrowColor)
                if (spec.preProcessLabel != null) {
                    FlowchartNodeBadge(FlowNodeShape.PROCESS, spec.preProcessLabel)
                    FlowDownArrow(color = arrowColor)
                }
                FlowchartNodeBadge(FlowNodeShape.DECISION, spec.conditionLabel)

                // Branch split connector lines
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                ) {
                    val leftX = size.width * 0.25f
                    val rightX = size.width * 0.75f
                    val midX = size.width / 2f
                    val midY = size.height * 0.45f
                    val stroke = 2.dp.toPx()

                    drawLine(arrowColor, Offset(midX, 0f), Offset(midX, midY), stroke)
                    drawLine(SuccessGreen, Offset(midX, midY), Offset(leftX, midY), stroke)
                    drawLine(SuccessGreen, Offset(leftX, midY), Offset(leftX, size.height), stroke)
                    drawLine(ErrorCrimson, Offset(midX, midY), Offset(rightX, midY), stroke)
                    drawLine(ErrorCrimson, Offset(rightX, midY), Offset(rightX, size.height), stroke)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "হ্যাঁ (Yes)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        FlowchartNodeBadge(
                            shape = FlowNodeShape.INPUT_OUTPUT,
                            label = spec.yesBranchLabel,
                            minWidth = 120.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorCrimson.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "না (No)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorCrimson,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        FlowchartNodeBadge(
                            shape = FlowNodeShape.INPUT_OUTPUT,
                            label = spec.noBranchLabel,
                            minWidth = 120.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Merge back to Stop
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                ) {
                    val leftX = size.width * 0.25f
                    val rightX = size.width * 0.75f
                    val midX = size.width / 2f
                    val midY = size.height * 0.45f
                    val stroke = 2.dp.toPx()

                    drawLine(arrowColor, Offset(leftX, 0f), Offset(leftX, midY), stroke)
                    drawLine(arrowColor, Offset(rightX, 0f), Offset(rightX, midY), stroke)
                    drawLine(arrowColor, Offset(leftX, midY), Offset(rightX, midY), stroke)
                    drawLine(arrowColor, Offset(midX, midY), Offset(midX, size.height - 4.dp.toPx()), stroke)
                    val head = Path().apply {
                        moveTo(midX - 5.dp.toPx(), size.height - 7.dp.toPx())
                        lineTo(midX, size.height)
                        lineTo(midX + 5.dp.toPx(), size.height - 7.dp.toPx())
                        close()
                    }
                    drawPath(head, color = arrowColor)
                }

                FlowchartNodeBadge(FlowNodeShape.TERMINAL, spec.endLabel)
            }

            is FlowchartDiagramSpec.LoopFlow -> {
                FlowchartNodeBadge(FlowNodeShape.TERMINAL, spec.startLabel)
                FlowDownArrow(color = arrowColor)
                FlowchartNodeBadge(FlowNodeShape.INPUT_OUTPUT, spec.inputLabel)
                FlowDownArrow(color = arrowColor)
                FlowchartNodeBadge(FlowNodeShape.PROCESS, spec.initLabel)
                FlowDownArrow(color = arrowColor)

                // Loop Section with visible loop-back track on the left
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Loop-back Arrow Rail
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxHeight().width(28.dp)) {
                            val railX = size.width * 0.35f
                            val topY = 28.dp.toPx()
                            val bottomY = size.height - 24.dp.toPx()
                            val stroke = 2.dp.toPx()
                            val loopColor = TealPrimary

                            // Horizontal line from bottom update box to rail
                            drawLine(loopColor, Offset(size.width, bottomY), Offset(railX, bottomY), stroke)
                            // Vertical line up to condition level
                            drawLine(loopColor, Offset(railX, bottomY), Offset(railX, topY), stroke)
                            // Horizontal arrow into condition diamond
                            drawLine(loopColor, Offset(railX, topY), Offset(size.width - 4.dp.toPx(), topY), stroke)
                            val head = Path().apply {
                                moveTo(size.width - 8.dp.toPx(), topY - 4.dp.toPx())
                                lineTo(size.width, topY)
                                lineTo(size.width - 8.dp.toPx(), topY + 4.dp.toPx())
                                close()
                            }
                            drawPath(head, color = loopColor)
                        }
                    }

                    // Main Loop Body Column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlowchartNodeBadge(FlowNodeShape.DECISION, spec.conditionLabel)
                        FlowDownArrow(color = SuccessGreen, tagLabel = "হ্যাঁ (Yes)")
                        FlowchartNodeBadge(FlowNodeShape.PROCESS, spec.bodyProcessLabel)
                        FlowDownArrow(color = arrowColor)
                        FlowchartNodeBadge(FlowNodeShape.PROCESS, spec.updateLabel)
                    }
                }

                // Exit path (No / মিথ্যা)
                FlowDownArrow(color = ErrorCrimson, height = 24.dp, tagLabel = "না (Loop Exit)")
                FlowchartNodeBadge(FlowNodeShape.INPUT_OUTPUT, spec.outputLabel)
                FlowDownArrow(color = arrowColor)
                FlowchartNodeBadge(FlowNodeShape.TERMINAL, spec.endLabel)
            }
        }
    }
}
