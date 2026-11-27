package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CurriculumSeed
import com.example.data.model.SyncStatus
import com.example.data.model.TicketStatus
import com.example.ui.IctLabUiState
import com.example.ui.SupportPrefill
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreen

@Composable
fun SupportScreen(
    state: IctLabUiState,
    prefill: SupportPrefill,
    onSubmitTicket: (String, String, String, String, String, String?) -> Unit,
    onUpdateInstructorPhoto: (Uri) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("প্রশ্ন", "টিকিট (${state.tickets.size})", "সাধারণ প্রশ্ন", "পরিচিতি")

    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(prefill.category) }
    var message by remember { mutableStateOf("") }
    var codeSnippet by remember { mutableStateOf(prefill.codeSnippet) }
    var errorMessage by remember { mutableStateOf(prefill.errorMessage) }
    var attachmentUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(prefill) {
        if (prefill.codeSnippet.isNotBlank() || prefill.errorMessage.isNotBlank()) {
            category = prefill.category
            codeSnippet = prefill.codeSnippet
            errorMessage = prefill.errorMessage
            if (subject.isBlank()) subject = "${prefill.category} কোডে সমস্যা"
            selectedTab = 0
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        attachmentUri = uri?.toString()
    }

    var faqSearch by remember { mutableStateOf("") }
    val filteredFaqs = remember(faqSearch) {
        if (faqSearch.isBlank()) CurriculumSeed.offlineFaqs
        else CurriculumSeed.offlineFaqs.filter {
            it.questionBn.contains(faqSearch, ignoreCase = true) ||
                it.answerBn.contains(faqSearch, ignoreCase = true) ||
                it.category.contains(faqSearch, ignoreCase = true)
        }
    }

    val helplineNumber = stringResource(R.string.helpline_number)
    val helplineNumberEn = stringResource(R.string.helpline_number_en)
    val helplineTel = stringResource(R.string.helpline_tel)
    val whatsappUrl = stringResource(R.string.whatsapp_url)

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1) },
                    modifier = Modifier.testTag("support_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Direct WhatsApp & Helpline Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "জরুরি হেল্পলাইন ও মেন্টর সাপোর্ট",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "সরাসরি WhatsApp ও কল সহায়তা: $helplineNumberEn ($helplineNumber)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            val waIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(whatsappUrl)
                                            )
                                            runCatching { context.startActivity(waIntent) }
                                                .onFailure { onShowMessage("WhatsApp বা ব্রাউজার ওপেন করা যায়নি।") }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("whatsapp_helpline_btn")
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("WhatsApp")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse("tel:$helplineTel")
                                            )
                                            runCatching { context.startActivity(dialIntent) }
                                                .onFailure { onShowMessage("ডায়ালার ওপেন করা যায়নি।") }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("call_helpline_btn")
                                    ) {
                                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("কল করুন")
                                    }
                                }
                            }
                        }
                    }

                    // Doubt Ticket Form
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "সমস্যা বা প্রশ্ন পাঠান (অফলাইনেও জমা থাকবে)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("HTML", "C Programming", "Exam").forEach { cat ->
                                        FilterChip(
                                            selected = category == cat,
                                            onClick = { category = cat },
                                            label = { Text(cat) }
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = subject,
                                    onValueChange = { subject = it },
                                    label = { Text("সমস্যার শিরোনাম") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ticket_subject_input")
                                )

                                OutlinedTextField(
                                    value = message,
                                    onValueChange = { message = it },
                                    label = { Text("বিস্তারিত প্রশ্ন লিখুন") },
                                    minLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ticket_message_input")
                                )

                                OutlinedTextField(
                                    value = codeSnippet,
                                    onValueChange = { codeSnippet = it },
                                    label = { Text("কোড স্নিপেট (ঐচ্ছিক)") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = errorMessage,
                                    onValueChange = { errorMessage = it },
                                    label = { Text("এরর মেসেজ (ঐচ্ছিক)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.testTag("ticket_attach_photo_btn")
                                    ) {
                                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (attachmentUri == null) "স্ক্রিনশট যোগ" else "ছবি পরিবর্তিত")
                                    }
                                    if (attachmentUri != null) {
                                        AsyncImage(
                                            model = attachmentUri,
                                            contentDescription = "Attached error screenshot",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onSubmitTicket(
                                            subject,
                                            category,
                                            message,
                                            codeSnippet,
                                            errorMessage,
                                            attachmentUri
                                        )
                                        if (subject.isNotBlank() && message.isNotBlank()) {
                                            subject = ""
                                            message = ""
                                            codeSnippet = ""
                                            errorMessage = ""
                                            attachmentUri = null
                                            selectedTab = 1
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ticket_submit_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("প্রশ্ন জমা দিন")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (state.tickets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("এখনও কোনো সাপোর্ট টিকিট নেই", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "‘প্রশ্ন’ ট্যাবে গিয়ে HTML বা C প্রোগ্রামিংয়ের যেকোনো সমস্যা পাঠাতে পারেন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.tickets, key = { it.id }) { ticket ->
                            val statusEnum = TicketStatus.entries.firstOrNull { it.name == ticket.status } ?: TicketStatus.PENDING
                            val synced = ticket.syncStatus == SyncStatus.SYNCED.name
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ticket.subject,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (statusEnum) {
                                                TicketStatus.PENDING -> AmberTertiary.copy(alpha = 0.16f)
                                                TicketStatus.ANSWERED -> MaterialTheme.colorScheme.primaryContainer
                                                TicketStatus.RESOLVED -> SuccessGreen.copy(alpha = 0.16f)
                                            }
                                        ) {
                                            Text(
                                                text = statusEnum.labelBn,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Text(text = ticket.message, style = MaterialTheme.typography.bodyMedium)

                                    if (ticket.codeSnippet.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = ticket.codeSnippet.lines().take(4).joinToString("\n"),
                                                fontFamily = JetBrainsMonoFontFamily,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                        }
                                    }

                                    if (ticket.adminReply.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "মেন্টরের উত্তর:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                                Text(
                                                    text = ticket.adminReply,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (synced) Icons.Rounded.CloudDone else Icons.Rounded.CloudQueue,
                                            contentDescription = null,
                                            tint = if (synced) SuccessGreen else AmberTertiary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (synced) "সংরক্ষিত ও সিঙ্ক সম্পন্ন" else "অফলাইনে জমা আছে (ইন্টারনেট পেলে সিঙ্ক হবে)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = faqSearch,
                            onValueChange = { faqSearch = it },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            label = { Text("সাধারণ প্রশ্ন ও সমাধান খুঁজুন") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("faq_search_input")
                        )
                    }
                    items(filteredFaqs, key = { it.id }) { faq ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = faq.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = faq.questionBn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = faq.answerBn,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (faq.codeExample != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = faq.codeExample,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        InstructorProfileCard(
                            customPhotoPath = state.settings.instructorPhotoPath,
                            photoVersion = state.settings.instructorPhotoVersion,
                            onUpdatePhoto = onUpdateInstructorPhoto,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
