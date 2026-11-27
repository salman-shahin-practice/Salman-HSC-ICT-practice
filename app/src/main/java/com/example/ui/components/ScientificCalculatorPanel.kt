package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary

private data class CalcHistoryEntry(
    val expression: String,
    val result: String
)

@Composable
fun ScientificCalculatorSection(
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf("sin(30) + 2^3") }
    var angleUnit by remember { mutableStateOf(AngleUnit.DEG) }
    var secondMode by remember { mutableStateOf(false) }
    var lastAnswer by remember { mutableStateOf(8.5) }
    var evaluatedResult by remember { mutableStateOf("8.5") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val history = remember { mutableStateListOf<CalcHistoryEntry>() }

    val liveEval = remember(expression, angleUnit, lastAnswer) {
        if (expression.isBlank()) null
        else ScientificNumberCalculator.evaluateScientificExpression(expression, angleUnit, lastAnswer)
    }

    fun evaluateNow() {
        val res = ScientificNumberCalculator.evaluateScientificExpression(expression, angleUnit, lastAnswer)
        if (res.success && res.numericValue != null) {
            evaluatedResult = res.formattedResult
            lastAnswer = res.numericValue
            errorMessage = null
            val entry = CalcHistoryEntry(expression, res.formattedResult)
            history.removeAll { it.expression == entry.expression && it.result == entry.result }
            history.add(0, entry)
            if (history.size > 6) history.removeAt(history.lastIndex)
        } else {
            errorMessage = res.errorMessageBn ?: "সমীকরণ সঠিক নয়"
        }
    }

    fun appendToken(token: String) {
        errorMessage = null
        expression += token
    }

    fun backspaceToken() {
        errorMessage = null
        if (expression.isEmpty()) return
        val multiTokens = listOf("asin(", "acos(", "atan(", "sqrt(", "cbrt(", "sin(", "cos(", "tan(", "log(", "ln(", "ANS")
        val matched = multiTokens.firstOrNull { expression.endsWith(it) }
        expression = if (matched != null) {
            expression.dropLast(matched.length)
        } else {
            expression.dropLast(1)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LCD Display Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scientific_calc_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = angleUnit == AngleUnit.DEG,
                            onClick = {
                                angleUnit = if (angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
                            },
                            label = { Text(angleUnit.label, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("sci_angle_toggle")
                        )
                        FilterChip(
                            selected = secondMode,
                            onClick = { secondMode = !secondMode },
                            label = { Text(if (secondMode) "2nd ON" else "2nd") },
                            modifier = Modifier.testTag("sci_second_toggle")
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TealPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "ANS = ${ScientificNumberCalculator.formatDoubleClean(lastAnswer)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            modifier = Modifier
                                .clickable { appendToken("ANS") }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                // Expression & Result Screen
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = expression.ifEmpty { "0" },
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFCBD5E1),
                                maxLines = 1,
                                modifier = Modifier.testTag("sci_expression_text")
                            )
                        }

                        val displayOut = when {
                            errorMessage != null -> errorMessage!!
                            liveEval?.success == true -> "= ${liveEval.formattedResult}"
                            else -> "= $evaluatedResult"
                        }
                        Text(
                            text = displayOut,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = if (errorMessage != null) {
                                MaterialTheme.typography.bodyMedium
                            } else {
                                MaterialTheme.typography.headlineSmall
                            },
                            fontWeight = FontWeight.Bold,
                            color = if (errorMessage != null) Color(0xFFFCA5A5) else Color(0xFF2DD4BF),
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sci_result_text")
                        )

                        // Live BIN/OCT/HEX preview if expression evaluates to an integer
                        val intConv = liveEval?.integerBaseSummary
                        if (intConv != null && intConv.valid) {
                            HorizontalDivider(color = Color(0xFF334155))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BaseMiniTag("BIN", intConv.binary, Color(0xFF38BDF8))
                                BaseMiniTag("OCT", intConv.octal, Color(0xFFFBBF24))
                                BaseMiniTag("HEX", intConv.hex, Color(0xFF34D399))
                            }
                        }
                    }
                }

                // Scientific Function Rows
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SciKey(
                            label = if (secondMode) "sin⁻¹" else "sin",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken(if (secondMode) "asin(" else "sin(") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = if (secondMode) "cos⁻¹" else "cos",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken(if (secondMode) "acos(" else "cos(") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = if (secondMode) "tan⁻¹" else "tan",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken(if (secondMode) "atan(" else "tan(") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = if (secondMode) "10ˣ" else "log",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken(if (secondMode) "10^" else "log(") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = if (secondMode) "eˣ" else "ln",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken(if (secondMode) "exp(" else "ln(") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SciKey(
                            label = if (secondMode) "∛" else "√",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken(if (secondMode) "cbrt(" else "sqrt(") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = "x²",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken("^2") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = "xʸ",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken("^") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = "n!",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken("!") },
                            modifier = Modifier.weight(1f)
                        )
                        SciKey(
                            label = "|x|",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            onClick = { appendToken("abs(") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SciKey("(", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, { appendToken("(") }, Modifier.weight(1f))
                        SciKey(")", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, { appendToken(")") }, Modifier.weight(1f))
                        SciKey("π", MaterialTheme.colorScheme.surfaceVariant, AmberTertiary, { appendToken("π") }, Modifier.weight(1f))
                        SciKey("e", MaterialTheme.colorScheme.surfaceVariant, AmberTertiary, { appendToken("e") }, Modifier.weight(1f))
                        SciKey("mod", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, { appendToken(" mod ") }, Modifier.weight(1f))
                    }

                    // Main Numeric Keypad
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciKey("7", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("7") }, Modifier.weight(1f), bordered = true)
                        SciKey("8", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("8") }, Modifier.weight(1f), bordered = true)
                        SciKey("9", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("9") }, Modifier.weight(1f), bordered = true)
                        SciKey("DEL", AmberTertiary.copy(alpha = 0.16f), AmberTertiary, { backspaceToken() }, Modifier.weight(1f))
                        SciKey("AC", ErrorCrimson.copy(alpha = 0.15f), ErrorCrimson, {
                            expression = ""
                            evaluatedResult = "0"
                            errorMessage = null
                        }, Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciKey("4", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("4") }, Modifier.weight(1f), bordered = true)
                        SciKey("5", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("5") }, Modifier.weight(1f), bordered = true)
                        SciKey("6", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("6") }, Modifier.weight(1f), bordered = true)
                        SciKey("×", TealPrimary.copy(alpha = 0.14f), TealPrimary, { appendToken("×") }, Modifier.weight(1f))
                        SciKey("÷", TealPrimary.copy(alpha = 0.14f), TealPrimary, { appendToken("÷") }, Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciKey("1", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("1") }, Modifier.weight(1f), bordered = true)
                        SciKey("2", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("2") }, Modifier.weight(1f), bordered = true)
                        SciKey("3", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("3") }, Modifier.weight(1f), bordered = true)
                        SciKey("+", TealPrimary.copy(alpha = 0.14f), TealPrimary, { appendToken("+") }, Modifier.weight(1f))
                        SciKey("−", TealPrimary.copy(alpha = 0.14f), TealPrimary, { appendToken("-") }, Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciKey("0", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken("0") }, Modifier.weight(1f), bordered = true)
                        SciKey(".", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, { appendToken(".") }, Modifier.weight(1f), bordered = true)
                        SciKey("%", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, { appendToken("%") }, Modifier.weight(1f))
                        SciKey("ANS", EmeraldSecondary.copy(alpha = 0.15f), EmeraldSecondary, { appendToken("ANS") }, Modifier.weight(1f))
                        SciKey(
                            label = "=",
                            containerColor = TealPrimary,
                            contentColor = Color.White,
                            onClick = { evaluateNow() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sci_equals_btn")
                        )
                    }
                }
            }
        }

        // Calculation History
        if (history.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.History, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "সাম্প্রতিক হিসাব (ট্যাপ করলে পুনরায় লোড হবে)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    history.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expression = item.expression }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.expression,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "= ${item.result}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberSystemCalculatorSection(
    modifier: Modifier = Modifier
) {
    var subMode by remember { mutableIntStateOf(0) } // 0 = Converter, 1 = Arithmetic & Bitwise, 2 = 2's Complement

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = subMode == 0,
                onClick = { subMode = 0 },
                label = { Text("রূপান্তর (BIN/OCT/DEC/HEX)") },
                modifier = Modifier.testTag("ns_mode_convert")
            )
            FilterChip(
                selected = subMode == 1,
                onClick = { subMode = 1 },
                label = { Text("যোগ-বিয়োগ ও বিটওয়াইজ") },
                modifier = Modifier.testTag("ns_mode_arith")
            )
            FilterChip(
                selected = subMode == 2,
                onClick = { subMode = 2 },
                label = { Text("২-এর পরিপূরক (2's Comp)") },
                modifier = Modifier.testTag("ns_mode_twos")
            )
        }

        when (subMode) {
            0 -> NumberBaseConverterCard()
            1 -> NumberBaseArithmeticCard()
            else -> TwosComplementCard()
        }
    }
}

@Composable
private fun NumberBaseConverterCard() {
    var fromBase by remember { mutableStateOf(NumberBase.DEC) }
    var targetExplainBase by remember { mutableStateOf(NumberBase.BIN) }
    var inputText by remember { mutableStateOf("25.625") }
    val clipboard = LocalClipboardManager.current

    val conv = remember(inputText, fromBase, targetExplainBase) {
        ScientificNumberCalculator.convertNumberBase(inputText, fromBase, targetExplainBase)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ns_converter_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "সংখ্যা পদ্ধতি রূপান্তর (পূর্ণাংশ ও ভগ্নাংশ সহ)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Input Base selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumberBase.entries.forEach { base ->
                    FilterChip(
                        selected = fromBase == base,
                        onClick = {
                            if (conv.valid) {
                                inputText = conv.inBase(base)
                            }
                            fromBase = base
                            if (targetExplainBase == base) {
                                targetExplainBase = if (base == NumberBase.DEC) NumberBase.BIN else NumberBase.DEC
                            }
                        },
                        label = { Text("${base.shortLabel} (${base.radix})") },
                        modifier = Modifier.testTag("ns_from_${base.shortLabel}")
                    )
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it.uppercase() },
                label = { Text("${fromBase.titleBn} সংখ্যা লিখুন (যেমন: 25.625 বা 101.1)") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii
                ),
                singleLine = true,
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { inputText = inputText.dropLast(1) }) {
                            Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ns_input_field")
            )

            // Base-aware Hex/Bin/Oct/Dec keypad
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val hexKeys = listOf("A", "B", "C", "D", "E", "F")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    hexKeys.forEach { ch ->
                        val enabled = fromBase == NumberBase.HEX
                        BaseKeypadButton(
                            label = ch,
                            enabled = enabled,
                            accent = AmberTertiary,
                            onClick = { inputText += ch },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                val row1 = listOf("7", "8", "9", "4", "5", "6")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    row1.forEach { ch ->
                        val digit = ch.toInt()
                        BaseKeypadButton(
                            label = ch,
                            enabled = digit < fromBase.radix,
                            accent = TealPrimary,
                            onClick = { inputText += ch },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("1", "2", "3", "0", ".").forEach { ch ->
                        val enabled = ch == "." || ch.toInt() < fromBase.radix
                        BaseKeypadButton(
                            label = ch,
                            enabled = enabled,
                            accent = TealPrimary,
                            onClick = {
                                if (ch != "." || !inputText.contains(".")) inputText += ch
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BaseKeypadButton(
                        label = "AC",
                        enabled = true,
                        accent = ErrorCrimson,
                        onClick = { inputText = "" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!conv.valid) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorCrimson.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = conv.errorMessageBn ?: "অবৈধ সংখ্যা",
                        color = ErrorCrimson,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                // Live 4-base outputs
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberBase.entries.forEach { base ->
                        val valueStr = conv.inBase(base)
                        val accent = when (base) {
                            NumberBase.BIN -> InfoSky
                            NumberBase.OCT -> AmberTertiary
                            NumberBase.DEC -> TealPrimary
                            NumberBase.HEX -> EmeraldSecondary
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accent.copy(alpha = 0.08f),
                            border = BorderStroke(
                                width = if (targetExplainBase == base) 1.5.dp else 1.dp,
                                color = accent.copy(alpha = if (targetExplainBase == base) 0.7f else 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetExplainBase = base }
                                .testTag("ns_output_${base.shortLabel}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = base.titleBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = valueStr,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(valueStr)) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy ${base.shortLabel}",
                                        modifier = Modifier.size(18.dp),
                                        tint = accent
                                    )
                                }
                            }
                        }
                    }
                }

                // Step-by-step Board Explanation
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TealPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "ধাপে ধাপে বোর্ড সমাধান (উপরের যেকোনো কার্ডে ট্যাপ করো):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                        conv.stepsBn.forEachIndexed { idx, step ->
                            Text(
                                text = "${idx + 1}. $step",
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

@Composable
private fun NumberBaseArithmeticCard() {
    var base by remember { mutableStateOf(NumberBase.BIN) }
    var leftVal by remember { mutableStateOf("1011") }
    var rightVal by remember { mutableStateOf("1101") }
    var operation by remember { mutableStateOf(BaseOperation.ADD) }

    val result = remember(leftVal, rightVal, base, operation) {
        ScientificNumberCalculator.evaluateBaseArithmetic(leftVal, rightVal, base, operation)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ns_arith_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "বাইনারি, অক্টাল, হেক্সা যোগ-বিয়োগ ও বিটওয়াইজ ম্যাথ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Quick presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { base = NumberBase.BIN; leftVal = "1011"; rightVal = "1101"; operation = BaseOperation.ADD },
                    label = { Text("1011 + 1101 (BIN)") }
                )
                FilterChip(
                    selected = false,
                    onClick = { base = NumberBase.OCT; leftVal = "75"; rightVal = "26"; operation = BaseOperation.ADD },
                    label = { Text("75 + 26 (OCT)") }
                )
                FilterChip(
                    selected = false,
                    onClick = { base = NumberBase.HEX; leftVal = "2A"; rightVal = "1F"; operation = BaseOperation.ADD },
                    label = { Text("2A + 1F (HEX)") }
                )
                FilterChip(
                    selected = false,
                    onClick = { base = NumberBase.BIN; leftVal = "1101"; rightVal = "1011"; operation = BaseOperation.XOR },
                    label = { Text("1101 XOR 1011") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumberBase.entries.forEach { b ->
                    FilterChip(
                        selected = base == b,
                        onClick = { base = b },
                        label = { Text(b.shortLabel) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = leftVal,
                    onValueChange = { leftVal = it.uppercase() },
                    label = { Text("প্রথম (${base.shortLabel})") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ns_arith_left")
                )
                OutlinedTextField(
                    value = rightVal,
                    onValueChange = { rightVal = it.uppercase() },
                    label = { Text("দ্বিতীয় (${base.shortLabel})") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ns_arith_right")
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BaseOperation.entries.forEach { op ->
                    FilterChip(
                        selected = operation == op,
                        onClick = { operation = op },
                        label = { Text(op.symbol, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("ns_op_${op.name}")
                    )
                }
            }

            if (!result.valid) {
                Text(
                    text = result.errorMessageBn ?: "অবৈধ ইনপুট",
                    color = ErrorCrimson,
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (result.conversion != null) {
                val conv = result.conversion
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldSecondary.copy(alpha = 0.09f),
                    border = BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ফলাফল (${base.shortLabel}): ${conv.inBase(base)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSecondary,
                            modifier = Modifier.testTag("ns_arith_result")
                        )
                        Text(
                            text = "BIN: ${conv.binary}  •  OCT: ${conv.octal}  •  DEC: ${conv.decimal}  •  HEX: ${conv.hex}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelMedium
                        )
                        HorizontalDivider(color = EmeraldSecondary.copy(alpha = 0.25f))
                        result.stepsBn.forEachIndexed { idx, line ->
                            Text(
                                text = "${idx + 1}. $line",
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

@Composable
private fun TwosComplementCard() {
    var singleMode by remember { mutableStateOf(false) }
    var bitWidth by remember { mutableIntStateOf(8) }
    var isSubtract by remember { mutableStateOf(true) }
    var valA by remember { mutableStateOf("25") }
    var valB by remember { mutableStateOf("14") }

    val parsedA = valA.trim().toIntOrNull()
    val parsedB = if (singleMode) null else valB.trim().toIntOrNull()
    val result = remember(parsedA, parsedB, isSubtract, bitWidth, singleMode) {
        when {
            parsedA == null -> null
            !singleMode && parsedB == null -> null
            else -> ScientificNumberCalculator.computeTwosComplement(
                valueA = parsedA,
                valueB = parsedB,
                isSubtract = isSubtract,
                bitWidth = bitWidth
            )
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ns_twos_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "২-এর পরিপূরক (2's Complement) ও সাইনড বাইনারি",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !singleMode && isSubtract,
                    onClick = { singleMode = false; isSubtract = true },
                    label = { Text("A − B (বিয়োগ)") }
                )
                FilterChip(
                    selected = !singleMode && !isSubtract,
                    onClick = { singleMode = false; isSubtract = false },
                    label = { Text("A + B (যোগ)") }
                )
                FilterChip(
                    selected = singleMode,
                    onClick = { singleMode = true },
                    label = { Text("একক সংখ্যার মান") }
                )
                FilterChip(
                    selected = bitWidth == 8,
                    onClick = { bitWidth = 8 },
                    label = { Text("8-bit") }
                )
                FilterChip(
                    selected = bitWidth == 16,
                    onClick = { bitWidth = 16 },
                    label = { Text("16-bit") }
                )
            }

            // Board presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { singleMode = false; isSubtract = true; valA = "25"; valB = "14"; bitWidth = 8 },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) { Text("25 − 14", style = MaterialTheme.typography.labelSmall) }
                Button(
                    onClick = { singleMode = false; isSubtract = true; valA = "14"; valB = "25"; bitWidth = 8 },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) { Text("14 − 25", style = MaterialTheme.typography.labelSmall) }
                Button(
                    onClick = { singleMode = true; valA = "-25"; bitWidth = 8 },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) { Text("-25 গঠন", style = MaterialTheme.typography.labelSmall) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = valA,
                    onValueChange = { valA = it },
                    label = { Text(if (singleMode) "ডেসিমাল সংখ্যা" else "A (ডেসিমাল)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    trailingIcon = {
                        IconButton(onClick = {
                            valA = if (valA.startsWith("-")) valA.drop(1) else "-$valA"
                        }) {
                            Text("±", fontWeight = FontWeight.Bold)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                if (!singleMode) {
                    OutlinedTextField(
                        value = valB,
                        onValueChange = { valB = it },
                        label = { Text("B (ডেসিমাল)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        trailingIcon = {
                            IconButton(onClick = {
                                valB = if (valB.startsWith("-")) valB.drop(1) else "-$valB"
                            }) {
                                Text("±", fontWeight = FontWeight.Bold)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (result == null) {
                Text(
                    text = "পূর্ণসংখ্যা (যেমন 25, -14) লিখুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!result.valid) {
                Text(
                    text = result.errorMessageBn ?: "রেঞ্জের বাইরে",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorCrimson
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AmberTertiary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Calculate, contentDescription = null, tint = AmberTertiary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "ফলাফল (${result.bitWidth}-bit): ${result.binaryResult}₂ = ${result.decimalResult}₁₀",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = AmberTertiary
                            )
                        }
                        if (singleMode) {
                            Text(
                                text = "প্রকৃত মান: ${result.signMagnitude} | ১-এর পরিপূরক: ${result.onesComplement} | ২-এর পরিপূরক: ${result.twosComplement}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        HorizontalDivider(color = AmberTertiary.copy(alpha = 0.25f))
                        result.stepsBn.forEachIndexed { idx, line ->
                            Text(
                                text = "${idx + 1}. $line",
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

@Composable
private fun BaseMiniTag(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFE2E8F0)
        )
    }
}

@Composable
private fun SciKey(
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bordered: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (bordered) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)) else null,
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = if (label.length > 3) 12.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BaseKeypadButton(
    label: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (enabled) accent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        modifier = modifier
            .height(48.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}
