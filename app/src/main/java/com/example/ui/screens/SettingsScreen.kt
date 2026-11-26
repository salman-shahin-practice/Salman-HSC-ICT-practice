package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EditorThemeMode
import com.example.ui.IctLabUiState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: IctLabUiState,
    onSetDarkMode: (Boolean) -> Unit,
    onSetFontSize: (Int) -> Unit,
    onSetEditorTheme: (EditorThemeMode) -> Unit,
    onSetStudentName: (String) -> Unit,
    onSetAutoSync: (Boolean) -> Unit,
    onManualSync: () -> Unit,
    onExportBackupJson: suspend () -> String,
    onImportBackupJson: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nameDraft by remember(state.settings.studentName) { mutableStateOf(state.settings.studentName) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = onExportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                }.onSuccess {
                    onShowMessage("ব্যাকআপ সফলভাবে সংরক্ষণ করা হয়েছে!")
                }.onFailure {
                    onShowMessage("ব্যাকআপ সংরক্ষণে সমস্যা হয়েছে।")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: ""
                }.onSuccess { text ->
                    if (text.isNotBlank()) onImportBackupJson(text)
                }.onFailure {
                    onShowMessage("ব্যাকআপ ফাইল পড়া যায়নি।")
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "সেটিংস ও ব্যাকআপ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Student Profile Name
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("শিক্ষার্থীর প্রোফাইল", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("তোমার নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { onSetStudentName(nameDraft) }) {
                        Text("নাম সেভ করো")
                    }
                }
            }
        }

        // Appearance & Code Editor Settings
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("থিম ও কোড এডিটর সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ডার্ক মোড (Dark Theme)", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.settings.isDarkMode,
                            onCheckedChange = onSetDarkMode,
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }

                    Text("কোড এডিটর থিম:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditorThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.settings.editorTheme == mode,
                                onClick = { onSetEditorTheme(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Text(
                        text = "কোড ফন্ট সাইজ: ${state.settings.editorFontSizeSp}sp",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = state.settings.editorFontSizeSp.toFloat(),
                        onValueChange = { onSetFontSize(it.toInt()) },
                        valueRange = 12f..20f,
                        steps = 7
                    )
                }
            }
        }

        // Hybrid Sync & JSON Backup/Restore
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("অফলাইন/অনলাইন সিঙ্ক ও ব্যাকআপ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("অনলাইনে স্বয়ংক্রিয় স্মার্ট সিঙ্ক", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.settings.autoSyncEnabled,
                            onCheckedChange = onSetAutoSync
                        )
                    }

                    OutlinedButton(
                        onClick = onManualSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("এখনই সিঙ্ক করো (${state.queuedSyncCount} পেন্ডিং)")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { exportLauncher.launch("hsc_ict_backup.json") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_btn")
                        ) {
                            Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Backup")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_backup_btn")
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restore")
                        }
                    }
                }
            }
        }
    }
}
