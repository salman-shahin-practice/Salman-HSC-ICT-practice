package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AlgoTopicCategory
import com.example.data.local.AlgorithmFlowchartData
import com.example.data.local.AlgorithmFlowchartTopic
import com.example.ui.components.FlowchartDiagramView
import com.example.ui.components.FlowchartNodeBadge
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BengaliFontFamily
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.InfoSky
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TealPrimary
import java.util.Locale
import kotlin.math.sqrt

@Composable
fun AlgorithmFlowchartScreen(
    onOpenInCLab: (code: String, stdin: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("অ্যালগরিদম ও ম্যাথ", "প্রতীক পরিচিতি", "ম্যাথ ক্যালকুলেটর")

    Column(modifier = modifier.fillMaxSize()) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("algo_back_btn")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "অ্যালগরিদম, ফ্লোচার্ট ও ম্যাথ",
                    fontFamily = BengaliFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "HSC ICT অধ্যায় ৫ • সূত্র, উদাহরণ, ধাপ ও ভিজ্যুয়াল ফ্লোচার্ট",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1) },
                    modifier = Modifier.testTag("algo_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> TopicsExplorerTab(onOpenInCLab = onOpenInCLab)
            1 -> SymbolsReferenceTab()
            2 -> InteractiveMathSolverTab()
        }
    }
}

@Composable
private fun TopicsExplorerTab(
    onOpenInCLab: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AlgoTopicCategory.ALL) }
    val filteredTopics = remember(selectedCategory) {
        if (selectedCategory == AlgoTopicCategory.ALL) {
            AlgorithmFlowchartData.topics
        } else {
            AlgorithmFlowchartData.topics.filter { it.category == selectedCategory }
        }
    }
    var expandedTopicId by remember(selectedCategory) {
        mutableStateOf(filteredTopics.firstOrNull()?.id)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlgoTopicCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.labelBn) },
                        modifier = Modifier.testTag("algo_cat_${cat.name}")
                    )
                }
            }
        }

        itemsIndexed(filteredTopics, key = { _, item -> item.id }) { index, topic ->
            val isExpanded = expandedTopicId == topic.id
            TopicOrganizedCard(
                index = index + 1,
                topic = topic,
                expanded = isExpanded,
                onToggleExpand = {
                    expandedTopicId = if (isExpanded) null else topic.id
                },
                onOpenInCLab = { onOpenInCLab(topic.cCode, topic.sampleStdin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            )
        }
    }
}

@Composable
private fun TopicOrganizedCard(
    index: Int,
    topic: AlgorithmFlowchartTopic,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenInCLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (topic.category) {
        AlgoTopicCategory.MATH_FORMULA -> TealPrimary
        AlgoTopicCategory.DECISION -> AmberTertiary
        AlgoTopicCategory.LOOP_SERIES -> EmeraldSecondary
        AlgoTopicCategory.ALL -> TealPrimary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (expanded) 1.5.dp else 1.dp,
            color = if (expanded) accentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = modifier.testTag("algo_topic_${topic.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Accordion Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f))
                ) {
                    Text(
                        text = index.toString().padStart(2, '0'),
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.titleBn,
                        fontFamily = BengaliFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${topic.category.labelBn} • ${topic.subtitleEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()

                    // 1. Formula Card
                    SectionBox(
                        icon = Icons.Rounded.Functions,
                        title = "১. ${topic.formulaTitleBn}",
                        accentColor = TealPrimary
                    ) {
                        Text(
                            text = topic.formulaText,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 2. Worked Math Example Card
                    SectionBox(
                        icon = Icons.Rounded.Calculate,
                        title = "২. ${topic.mathExampleTitleBn}",
                        accentColor = InfoSky
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            topic.mathStepsBn.forEach { step ->
                                Text(
                                    text = "• $step",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 3. Step-by-Step Algorithm Card
                    SectionBox(
                        icon = Icons.Rounded.ListAlt,
                        title = "৩. অ্যালগরিদম (Algorithm)",
                        accentColor = EmeraldSecondary
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            topic.algorithmStepsBn.forEach { step ->
                                Text(
                                    text = step,
                                    fontFamily = BengaliFontFamily,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // 4. Visual Flowchart Card
                    SectionBox(
                        icon = Icons.Rounded.AccountTree,
                        title = "৪. ফ্লোচার্ট (Flowchart Diagram)",
                        accentColor = AmberTertiary
                    ) {
                        FlowchartDiagramView(spec = topic.flowchart)
                    }

                    // 5. Matching C Code + Run in Lab
                    SectionBox(
                        icon = Icons.Rounded.Code,
                        title = "৫. C প্রোগ্রামিং কোড",
                        accentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = topic.cCode,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Button(
                                onClick = onOpenInCLab,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_in_c_lab_${topic.id}")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("C ল্যাবে কোডটি রান ও প্র্যাকটিস করো")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontFamily = BengaliFontFamily,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            content()
        }
    }
}

@Composable
private fun SymbolsReferenceTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "ফ্লোচার্ট আঁকার মৌলিক প্রতীক ও নিয়মাবলি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "• প্রতিটি ফ্লোচার্ট সবসময় উপর থেকে নিচে ও বাম থেকে ডানে প্রবাহিত হয়।\n• প্রবাহের দিক বোঝাতে অবশ্যই তীরচিহ্ন (Arrow) ব্যবহার করতে হবে।\n• সিদ্ধান্ত (Diamond) প্রতীক থেকে সবসময় দুটি পথ বের হয় (Yes/হ্যাঁ এবং No/না)।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        itemsIndexed(AlgorithmFlowchartData.symbols, key = { _, s -> s.id }) { idx, symbol ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. ${symbol.nameBn}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = symbol.nameEn,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    FlowchartNodeBadge(
                        shape = symbol.shape,
                        label = symbol.exampleText
                    )

                    Text(
                        text = symbol.purposeBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveMathSolverTab() {
    val modes = listOf(
        "ত্রিভুজ (হেরন)",
        "°C → °F তাপমাত্রা",
        "বৃত্তের ক্ষেত্রফল",
        "দ্বিঘাত সমীকরণ",
        "ধারা (1+2+...+n)",
        "ফ্যাক্টোরিয়াল (n!)"
    )
    var selectedMode by remember { mutableIntStateOf(0) }
    var valA by remember(selectedMode) {
        mutableStateOf(
            when (selectedMode) {
                0 -> "3"
                1 -> "37"
                2 -> "5"
                3 -> "1"
                4 -> "5"
                else -> "5"
            }
        )
    }
    var valB by remember(selectedMode) {
        mutableStateOf(
            when (selectedMode) {
                0 -> "4"
                3 -> "-5"
                else -> ""
            }
        )
    }
    var valC by remember(selectedMode) {
        mutableStateOf(
            when (selectedMode) {
                0 -> "5"
                3 -> "6"
                else -> ""
            }
        )
    }

    val calculationLines = remember(selectedMode, valA, valB, valC) {
        computeInteractiveMathSteps(selectedMode, valA, valB, valC)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedMode == idx,
                        onClick = { selectedMode = idx },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "মান বসিয়ে ধাপে ধাপে ম্যাথ দেখো: ${modes[selectedMode]}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val labelA = when (selectedMode) {
                            1 -> "সেলসিয়াস (C)"
                            2 -> "ব্যাসার্ধ (r)"
                            4, 5 -> "n-এর মান"
                            else -> "a"
                        }
                        OutlinedTextField(
                            value = valA,
                            onValueChange = { valA = it },
                            label = { Text(labelA) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedMode == 0 || selectedMode == 3) {
                            OutlinedTextField(
                                value = valB,
                                onValueChange = { valB = it },
                                label = { Text("b") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = valC,
                                onValueChange = { valC = it },
                                label = { Text("c") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider()

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
                            Text(
                                text = "ধাপে ধাপে ম্যাথ সমাধান:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            calculationLines.forEachIndexed { i, line ->
                                Text(
                                    text = "${i + 1}. $line",
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

private fun computeInteractiveMathSteps(
    mode: Int,
    rawA: String,
    rawB: String,
    rawC: String
): List<String> {
    val a = rawA.trim().toDoubleOrNull()
    val b = rawB.trim().toDoubleOrNull()
    val c = rawC.trim().toDoubleOrNull()
    fun fmt(v: Double): String = String.format(Locale.US, "%.2f", v)

    return when (mode) {
        0 -> {
            if (a == null || b == null || c == null || a <= 0 || b <= 0 || c <= 0) {
                return listOf("a, b, c এর ধনাত্মক সংখ্যা লিখুন (যেমন: 3, 4, 5)")
            }
            if (a + b <= c || b + c <= a || c + a <= b) {
                return listOf("শর্ত যাচাই: a+b>c, b+c>a, c+a>b মিথ্যা → এই ৩ বাহু দিয়ে ত্রিভুজ সম্ভব নয়!")
            }
            val s = (a + b + c) / 2.0
            val inner = s * (s - a) * (s - b) * (s - c)
            val area = sqrt(inner)
            listOf(
                "s = (${fmt(a)} + ${fmt(b)} + ${fmt(c)}) ÷ 2 = ${fmt(s)}",
                "s(s-a)(s-b)(s-c) = ${fmt(s)} × ${fmt(s - a)} × ${fmt(s - b)} × ${fmt(s - c)} = ${fmt(inner)}",
                "Area = √${fmt(inner)} = ${fmt(area)} বর্গ একক"
            )
        }
        1 -> {
            if (a == null) return listOf("সেলসিয়াস তাপমাত্রা C এর মান লিখুন")
            val f = (9.0 * a) / 5.0 + 32.0
            listOf(
                "সূত্র: F = (9 × C) ÷ 5 + 32",
                "F = (9 × ${fmt(a)}) ÷ 5 + 32 = ${fmt(9.0 * a / 5.0)} + 32",
                "ফলাফল: F = ${fmt(f)} °F"
            )
        }
        2 -> {
            if (a == null || a < 0) return listOf("ব্যাসার্ধ r এর মান লিখুন")
            val pi = 3.1416
            val area = pi * a * a
            val circ = 2 * pi * a
            listOf(
                "ব্যাসার্ধ r = ${fmt(a)}, π = 3.1416",
                "Area = 3.1416 × ${fmt(a)}² = ${fmt(area)}",
                "Circumference = 2 × 3.1416 × ${fmt(a)} = ${fmt(circ)}"
            )
        }
        3 -> {
            if (a == null || b == null || c == null || a == 0.0) {
                return listOf("সহগ a (শূন্য নয়), b, c এর মান লিখুন")
            }
            val d = b * b - 4 * a * c
            if (d < 0) {
                listOf(
                    "নিশ্চায়ক D = (${fmt(b)})² - 4(${fmt(a)})(${fmt(c)}) = ${fmt(d)}",
                    "যেহেতু D < 0, সমীকরণটির কোনো বাস্তব মূল নেই (Imaginary Roots)।"
                )
            } else {
                val x1 = (-b + sqrt(d)) / (2 * a)
                val x2 = (-b - sqrt(d)) / (2 * a)
                listOf(
                    "নিশ্চায়ক D = (${fmt(b)})² - 4(${fmt(a)})(${fmt(c)}) = ${fmt(d)}",
                    "x₁ = (-b + √D) ÷ 2a = ${fmt(x1)}",
                    "x₂ = (-b - √D) ÷ 2a = ${fmt(x2)}"
                )
            }
        }
        4 -> {
            val n = a?.toInt()?.coerceIn(1, 500) ?: return listOf("n এর পূর্ণসংখ্যা লিখুন (১-৫০০)")
            val sum = n * (n + 1) / 2
            val preview = (1..minOf(n, 5)).joinToString(" + ") + if (n > 5) " + ... + $n" else ""
            listOf(
                "ধারা: $preview",
                "সূত্র: Sum = n(n+1) ÷ 2 = $n × ${n + 1} ÷ 2",
                "ফলাফল: Sum = $sum"
            )
        }
        else -> {
            val n = a?.toInt()?.coerceIn(0, 15) ?: return listOf("n এর পূর্ণসংখ্যা লিখুন (০-১৫)")
            var fact = 1L
            for (i in 1..n) fact *= i
            val expr = if (n == 0) "0! = 1" else (1..n).joinToString(" × ")
            listOf(
                "গুণফল ধাপ: $expr",
                "ফলাফল: $n! = $fact"
            )
        }
    }
}
