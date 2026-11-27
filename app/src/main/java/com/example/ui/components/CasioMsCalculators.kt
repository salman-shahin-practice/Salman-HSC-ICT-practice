package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AngleUnit
import com.example.engine.BaseOperation
import com.example.engine.NumberBase
import com.example.engine.ScientificNumberCalculator
import com.example.engine.inBase
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.InfoSky
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TealPrimary
import kotlin.math.pow

enum class CasioMsModel(
    val modelCode: String,
    val subtitleBn: String,
    val functionsBadge: String
) {
    FX_100MS("fx-100MS", "S-V.P.A.M. • ক্লাসিক ২-লাইন", "240 FUNCTIONS"),
    FX_991MS("fx-991MS", "S-V.P.A.M. • সোলার + BASE-N/EQN", "401 FUNCTIONS")
}

private enum class Fx100Mode(val label: String) {
    COMP("COMP"),
    SD("SD (পরিসংখ্যান)")
}

private enum class Fx991Mode(val label: String) {
    COMP("COMP"),
    BASE_N("BASE-N"),
    EQN("EQN"),
    CMPLX("CMPLX"),
    CALC_INT("∫dx / d/dx"),
    SD("SD")
}

private data class MsHistoryItem(val expr: String, val ans: String)

@Composable
fun CasioMsCalculatorDevice(
    model: CasioMsModel,
    modifier: Modifier = Modifier
) {
    val is991 = model == CasioMsModel.FX_991MS
    val clipboard = LocalClipboardManager.current

    // Shared scientific calculator state per model instance
    var expression by remember(model) { mutableStateOf(if (is991) "5C2 + sin(30)" else "sin(30) + 1÷2") }
    var angleUnit by remember(model) { mutableStateOf(AngleUnit.DEG) }
    var shiftActive by remember(model) { mutableStateOf(false) }
    var alphaActive by remember(model) { mutableStateOf(false) }
    var hypActive by remember(model) { mutableStateOf(false) }
    var fractionDisplay by remember(model) { mutableStateOf(false) }
    var stoPending by remember(model) { mutableStateOf(false) }
    var rclPending by remember(model) { mutableStateOf(false) }
    var lastAnswer by remember(model) { mutableStateOf(if (is991) 10.5 else 1.0) }
    var displayResult by remember(model) { mutableStateOf(if (is991) "10.5" else "1") }
    var errorMsg by remember(model) { mutableStateOf<String?>(null) }

    val memories = remember(model) {
        mutableStateMapOf("A" to 0.0, "B" to 0.0, "C" to 0.0, "D" to 0.0, "X" to 0.0, "Y" to 0.0, "M" to 0.0)
    }
    val history = remember(model) { mutableStateListOf<MsHistoryItem>() }
    var historyCursor by remember(model) { mutableIntStateOf(-1) }

    // Mode states
    var fx100Mode by remember(model) { mutableStateOf(Fx100Mode.COMP) }
    var fx991Mode by remember(model) { mutableStateOf(Fx991Mode.COMP) }

    // SD Statistics state
    val sdSamples = remember(model) { mutableStateListOf(10.0, 20.0, 30.0) }
    var sdInput by remember(model) { mutableStateOf("25") }

    val liveEval = remember(expression, angleUnit, lastAnswer, memories.toMap()) {
        if (expression.isBlank()) null
        else ScientificNumberCalculator.evaluateScientificExpression(
            rawExpression = expression,
            angleUnit = angleUnit,
            lastAnswer = lastAnswer,
            variables = memories
        )
    }

    fun evaluateNow() {
        val res = ScientificNumberCalculator.evaluateScientificExpression(
            rawExpression = expression,
            angleUnit = angleUnit,
            lastAnswer = lastAnswer,
            variables = memories
        )
        if (res.success && res.numericValue != null) {
            lastAnswer = res.numericValue
            displayResult = if (fractionDisplay) {
                ScientificNumberCalculator.toFractionString(res.numericValue)
            } else {
                res.formattedResult
            }
            errorMsg = null
            history.removeAll { it.expr == expression && it.ans == res.formattedResult }
            history.add(0, MsHistoryItem(expression, res.formattedResult))
            if (history.size > 10) history.removeAt(history.lastIndex)
            historyCursor = -1
        } else {
            errorMsg = res.errorMessageBn ?: "Syntax ERROR"
        }
        shiftActive = false
        alphaActive = false
        hypActive = false
        stoPending = false
        rclPending = false
    }

    fun handleVariableKey(varName: String): Boolean {
        if (stoPending) {
            val currentVal = liveEval?.numericValue ?: lastAnswer
            memories[varName] = currentVal
            displayResult = ScientificNumberCalculator.formatDoubleClean(currentVal)
            errorMsg = null
            stoPending = false
            shiftActive = false
            return true
        }
        if (rclPending) {
            val v = memories[varName] ?: 0.0
            expression += ScientificNumberCalculator.formatDoubleClean(v)
            rclPending = false
            return true
        }
        if (alphaActive) {
            expression += varName
            alphaActive = false
            return true
        }
        return false
    }

    fun appendToken(tok: String) {
        errorMsg = null
        expression += tok
        shiftActive = false
        alphaActive = false
        hypActive = false
    }

    fun deleteLast() {
        errorMsg = null
        if (expression.isEmpty()) return
        val tokens = listOf(
            "asinh(", "acosh(", "atanh(", "sinh(", "cosh(", "tanh(",
            "asin(", "acos(", "atan(", "sqrt(", "cbrt(", "sin(", "cos(",
            "tan(", "log(", "ln(", "abs(", "exp(", "Ans"
        )
        val matched = tokens.firstOrNull { expression.endsWith(it) }
        expression = if (matched != null) expression.dropLast(matched.length) else expression.dropLast(1)
    }

    // Chassis colors matching real fx-100MS (Dark Slate Charcoal) vs fx-991MS (Metallic Silver-Slate)
    val chassisBrush = if (is991) {
        Brush.verticalGradient(listOf(Color(0xFF3B4758), Color(0xFF1E2633)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF26303D), Color(0xFF151C26)))
    }
    val bezelBorder = if (is991) Color(0xFF94A3B8) else Color(0xFF475569)
    val lcdBg = if (is991) Color(0xFFD5E4E8) else Color(0xFFC8DAC4)
    val lcdText = Color(0xFF0F172A)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Handheld Calculator Body
        Surface(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, bezelBorder.copy(alpha = 0.65f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .testTag(if (is991) "casio_fx991ms_body" else "casio_fx100ms_body")
        ) {
            Column(
                modifier = Modifier
                    .background(chassisBrush)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Brand Faceplate + Solar Cell (on fx-991MS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MS SERIES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (is991) Color(0xFFE2E8F0) else Color(0xFFCBD5E1),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (is991) Color(0xFF0EA5E9).copy(alpha = 0.25f) else AmberTertiary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = model.modelCode,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is991) Color(0xFF7DD3FC) else Color(0xFFFCD34D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${model.subtitleBn} • ${model.functionsBadge}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    if (is991) {
                        // Signature fx-991MS Solar Panel Strip
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF2B1D19),
                                border = BorderStroke(1.dp, Color(0xFF94A3B8))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 10.dp, height = 14.dp)
                                                .background(Color(0xFF45271F))
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.WbSunny,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "TWO WAY POWER",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF334155)
                        ) {
                            Text(
                                text = "BATTERY",
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Mode Switcher Strip inside the calculator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (is991) {
                        Fx991Mode.entries.forEach { m ->
                            val selected = fx991Mode == m
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) TealPrimary else Color(0xFF0F172A).copy(alpha = 0.78f),
                                border = BorderStroke(1.dp, if (selected) Color(0xFF5EEAD4) else Color(0xFF64748B)),
                                modifier = Modifier
                                    .clickable { fx991Mode = m }
                                    .testTag("fx991_mode_${m.name}")
                            ) {
                                Text(
                                    text = m.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        Fx100Mode.entries.forEach { m ->
                            val selected = fx100Mode == m
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) AmberTertiary else Color(0xFF0F172A).copy(alpha = 0.78f),
                                border = BorderStroke(1.dp, if (selected) Color(0xFFFDE68A) else Color(0xFF64748B)),
                                modifier = Modifier
                                    .clickable { fx100Mode = m }
                                    .testTag("fx100_mode_${m.name}")
                            ) {
                                Text(
                                    text = m.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // If fx-991MS is in special non-COMP interactive modes (BASE-N, EQN, CMPLX, CALC/INT, SD), show specialized panel below LCD or inline!
                val showScientificKeypad = if (is991) fx991Mode == Fx991Mode.COMP else fx100Mode == Fx100Mode.COMP

                if (showScientificKeypad) {
                    // Authentic 2-Line S-V.P.A.M. LCD Screen
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = lcdBg,
                        border = BorderStroke(2.dp, Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Top status annunciators: S, A, HYP, STO, RCL, D/R/G, a b/c, M
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    LcdStatusBadge("S", shiftActive, Color(0xFFB45309))
                                    LcdStatusBadge("A", alphaActive, Color(0xFFBE123C))
                                    LcdStatusBadge("HYP", hypActive, lcdText)
                                    LcdStatusBadge("STO", stoPending, lcdText)
                                    LcdStatusBadge("RCL", rclPending, lcdText)
                                    LcdStatusBadge("a b/c", fractionDisplay, lcdText)
                                    if ((memories["M"] ?: 0.0) != 0.0) {
                                        LcdStatusBadge("M", true, lcdText)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = lcdText.copy(alpha = 0.12f),
                                        modifier = Modifier.clickable {
                                            angleUnit = when (angleUnit) {
                                                AngleUnit.DEG -> AngleUnit.RAD
                                                AngleUnit.RAD -> AngleUnit.GRA
                                                AngleUnit.GRA -> AngleUnit.DEG
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = angleUnit.label,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = lcdText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy result",
                                        tint = lcdText.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clickable {
                                                clipboard.setText(AnnotatedString(displayResult))
                                            }
                                    )
                                }
                            }

                            // Line 1: Dot-Matrix Expression
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = expression.ifEmpty { "0" },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = lcdText,
                                    maxLines = 1,
                                    modifier = Modifier.testTag(if (is991) "fx991_lcd_expr" else "fx100_lcd_expr")
                                )
                            }

                            // Line 2: 7-segment style numeric result (or fraction)
                            val shownOut = when {
                                errorMsg != null -> errorMsg!!
                                liveEval?.success == true && liveEval.numericValue != null -> {
                                    if (fractionDisplay) ScientificNumberCalculator.toFractionString(liveEval.numericValue)
                                    else liveEval.formattedResult
                                }
                                else -> displayResult
                            }
                            Text(
                                text = shownOut,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = if (errorMsg != null) 14.sp else 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (errorMsg != null) ErrorCrimson else lcdText,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(if (is991) "fx991_lcd_result" else "fx100_lcd_result")
                            )

                            // fx-991MS bonus live base strip on integer values
                            val baseSum = liveEval?.integerBaseSummary
                            if (is991 && baseSum != null && baseSum.valid) {
                                HorizontalDivider(color = lcdText.copy(alpha = 0.2f))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("BIN:${baseSum.binary}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = lcdText, fontWeight = FontWeight.Bold)
                                    Text("OCT:${baseSum.octal}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = lcdText, fontWeight = FontWeight.Bold)
                                    Text("HEX:${baseSum.hex}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = lcdText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Top Function Control Row: SHIFT, ALPHA, REPLAY ▲▼, DRG, S⇔D
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        MsKeyButton(
                            primary = "SHIFT",
                            secondary = "2nd",
                            bg = if (shiftActive) Color(0xFFF59E0B) else Color(0xFF1E293B),
                            fg = if (shiftActive) Color.Black else Color(0xFFFDE047),
                            onClick = {
                                shiftActive = !shiftActive
                                alphaActive = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "ALPHA",
                            secondary = "VAR",
                            bg = if (alphaActive) Color(0xFFE11D48) else Color(0xFF1E293B),
                            fg = if (alphaActive) Color.White else Color(0xFFFDA4AF),
                            onClick = {
                                alphaActive = !alphaActive
                                shiftActive = false
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Authentic Oval REPLAY Rocker (▲ / ▼ history) with clean header label
                        Column(
                            modifier = Modifier.weight(1.25f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "REPLAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF334155),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .clickable {
                                                if (history.isNotEmpty()) {
                                                    historyCursor = (historyCursor + 1).coerceAtMost(history.lastIndex)
                                                    expression = history[historyCursor].expr
                                                }
                                            }
                                    ) {
                                        Icon(
                                            Icons.Rounded.ArrowDropUp,
                                            contentDescription = "Previous",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(22.dp)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .clickable {
                                                if (history.isNotEmpty() && historyCursor > 0) {
                                                    historyCursor--
                                                    expression = history[historyCursor].expr
                                                }
                                            }
                                    ) {
                                        Icon(
                                            Icons.Rounded.ArrowDropDown,
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }

                        MsKeyButton(
                            primary = angleUnit.label,
                            secondary = "DRG",
                            bg = Color(0xFF334155),
                            fg = Color.White,
                            onClick = {
                                angleUnit = when (angleUnit) {
                                    AngleUnit.DEG -> AngleUnit.RAD
                                    AngleUnit.RAD -> AngleUnit.GRA
                                    AngleUnit.GRA -> AngleUnit.DEG
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = if (fractionDisplay) "S⇔D" else "a b/c",
                            secondary = "FRAC",
                            bg = if (fractionDisplay) TealPrimary else Color(0xFF334155),
                            fg = Color.White,
                            onClick = { fractionDisplay = !fractionDisplay },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // MS Scientific Keys Row 1
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        MsKeyButton(
                            primary = "x⁻¹",
                            secondary = "x!",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "!" else "^(-1)") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "nCr",
                            secondary = "nPr",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "P" else "C") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "x³",
                            secondary = "∛",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "cbrt(" else "^3") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "√",
                            secondary = "abs",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "abs(" else "sqrt(") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "x²",
                            secondary = "mod",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) " mod " else "^2") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "^",
                            secondary = "π",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "π" else "^") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // MS Scientific Keys Row 2 (log, ln, hyp, sin, cos, tan)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        MsKeyButton(
                            primary = "log",
                            secondary = "10ˣ",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "10^" else "log(") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "ln",
                            secondary = "eˣ",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { appendToken(if (shiftActive) "exp(" else "ln(") },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "hyp",
                            bg = if (hypActive) AmberTertiary else Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = { hypActive = !hypActive },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "sin",
                            secondary = "sin⁻¹",
                            alphaLabel = "D",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                if (!handleVariableKey("D")) {
                                    val fn = when {
                                        hypActive && shiftActive -> "asinh("
                                        hypActive -> "sinh("
                                        shiftActive -> "asin("
                                        else -> "sin("
                                    }
                                    appendToken(fn)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "cos",
                            secondary = "cos⁻¹",
                            alphaLabel = "E",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                val fn = when {
                                    hypActive && shiftActive -> "acosh("
                                    hypActive -> "cosh("
                                    shiftActive -> "acos("
                                    else -> "cos("
                                }
                                appendToken(fn)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "tan",
                            secondary = "tan⁻¹",
                            alphaLabel = "F",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                val fn = when {
                                    hypActive && shiftActive -> "atanh("
                                    hypActive -> "tanh("
                                    shiftActive -> "atan("
                                    else -> "tan("
                                }
                                appendToken(fn)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // MS Memory & Brackets Row: RCL/STO, (-), (, ), M+/M-, A/B/C/X/Y
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        MsKeyButton(
                            primary = "RCL",
                            secondary = "STO",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                if (shiftActive) {
                                    stoPending = true
                                    rclPending = false
                                    shiftActive = false
                                } else {
                                    rclPending = !rclPending
                                    stoPending = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "(−)",
                            alphaLabel = "A",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                if (!handleVariableKey("A")) appendToken("-")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "(",
                            alphaLabel = "B",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                if (!handleVariableKey("B")) appendToken("(")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = ")",
                            alphaLabel = "C",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                if (!handleVariableKey("C")) appendToken(")")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "X",
                            alphaLabel = "Y",
                            bg = Color(0xFF1E293B),
                            fg = Color(0xFFFDA4AF),
                            onClick = {
                                val target = if (alphaActive || stoPending || rclPending) {
                                    if (shiftActive) "Y" else "X"
                                } else null
                                if (target == null || !handleVariableKey(target)) {
                                    appendToken("X")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MsKeyButton(
                            primary = "M+",
                            secondary = "M−",
                            alphaLabel = "M",
                            bg = Color(0xFF1E293B),
                            fg = Color.White,
                            onClick = {
                                if (!handleVariableKey("M")) {
                                    val v = liveEval?.numericValue ?: lastAnswer
                                    val prev = memories["M"] ?: 0.0
                                    memories["M"] = if (shiftActive) prev - v else prev + v
                                    shiftActive = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Main 5-Column Casio Number & Operator Keypad
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MsKeyButton("7", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("7") }, modifier = Modifier.weight(1f))
                        MsKeyButton("8", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("8") }, modifier = Modifier.weight(1f))
                        MsKeyButton("9", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("9") }, modifier = Modifier.weight(1f))
                        MsKeyButton("DEL", bg = Color(0xFFD97706), fg = Color.White, onClick = { deleteLast() }, modifier = Modifier.weight(1f))
                        MsKeyButton("AC", secondary = "OFF", bg = Color(0xFFDC2626), fg = Color.White, onClick = {
                            expression = ""
                            displayResult = "0"
                            errorMsg = null
                            shiftActive = false
                            alphaActive = false
                        }, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MsKeyButton("4", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("4") }, modifier = Modifier.weight(1f))
                        MsKeyButton("5", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("5") }, modifier = Modifier.weight(1f))
                        MsKeyButton("6", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("6") }, modifier = Modifier.weight(1f))
                        MsKeyButton("×", bg = Color(0xFF1E293B), fg = Color(0xFF99F6E4), onClick = { appendToken("×") }, modifier = Modifier.weight(1f))
                        MsKeyButton("÷", bg = Color(0xFF1E293B), fg = Color(0xFF99F6E4), onClick = { appendToken("÷") }, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MsKeyButton("1", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("1") }, modifier = Modifier.weight(1f))
                        MsKeyButton("2", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("2") }, modifier = Modifier.weight(1f))
                        MsKeyButton("3", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("3") }, modifier = Modifier.weight(1f))
                        MsKeyButton("+", bg = Color(0xFF1E293B), fg = Color(0xFF99F6E4), onClick = { appendToken("+") }, modifier = Modifier.weight(1f))
                        MsKeyButton("−", bg = Color(0xFF1E293B), fg = Color(0xFF99F6E4), onClick = { appendToken("-") }, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MsKeyButton("0", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken("0") }, modifier = Modifier.weight(1f))
                        MsKeyButton(".", bg = Color(0xFF334155), fg = Color.White, onClick = { appendToken(".") }, modifier = Modifier.weight(1f))
                        MsKeyButton("EXP", secondary = "e", bg = Color(0xFF334155), fg = Color.White, onClick = {
                            appendToken(if (shiftActive) "e" else "*10^")
                        }, modifier = Modifier.weight(1f))
                        MsKeyButton("Ans", secondary = "%", bg = Color(0xFF334155), fg = Color(0xFFFEF08A), onClick = {
                            appendToken(if (shiftActive) "%" else "Ans")
                        }, modifier = Modifier.weight(1f))
                        MsKeyButton(
                            primary = "=",
                            bg = TealPrimary,
                            fg = Color.White,
                            onClick = { evaluateNow() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag(if (is991) "fx991_equals_btn" else "fx100_equals_btn")
                        )
                    }

                    // Active Memory Variables Strip
                    val activeVars = memories.filterValues { it != 0.0 }
                    if (activeVars.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                activeVars.forEach { (k, v) ->
                                    Text(
                                        text = "$k=${ScientificNumberCalculator.formatDoubleClean(v)}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFCD34D)
                                    )
                                }
                            }
                        }
                    }

                    // Scientific Constants quick insert strip for fx-991MS
                    if (is991) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val constants = listOf(
                                "c (2.998e8)" to "2.99792458E8",
                                "g (9.80665)" to "9.80665",
                                "h (6.626e-34)" to "6.62607E-34",
                                "R (8.314)" to "8.31446",
                                "Nₐ (6.022e23)" to "6.02214E23"
                            )
                            constants.forEach { (label, valStr) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, Color(0xFF475569)),
                                    modifier = Modifier.clickable { appendToken(valStr) }
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFBAE6FD),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Specialized Sub-modes inside fx-100MS (SD) or fx-991MS (BASE-N, EQN, CMPLX, ∫dx/d/dx, SD)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!is991) {
                                MsSdStatsPanel(
                                    samples = sdSamples,
                                    inputText = sdInput,
                                    onInputChange = { sdInput = it }
                                )
                            } else {
                                when (fx991Mode) {
                                    Fx991Mode.BASE_N -> Fx991BaseNPanel()
                                    Fx991Mode.EQN -> Fx991EqnPanel()
                                    Fx991Mode.CMPLX -> Fx991ComplexPanel(angleUnit = angleUnit)
                                    Fx991Mode.CALC_INT -> Fx991CalculusPanel(angleUnit = angleUnit)
                                    Fx991Mode.SD -> MsSdStatsPanel(
                                        samples = sdSamples,
                                        inputText = sdInput,
                                        onInputChange = { sdInput = it }
                                    )
                                    Fx991Mode.COMP -> Unit
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
private fun LcdStatusBadge(text: String, active: Boolean, color: Color) {
    if (!active) return
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = color.copy(alpha = 0.16f)
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun MsKeyButton(
    primary: String,
    secondary: String? = null,
    alphaLabel: String? = null,
    bg: Color,
    fg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDigitOrOp = primary.length == 1 && (primary[0].isDigit() || primary in listOf(".", "+", "−", "×", "÷", "=", "(", ")", "^", "√", "X"))
    val labelFontSize = when {
        isDigitOrOp -> 17.sp
        primary.length <= 2 -> 15.sp
        primary.length == 3 -> 13.5.sp
        else -> 11.5.sp
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (secondary != null || alphaLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (secondary != null) {
                    Text(
                        text = secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047),
                        maxLines = 1,
                        softWrap = false
                    )
                }
                if (secondary != null && alphaLabel != null) {
                    Spacer(Modifier.width(4.dp))
                }
                if (alphaLabel != null) {
                    Text(
                        text = alphaLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDA4AF),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(9.dp),
            color = bg,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(9.dp))
                .clickable(onClick = onClick)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = primary,
                    fontFamily = if (isDigitOrOp) JetBrainsMonoFontFamily else androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.Bold,
                    color = fg,
                    maxLines = 1,
                    softWrap = false,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

@Composable
private fun MsSdStatsPanel(
    samples: MutableList<Double>,
    inputText: String,
    onInputChange: (String) -> Unit
) {
    val stats = ScientificNumberCalculator.computeSdStats(samples)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "SD পরিসংখ্যান মোড (M+ দিয়ে ডাটা যোগ করো)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                label = { Text("ডাটা মান (x)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    inputText.trim().toDoubleOrNull()?.let { samples.add(it) }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text("DT (M+)")
            }
            Button(
                onClick = { samples.clear() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Text("CLR")
            }
        }
        Text(
            text = "তালিকা (n=${samples.size}): ${samples.joinToString(", ") { ScientificNumberCalculator.formatDoubleClean(it) }}",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (stats != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TealPrimary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "n = ${stats.n}   |   Σx = ${ScientificNumberCalculator.formatDoubleClean(stats.sumX)}   |   Σx² = ${ScientificNumberCalculator.formatDoubleClean(stats.sumX2)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "গড় (x̄) = ${ScientificNumberCalculator.formatDoubleClean(stats.mean)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "σn = ${ScientificNumberCalculator.formatDoubleClean(stats.popStdDev)}   |   σn-1 = ${ScientificNumberCalculator.formatDoubleClean(stats.sampleStdDev)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun Fx991BaseNPanel() {
    var activeBase by remember { mutableStateOf(NumberBase.DEC) }
    var leftInput by remember { mutableStateOf("25") }
    var rightInput by remember { mutableStateOf("14") }
    var op by remember { mutableStateOf(BaseOperation.ADD) }

    val singleConv = remember(leftInput, activeBase) {
        ScientificNumberCalculator.convertNumberBase(leftInput, activeBase, NumberBase.BIN)
    }
    val arith = remember(leftInput, rightInput, activeBase, op) {
        ScientificNumberCalculator.evaluateBaseArithmetic(leftInput, rightInput, activeBase, op)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "fx-991MS BASE-N মোড (DEC / HEX / BIN / OCT)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NumberBase.entries.forEach { b ->
                FilterChip(
                    selected = activeBase == b,
                    onClick = {
                        if (singleConv.valid) leftInput = singleConv.inBase(b)
                        activeBase = b
                    },
                    label = { Text(b.shortLabel, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fx991_base_${b.shortLabel}")
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = leftInput,
                onValueChange = { leftInput = it.uppercase() },
                label = { Text("A (${activeBase.shortLabel})") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = rightInput,
                onValueChange = { rightInput = it.uppercase() },
                label = { Text("B (${activeBase.shortLabel})") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BaseOperation.entries.forEach { item ->
                FilterChip(
                    selected = op == item,
                    onClick = { op = item },
                    label = { Text(item.symbol, fontWeight = FontWeight.Bold) }
                )
            }
        }
        if (singleConv.valid) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = InfoSky.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "A-এর মান: BIN=${singleConv.binary} | OCT=${singleConv.octal} | DEC=${singleConv.decimal} | HEX=${singleConv.hex}",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        if (arith.valid && arith.conversion != null) {
            val res = arith.conversion
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = EmeraldSecondary.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ফলাফল (${activeBase.shortLabel}): ${res.inBase(activeBase)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSecondary
                    )
                    Text(
                        text = "BIN: ${res.binary}  •  OCT: ${res.octal}  •  DEC: ${res.decimal}  •  HEX: ${res.hex}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else if (!arith.valid) {
            Text(
                text = arith.errorMessageBn ?: "অবৈধ ইনপুট",
                color = ErrorCrimson,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Fx991EqnPanel() {
    var eqnType by remember { mutableIntStateOf(0) } // 0 = Quadratic, 1 = Linear 2x2
    var a1 by remember { mutableStateOf("1") }
    var b1 by remember { mutableStateOf("-5") }
    var c1 by remember { mutableStateOf("6") }
    var a2 by remember { mutableStateOf("2") }
    var b2 by remember { mutableStateOf("1") }
    var c2 by remember { mutableStateOf("5") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "fx-991MS EQN (সমীকরণ সমাধান)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = eqnType == 0,
                onClick = { eqnType = 0 },
                label = { Text("ax² + bx + c = 0") }
            )
            FilterChip(
                selected = eqnType == 1,
                onClick = { eqnType = 1 },
                label = { Text("2-Unknown (x, y)") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text(if (eqnType == 0) "a" else "a₁") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = b1, onValueChange = { b1 = it }, label = { Text(if (eqnType == 0) "b" else "b₁") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text(if (eqnType == 0) "c" else "c₁") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
        }
        if (eqnType == 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("a₂") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = b2, onValueChange = { b2 = it }, label = { Text("b₂") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("c₂") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
            }
        }
        val lines = remember(eqnType, a1, b1, c1, a2, b2, c2) {
            val va1 = a1.toDoubleOrNull()
            val vb1 = b1.toDoubleOrNull()
            val vc1 = c1.toDoubleOrNull()
            if (eqnType == 0) {
                if (va1 == null || vb1 == null || vc1 == null) listOf("সঠিক সংখ্যা লিখুন")
                else ScientificNumberCalculator.solveQuadratic(va1, vb1, vc1)
            } else {
                val va2 = a2.toDoubleOrNull()
                val vb2 = b2.toDoubleOrNull()
                val vc2 = c2.toDoubleOrNull()
                if (va1 == null || vb1 == null || vc1 == null || va2 == null || vb2 == null || vc2 == null) listOf("সঠিক সংখ্যা লিখুন")
                else ScientificNumberCalculator.solveLinear2x2(va1, vb1, vc1, va2, vb2, vc2)
            }
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = TealPrimary.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                lines.forEach { line ->
                    Text(line, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun Fx991ComplexPanel(angleUnit: AngleUnit) {
    var a by remember { mutableStateOf("3") }
    var b by remember { mutableStateOf("4") }
    var c by remember { mutableStateOf("1") }
    var d by remember { mutableStateOf("-2") }
    var op by remember { mutableStateOf("+") }

    val res = remember(a, b, c, d, op, angleUnit) {
        val va = a.toDoubleOrNull()
        val vb = b.toDoubleOrNull()
        val vc = c.toDoubleOrNull()
        val vd = d.toDoubleOrNull()
        if (va == null || vb == null || vc == null || vd == null) null
        else ScientificNumberCalculator.evaluateComplex(va, vb, vc, vd, op, angleUnit)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "fx-991MS CMPLX (জটিল সংখ্যা: a + bi)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a (Re₁)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b (Im₁)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("+", "−", "×", "÷").forEach { symbol ->
                FilterChip(selected = op == symbol, onClick = { op = symbol }, label = { Text(symbol, fontWeight = FontWeight.Bold) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c (Re₂)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("d (Im₂)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true, modifier = Modifier.weight(1f))
        }
        if (res != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AmberTertiary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("আয়তাকার (Rectangular): ${res.rectangularText}", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("পোলার (r∠θ): ${res.polarText}", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun Fx991CalculusPanel(angleUnit: AngleUnit) {
    var calcType by remember { mutableIntStateOf(0) } // 0 = integral, 1 = derivative
    var expr by remember { mutableStateOf("X^2 + 2*X") }
    var lowerOrX by remember { mutableStateOf("0") }
    var upper by remember { mutableStateOf("3") }

    val eval = remember(calcType, expr, lowerOrX, upper, angleUnit) {
        val v1 = lowerOrX.toDoubleOrNull()
        val v2 = upper.toDoubleOrNull()
        if (calcType == 1) {
            if (v1 == null) null else ScientificNumberCalculator.evaluateDerivative(expr, v1, angleUnit)
        } else {
            if (v1 == null || v2 == null) null else ScientificNumberCalculator.evaluateIntegral(expr, v1, v2, angleUnit)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "fx-991MS ক্যালকুলাস (∫dx ও d/dx)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = calcType == 0, onClick = { calcType = 0 }, label = { Text("∫ f(X) dx") })
            FilterChip(selected = calcType == 1, onClick = { calcType = 1 }, label = { Text("d/dx f(X)") })
        }
        OutlinedTextField(
            value = expr,
            onValueChange = { expr = it },
            label = { Text("f(X) ফাংশন (যেমন: X^2 + 2*X, sin(X))") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lowerOrX,
                onValueChange = { lowerOrX = it },
                label = { Text(if (calcType == 0) "নিম্ন সীমা (a)" else "X-এর মান") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            if (calcType == 0) {
                OutlinedTextField(
                    value = upper,
                    onValueChange = { upper = it },
                    label = { Text("উচ্চ সীমা (b)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (eval != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TealPrimary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (eval.success) {
                        if (calcType == 0) "∫($expr)dx = ${eval.formattedResult}"
                        else "d/dx($expr)|x=$lowerOrX = ${eval.formattedResult}"
                    } else {
                        eval.errorMessageBn ?: "হিসাব করা যায়নি"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (eval.success) TealPrimary else ErrorCrimson,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
