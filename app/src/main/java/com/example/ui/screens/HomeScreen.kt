package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.IctLabUiState
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BengaliFontFamily
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import java.io.File

@Composable
fun HomeScreen(
    state: IctLabUiState,
    isSyncing: Boolean,
    onManualSync: () -> Unit,
    onNavigateToHtml: () -> Unit,
    onNavigateToC: () -> Unit,
    onNavigateToExam: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onUpdateInstructorPhoto: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToAlgoFlow: () -> Unit = {},
    onNavigateToCalculator: () -> Unit = {},
    onNavigateToFx100: () -> Unit = {},
    onNavigateToFx991: () -> Unit = {}
) {
    val htmlProblems = state.problems.filter { it.language == "HTML" }
    val cProblems = state.problems.filter { it.language == "C" }
    val htmlSolved = htmlProblems.count { state.progressMap[it.id]?.completed == true }
    val cSolved = cProblems.count { state.progressMap[it.id]?.completed == true }
    val htmlPct = if (htmlProblems.isNotEmpty()) (htmlSolved * 100) / htmlProblems.size else 0
    val cPct = if (cProblems.isNotEmpty()) (cSolved * 100) / cProblems.size else 0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Status & Greeting Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "স্বাগতম, ${state.settings.studentName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.settings.activeRole == UserRole.ADMIN) {
                            "অ্যাডমিন মোড সক্রিয়"
                        } else {
                            "HSC ICT প্র্যাকটিস ড্যাশবোর্ড"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Offline / Online + Smart Sync Badge
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (state.isOnline) {
                        SuccessGreen.copy(alpha = 0.14f)
                    } else {
                        AmberTertiary.copy(alpha = 0.16f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (state.isOnline) SuccessGreen.copy(alpha = 0.4f) else AmberTertiary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = onManualSync)
                        .testTag("sync_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isSyncing -> Icons.Rounded.Sync
                                state.isOnline -> Icons.Rounded.CloudDone
                                else -> Icons.Rounded.CloudOff
                            },
                            contentDescription = "Connection status",
                            tint = if (state.isOnline) SuccessGreen else AmberTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when {
                                isSyncing -> "Syncing..."
                                state.isOnline && state.queuedSyncCount == 0 -> "Online • Synced"
                                state.isOnline -> "Online • ${state.queuedSyncCount} Pending"
                                else -> "Offline Mode"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.isOnline) SuccessGreen else AmberTertiary
                        )
                    }
                }
            }
        }

        // Hero Hook Card ("❝আমি বিশ্বাস করি না!❞ A+ পাবো?")
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_coding),
                        contentDescription = "HSC ICT Coding Practice Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xD9042F2E),
                                        Color(0xEB0F172A)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AmberTertiary.copy(alpha = 0.22f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "HSC ICT • HTML5 & C Programming",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFEF3C7)
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.hero_hook_title),
                            fontFamily = BengaliFontFamily,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("hero_hook_headline")
                        )

                        Text(
                            text = stringResource(R.string.hero_hook_subtitle),
                            fontFamily = BengaliFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )

                        Spacer(Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToHtml,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldSecondary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_start_html_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Html,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("HTML ল্যাব", maxLines = 1)
                            }
                            Button(
                                onClick = onNavigateToC,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberTertiary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_start_c_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("C প্রোগ্রামিং", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Quick Progress Snapshot Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressMiniCard(
                    title = "HTML5 অনুশীলন",
                    solvedText = "$htmlSolved/${htmlProblems.size} সম্পন্ন",
                    percent = htmlPct,
                    accentColor = TealPrimary,
                    onClick = onNavigateToHtml,
                    modifier = Modifier.weight(1f)
                )
                ProgressMiniCard(
                    title = "C প্রোগ্রামিং",
                    solvedText = "$cSolved/${cProblems.size} সম্পন্ন",
                    percent = cPct,
                    accentColor = AmberTertiary,
                    onClick = onNavigateToC,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Featured Algorithm, Flowchart & Math Module Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onNavigateToAlgoFlow)
                    .testTag("home_algo_flow_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountTree,
                            contentDescription = "Algorithm & Flowchart",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "অ্যালগরিদম, ফ্লোচার্ট ও ম্যাথ",
                            fontFamily = BengaliFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "প্রতীক, সূত্র, ফ্লোচার্ট, সায়েন্টিফিক ও নাম্বার সিস্টেম ক্যালকুলেটর",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Direct Shortcuts: MS Series fx-100MS, fx-991MS & Number System Math
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = AmberTertiary.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToFx100)
                            .testTag("home_fx100_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AmberTertiary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Calculate,
                                        contentDescription = "fx-100MS",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "MS [fx-100MS]",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "240 Functions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberTertiary
                                    )
                                }
                            }
                            Text(
                                text = "ক্লাসিক ২-লাইন, ভগ্নাংশ (a b/c), SD ও সায়েন্টিফিক",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = TealPrimary.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToFx991)
                            .testTag("home_fx991_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(TealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Calculate,
                                        contentDescription = "fx-991MS",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "MS [fx-991MS]",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "401 Functions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TealPrimary
                                    )
                                }
                            }
                            Text(
                                text = "BASE-N, EQN, জটিল সংখ্যা, ∫dx/d/dx ও সোলার",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)
                    ),
                    border = BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToCalculator)
                        .testTag("home_sci_calc_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "নাম্বার সিস্টেম বোর্ড ম্যাথ (BIN/OCT/DEC/HEX ও ২-এর পরিপূরক)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = EmeraldSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Grid (Exam Mode, Bookmarks/Progress, Settings, Admin Panel)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickShortcutCard(
                    icon = Icons.Rounded.Quiz,
                    label = "বোর্ড পরীক্ষা",
                    tint = TealPrimary,
                    onClick = onNavigateToExam,
                    tag = "quick_exam_btn",
                    modifier = Modifier.weight(1f)
                )
                QuickShortcutCard(
                    icon = Icons.Rounded.Bookmark,
                    label = "মিস্টেক ও বুকমার্ক",
                    tint = EmeraldSecondary,
                    onClick = onNavigateToProgress,
                    tag = "quick_bookmarks_btn",
                    modifier = Modifier.weight(1f)
                )
                QuickShortcutCard(
                    icon = Icons.Rounded.AdminPanelSettings,
                    label = "অ্যাডমিন",
                    tint = AmberTertiary,
                    onClick = onNavigateToAdmin,
                    tag = "quick_admin_btn",
                    modifier = Modifier.weight(1f)
                )
                QuickShortcutCard(
                    icon = Icons.Rounded.Settings,
                    label = "সেটিংস",
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToSettings,
                    tag = "quick_settings_btn",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Notices & Announcements from Admin Panel
        if (state.notices.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "নোটিশ ও বোর্ড টিপস",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    state.notices.take(2).forEach { notice ->
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notice.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = notice.tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = notice.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dedicated পরিচিতি (About / Instructor Profile) Section on Home
        item {
            InstructorProfileCard(
                customPhotoPath = state.settings.instructorPhotoPath,
                photoVersion = state.settings.instructorPhotoVersion,
                onUpdatePhoto = onUpdateInstructorPhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
            )
        }
    }
}

@Composable
fun InstructorProfileCard(
    customPhotoPath: String? = null,
    photoVersion: Long = 0L,
    onUpdatePhoto: ((Uri) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdatePhoto?.invoke(uri)
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("instructor_about_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.instructor_section_title),
                    fontFamily = BengaliFontFamily,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            if (customPhotoPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(customPhotoPath))
                        .memoryCacheKey("instructor_$photoVersion")
                        .diskCacheKey("instructor_$photoVersion")
                        .build(),
                    contentDescription = "সালমান শাহীন - আদ্-দ্বীন সায়েন্স কলেজ, রংপুর",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_salman_shahin),
                    contentDescription = "সালমান শাহীন - আদ্-দ্বীন সায়েন্স কলেজ, রংপুর",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.instructor_name),
                    fontFamily = BengaliFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.instructor_college),
                    fontFamily = BengaliFontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = stringResource(R.string.instructor_message),
                fontFamily = BengaliFontFamily,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // WhatsApp & Direct Helpline Contact Strip
            val whatsappNumEn = stringResource(R.string.helpline_number_en)
            val whatsappUrl = stringResource(R.string.whatsapp_url)
            val telNumber = stringResource(R.string.helpline_tel)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = EmeraldSecondary.copy(alpha = 0.09f),
                border = BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "WhatsApp: $whatsappNumEn",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSecondary,
                        modifier = Modifier.testTag("instructor_whatsapp_number")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                                runCatching { context.startActivity(intent) }
                                    .onFailure {
                                        Toast.makeText(context, "WhatsApp বা ব্রাউজার ওপেন করা যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("instructor_whatsapp_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("WhatsApp", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telNumber"))
                                runCatching { context.startActivity(intent) }
                                    .onFailure {
                                        Toast.makeText(context, "ডায়ালার ওপেন করা যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("instructor_call_btn")
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("কল করুন", maxLines = 1)
                        }
                    }
                }
            }

            if (onUpdatePhoto != null) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("change_instructor_photo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("ছবি পরিবর্তন করুন")
                }
            }
        }
    }
}

@Composable
private fun ProgressMiniCard(
    title: String,
    solvedText: String,
    percent: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            LinearProgressIndicator(
                progress = { (percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = solvedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickShortcutCard(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
