package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.CurriculumSeed
import com.example.data.model.PracticeProblemEntity
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.ui.IctLabUiState
import com.example.ui.components.TopicMasteryBar
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import java.util.UUID

@Composable
fun AdminPanelScreen(
    state: IctLabUiState,
    onSetupAdminPin: (String, (Boolean) -> Unit) -> Unit,
    onUnlockAdmin: (String, (Boolean) -> Unit) -> Unit,
    onSwitchToStudent: () -> Unit,
    onSaveProblem: (PracticeProblemEntity) -> Unit,
    onDeleteProblem: (String) -> Unit,
    onReplyTicket: (String, String, TicketStatus) -> Unit,
    onPublishNotice: (String, String, String, Boolean) -> Unit,
    onDeleteNotice: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    if (state.settings.activeRole != UserRole.ADMIN) {
        AdminPinGate(
            hasPinConfigured = state.settings.hasAdminPinConfigured,
            onSetupPin = onSetupAdminPin,
            onUnlockPin = onUnlockAdmin,
            onBack = onBack,
            modifier = modifier
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("কনটেন্ট", "ব্যাচ মনিটরিং", "সাপোর্ট ও নোটিশ")

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "অ্যাডমিন প্যানেল",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                onClick = onSwitchToStudent,
                modifier = Modifier.testTag("switch_to_student_btn")
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Student মোড")
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1) }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminContentManagerTab(
                problems = state.problems,
                onSaveProblem = onSaveProblem,
                onDeleteProblem = onDeleteProblem
            )
            1 -> AdminAnalyticsTab(state = state)
            2 -> AdminSupportAndNoticesTab(
                state = state,
                onReplyTicket = onReplyTicket,
                onPublishNotice = onPublishNotice,
                onDeleteNotice = onDeleteNotice
            )
        }
    }
}

@Composable
private fun AdminPinGate(
    hasPinConfigured: Boolean,
    onSetupPin: (String, (Boolean) -> Unit) -> Unit,
    onUnlockPin: (String, (Boolean) -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.AdminPanelSettings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = if (hasPinConfigured) "অ্যাডমিন প্যানেল আনলক করুন" else "প্রথমবার অ্যাডমিন পিন সেট করুন",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = if (hasPinConfigured) {
                "কনটেন্ট ম্যানেজমেন্ট ও সাপোর্ট ডেস্ক অ্যাক্সেস করতে আপনার অ্যাডমিন পিন দিন।"
            } else {
                "কমপক্ষে ৪ সংখ্যার একটি নিরাপদ অ্যাডমিন পিন সেট করে শিক্ষকের ড্যাশবোর্ড আনলক করুন।"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it; errorMsg = null },
            label = { Text("অ্যাডমিন পিন (কমপক্ষে ৪ অক্ষর)") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_pin_input")
        )
        if (errorMsg != null) {
            Spacer(Modifier.size(6.dp))
            Text(text = errorMsg!!, color = ErrorCrimson, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("ফিরে যান")
            }
            Button(
                onClick = {
                    if (hasPinConfigured) {
                        onUnlockPin(pin) { ok ->
                            if (!ok) errorMsg = "ভুল পিন দেওয়া হয়েছে! আবার চেষ্টা করুন।"
                        }
                    } else {
                        onSetupPin(pin) { ok ->
                            if (!ok) errorMsg = "কমপক্ষে ৪ অক্ষরের পিন দিন।"
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_unlock_btn")
            ) {
                Text(if (hasPinConfigured) "আনলক" else "সেট ও আনলক")
            }
        }
    }
}

@Composable
private fun AdminContentManagerTab(
    problems: List<PracticeProblemEntity>,
    onSaveProblem: (PracticeProblemEntity) -> Unit,
    onDeleteProblem: (String) -> Unit
) {
    var lang by remember { mutableStateOf("C") }
    var titleBn by remember { mutableStateOf("") }
    var moduleBn by remember { mutableStateOf("নতুন অনুশীলন") }
    var instructionsBn by remember { mutableStateOf("") }
    var starterCode by remember { mutableStateOf("") }
    var expectedOutput by remember { mutableStateOf("") }
    var hintBn by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    Text(
                        text = "নতুন অনুশীলন সমস্যা যোগ করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HTML", "C").forEach { l ->
                            FilterChip(
                                selected = lang == l,
                                onClick = { lang = l },
                                label = { Text(l) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = titleBn,
                        onValueChange = { titleBn = it },
                        label = { Text("সমস্যার শিরোনাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = moduleBn,
                        onValueChange = { moduleBn = it },
                        label = { Text("মডিউল নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instructionsBn,
                        onValueChange = { instructionsBn = it },
                        label = { Text("নির্দেশনা") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = starterCode,
                        onValueChange = { starterCode = it },
                        label = { Text("স্টার্টার কোড") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expectedOutput,
                        onValueChange = { expectedOutput = it },
                        label = { Text("প্রত্যাশিত আউটপুট বা ট্যাগ (কমা দিয়ে)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hintBn,
                        onValueChange = { hintBn = it },
                        label = { Text("হিন্ট ও বোর্ড টিপস") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (titleBn.isNotBlank() && instructionsBn.isNotBlank()) {
                                val fallbackCode = starterCode.ifBlank {
                                    if (lang == "HTML") "<!DOCTYPE html>\n<html>\n<body>\n  <h1>Hello</h1>\n</body>\n</html>"
                                    else "#include <stdio.h>\nint main() {\n    return 0;\n}"
                                }
                                onSaveProblem(
                                    PracticeProblemEntity(
                                        id = "custom_${UUID.randomUUID().toString().take(8)}",
                                        language = lang,
                                        moduleKey = "custom_${lang.lowercase()}",
                                        moduleTitleBn = moduleBn.ifBlank { "অ্যাডমিন লেসন" },
                                        moduleTitleEn = "Custom",
                                        titleBn = titleBn.trim(),
                                        titleEn = titleBn.trim(),
                                        difficulty = "Board",
                                        instructionsBn = instructionsBn.trim(),
                                        starterCode = fallbackCode,
                                        solutionCode = fallbackCode,
                                        hintBn = hintBn.ifBlank { "ধাপে ধাপে সমাধান করো।" },
                                        commonMistakeBn = "সেমিকোলন ও বন্ধনী সঠিকভাবে যাচাই করো।",
                                        expectedOutput = if (lang == "C") expectedOutput.trim() else "",
                                        requiredTagsCsv = if (lang == "HTML") expectedOutput.trim() else "",
                                        isCustom = true,
                                        sortOrder = 99
                                    )
                                )
                                titleBn = ""
                                instructionsBn = ""
                                starterCode = ""
                                expectedOutput = ""
                                hintBn = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("সমস্যা প্রকাশ করুন")
                    }
                }
            }
        }

        items(problems, key = { it.id }) { prob ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${prob.language} • ${prob.titleBn}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = prob.moduleTitleBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteProblem(prob.id) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete problem")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAnalyticsTab(state: IctLabUiState) {
    val batch = CurriculumSeed.sampleBatchSummaries
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ব্যাচ সামগ্রিক অগ্রগতি (HSC-2026)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "মোট প্রশ্ন: ${state.problems.size} • খোলা সাপোর্ট টিকিট: ${state.tickets.count { it.status == TicketStatus.PENDING.name }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TopicMasteryBar("ব্যাচ HTML গড় দক্ষতা", 81, 100, TealPrimary)
                    TopicMasteryBar("ব্যাচ C প্রোগ্রামিং গড় দক্ষতা", 67, 100, AmberTertiary)
                }
            }
        }

        items(batch) { student ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(student.lastActive, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(student.batch, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("HTML: ${student.htmlPercent}% • C: ${student.cPercent}%", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "দুর্বল টপিক: ${student.weakTopic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorCrimson
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSupportAndNoticesTab(
    state: IctLabUiState,
    onReplyTicket: (String, String, TicketStatus) -> Unit,
    onPublishNotice: (String, String, String, Boolean) -> Unit,
    onDeleteNotice: (String) -> Unit
) {
    var noticeTitle by remember { mutableStateOf("") }
    var noticeBody by remember { mutableStateOf("") }
    var noticePinned by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    Text(
                        text = "নতুন নোটিশ / ঘোষণা প্রকাশ করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("নোটিশের শিরোনাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text("বিস্তারিত বার্তা") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = noticePinned, onCheckedChange = { noticePinned = it })
                        Text("হোম স্ক্রিনে পিন করে রাখুন", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = {
                            if (noticeTitle.isNotBlank() && noticeBody.isNotBlank()) {
                                onPublishNotice(noticeTitle, noticeBody, "Notice", noticePinned)
                                noticeTitle = ""
                                noticeBody = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("নোটিশ প্রকাশ করুন")
                    }
                }
            }
        }

        items(state.notices, key = { it.id }) { notice ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(notice.body, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteNotice(notice.id) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete notice")
                    }
                }
            }
        }

        item {
            Text(
                text = "শিক্ষার্থীদের সাপোর্ট টিকিট (${state.tickets.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.tickets, key = { it.id }) { ticket ->
            var replyDraft by remember(ticket.id) { mutableStateOf(ticket.adminReply) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${ticket.studentName} • ${ticket.subject}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = ticket.message, style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = replyDraft,
                        onValueChange = { replyDraft = it },
                        label = { Text("অ্যাডমিনের উত্তর লিখুন") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (replyDraft.isNotBlank()) {
                                    onReplyTicket(ticket.id, replyDraft, TicketStatus.ANSWERED)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("উত্তর পাঠান")
                        }
                        OutlinedButton(
                            onClick = {
                                onReplyTicket(ticket.id, replyDraft.ifBlank { "সমাধান সম্পন্ন হয়েছে।" }, TicketStatus.RESOLVED)
                            }
                        ) {
                            Text("Resolved", color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }
}
