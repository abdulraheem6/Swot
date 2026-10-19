package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioSchedule
import com.example.ui.VaultViewModel
import com.example.ui.components.RegimeBadge
import com.example.ui.components.defaultTextFieldColors
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VaultBg
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultPrimaryLight
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class ToneDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String
)

val AVAILABLE_TONES = listOf(
    ToneDescriptor("chime", "Harmonic Chime", "Dual C5 & G5 natural decay bell tone", "🔔"),
    ToneDescriptor("gentle", "Gentle Bell", "Warm soothing 4-note harp arpeggio", "🎶"),
    ToneDescriptor("radar", "Radar Pulse", "Focused high-pitch sonar repetition", "📡"),
    ToneDescriptor("alarm", "Warning Alarm", "Dual alert frequency pulse", "🚨"),
    ToneDescriptor("digital", "Cyber Blip", "Short futuristic vault status beep", "⚡")
)

@Composable
fun AudioScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val schedules by viewModel.schedules.collectAsState()
    val playingTone by viewModel.currentlyPlayingTone.collectAsState()
    var editingSchedule by remember { mutableStateOf<AudioSchedule?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔊 Audio Library",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            RegimeBadge(
                                regime = viewModel.settings.collectAsState().value.securityRegime,
                                onClick = { viewModel.openRegimeDialog() }
                            )
                        }
                        Text(
                            text = "Synthesized tones, custom audio messages & reminders.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.openAddScheduleDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_schedule_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Audio Tone Synthesizers section
            item {
                Text(
                    text = "AVAILABLE AUDIO TONES (${AVAILABLE_TONES.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VaultPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(AVAILABLE_TONES, key = { it.id }) { tone ->
                val isPlaying = playingTone == tone.id
                Card(
                    colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            1.dp,
                            if (isPlaying) VaultPrimaryLight else BorderSubtle,
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x266C63FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tone.emoji, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tone.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = tone.description,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) viewModel.stopAudioTone()
                                else viewModel.playAudioTone(tone.id)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPlaying) VaultPrimary else Color(0x1F6C63FF))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Play",
                                tint = if (isPlaying) Color.White else VaultPrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Scheduled Audio Reminders section
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCHEDULED REMINDERS (${schedules.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VaultPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (schedules.isEmpty()) {
                item {
                    Text(
                        text = "No audio reminders scheduled yet. Tap 'Add' to set one.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(schedules, key = { it.id }) { sched ->
                    val isCustom = sched.soundTone == "custom" || !sched.customAudioUri.isNullOrBlank()
                    val isPlayingThis = playingTone == (if (isCustom) "custom" else sched.soundTone)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, if (isPlayingThis) VaultPrimaryLight else BorderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCustom) Color(0x336C63FF) else if (sched.enabled) SafeGreenBg else Color(0x14FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isCustom) "🎵" else "⏰", fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sched.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sched.enabled) TextPrimary else TextMuted
                                    )
                                    if (sched.repeat != "none") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SafeGreenBg)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(sched.repeat, fontSize = 8.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (isCustom) {
                                    Text(
                                        text = "🎵 Custom: ${sched.customAudioName ?: "User Audio"} · ${sched.dateTime.replace("T", " ")}",
                                        fontSize = 11.sp,
                                        color = VaultPrimaryLight
                                    )
                                } else {
                                    Text(
                                        text = "${sched.soundTone.replaceFirstChar { it.uppercase() }} tone · ${sched.dateTime.replace("T", " ")}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPlayingThis) viewModel.stopAudioTone()
                                        else viewModel.playScheduleAudio(sched)
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPlayingThis) VaultPrimary else Color(0x1A6C63FF))
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Test Sound",
                                        tint = if (isPlayingThis) Color.White else VaultPrimaryLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Switch(
                                    checked = sched.enabled,
                                    onCheckedChange = { viewModel.repository.toggleSchedule(sched.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = VaultPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { editingSchedule = sched },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.primaryContainer)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Schedule", tint = theme.primaryLight, modifier = Modifier.size(15.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.repository.deleteSchedule(sched.id) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        if (editingSchedule != null) {
            EditScheduleSheet(
                schedule = editingSchedule!!,
                onDismiss = { editingSchedule = null },
                onSave = { updated ->
                    viewModel.updateSchedule(updated)
                    editingSchedule = null
                },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    onDismiss: () -> Unit,
    onSave: (AudioSchedule) -> Unit,
    viewModel: VaultViewModel
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val defaultDate = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        dateFormat.format(cal.time)
    }

    var name by remember { mutableStateOf("") }
    var soundTone by remember { mutableStateOf("chime") }
    var isCustomAudio by remember { mutableStateOf(false) }
    var customAudioUri by remember { mutableStateOf<String?>(null) }
    var customAudioName by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf(defaultDate) }
    var time by remember { mutableStateOf("09:00") }
    var repeat by remember { mutableStateOf("none") }
    var volume by remember { mutableFloatStateOf(80f) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "audio_${System.currentTimeMillis()}.mp3"
            val savedPath = viewModel.saveAudioFile(uri, fileName)
            customAudioUri = savedPath
            customAudioName = fileName
            isCustomAudio = true
            soundTone = "custom"
            viewModel.showToast("🎵 Loaded custom audio: $fileName")
        }
    }

    val repeats = listOf(
        "none" to "Once",
        "daily" to "Daily",
        "weekly" to "Weekly",
        "monthly" to "Monthly",
        "yearly" to "Yearly"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = VaultSurface,
        modifier = Modifier.testTag("add_schedule_sheet")
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Audio Reminder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Pick a sound tone or choose your own audio file, date and time.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Reminder Name") },
                placeholder = { Text("e.g. Daily Standup, Medicine, Mortgage Alert") },
                modifier = Modifier.fillMaxWidth().testTag("schedule_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sound Source Switcher: Built-in Tone vs User's Own Audio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VaultSurfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isCustomAudio) VaultPrimary else Color.Transparent)
                        .clickable { isCustomAudio = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔔 Built-in Tone",
                        fontSize = 12.sp,
                        fontWeight = if (!isCustomAudio) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isCustomAudio) Color.White else TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCustomAudio) VaultPrimary else Color.Transparent)
                        .clickable { isCustomAudio = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🎙️ My Own Audio",
                        fontSize = 12.sp,
                        fontWeight = if (isCustomAudio) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCustomAudio) Color.White else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isCustomAudio) {
                // Tone dropdown
                var toneExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = toneExpanded,
                    onExpandedChange = { toneExpanded = it }
                ) {
                    OutlinedTextField(
                        value = soundTone.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sound Tone") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toneExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = defaultTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = toneExpanded,
                        onDismissRequest = { toneExpanded = false },
                        containerColor = VaultSurfaceVariant
                    ) {
                        AVAILABLE_TONES.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.emoji} ${t.name}", color = TextPrimary) },
                                onClick = {
                                    soundTone = t.id
                                    toneExpanded = false
                                    viewModel.playAudioTone(t.id, volume.toInt())
                                }
                            )
                        }
                    }
                }
            } else {
                // User's own audio picker
                Card(
                    colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderAccent, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "USER AUDIO MESSAGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VaultPrimaryLight
                            )
                            if (customAudioUri != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SafeGreenBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("READY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (customAudioUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFFFFF))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎵", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customAudioName ?: "Selected audio file",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Stored securely in private app cache",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.playCustomAudio(customAudioUri!!, volume.toInt())
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = VaultPrimaryLight)
                                }

                                IconButton(
                                    onClick = {
                                        customAudioUri = null
                                        customAudioName = null
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = UrgentRed)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = VaultPrimaryLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (customAudioUri != null) "Change Audio File" else "📂 Choose Audio File (MP3, WAV, M4A, OGG)",
                                color = VaultPrimaryLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    readOnly = true,
                    label = { Text("Date (YYYY-MM-DD)") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    date = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Default.Event, contentDescription = "Pick Date", tint = VaultPrimaryLight)
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable {
                            val cal = Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    date = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    readOnly = true,
                    label = { Text("Time (HH:MM)") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    time = String.format(java.util.Locale.US, "%02d:%02d", hour, minute)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick Time", tint = VaultPrimaryLight)
                        }
                    },
                    modifier = Modifier
                        .weight(0.8f)
                        .clickable {
                            val cal = Calendar.getInstance()
                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    time = String.format(java.util.Locale.US, "%02d:%02d", hour, minute)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                    colors = defaultTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Repeat Dropdown
            var repeatExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = repeatExpanded,
                onExpandedChange = { repeatExpanded = it }
            ) {
                OutlinedTextField(
                    value = repeats.find { it.first == repeat }?.second ?: repeat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Repeat Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = repeatExpanded,
                    onDismissRequest = { repeatExpanded = false },
                    containerColor = VaultSurfaceVariant
                ) {
                    repeats.forEach { (repId, repLabel) ->
                        DropdownMenuItem(
                            text = { Text(repLabel, color = TextPrimary) },
                            onClick = {
                                repeat = repId
                                repeatExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Volume", fontSize = 13.sp, color = TextPrimary)
                Text("${volume.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VaultPrimary)
            }

            Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = VaultPrimary,
                    activeTrackColor = VaultPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showToast("⚠️ Reminder name required")
                        return@Button
                    }
                    val newSched = AudioSchedule(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        soundTone = if (isCustomAudio) "custom" else soundTone,
                        customAudioUri = if (isCustomAudio) customAudioUri else null,
                        customAudioName = if (isCustomAudio) (customAudioName ?: "User Audio") else null,
                        dateTime = "${date.trim()}T${time.trim()}",
                        repeat = repeat,
                        volume = volume.toInt(),
                        enabled = true
                    )
                    onSave(newSched)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_schedule_button")
            ) {
                Text("Schedule Reminder", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = TextMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleSheet(
    schedule: AudioSchedule,
    onDismiss: () -> Unit,
    onSave: (AudioSchedule) -> Unit,
    viewModel: VaultViewModel
) {
    val context = LocalContext.current
    val parts = schedule.dateTime.split("T", " ")
    val initialDate = if (parts.isNotEmpty()) parts[0] else "2026-10-12"
    val initialTime = if (parts.size > 1) parts[1] else "09:00"

    var name by remember { mutableStateOf(schedule.name) }
    var soundTone by remember { mutableStateOf(if (schedule.soundTone == "custom") "chime" else schedule.soundTone) }
    var isCustomAudio by remember { mutableStateOf(schedule.soundTone == "custom" || !schedule.customAudioUri.isNullOrBlank()) }
    var customAudioUri by remember { mutableStateOf(schedule.customAudioUri) }
    var customAudioName by remember { mutableStateOf(schedule.customAudioName) }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var repeat by remember { mutableStateOf(schedule.repeat) }
    var volume by remember { mutableStateOf(schedule.volume.toFloat()) }

    var toneExpanded by remember { mutableStateOf(false) }
    var repeatExpanded by remember { mutableStateOf(false) }

    val tones = listOf("chime", "radar", "gentle", "alarm", "pulse", "bell", "digital")
    val repeatOptions = listOf("none", "daily", "weekly", "monthly", "yearly")

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            var displayName = "User Audio"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}

            customAudioUri = uri.toString()
            customAudioName = displayName
            isCustomAudio = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = VaultSurface,
        modifier = Modifier.testTag("edit_schedule_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Edit Scheduled Reminder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Update alarm trigger, date & sound tone.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Reminder Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_schedule_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Time pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val d = sdf.parse(date)
                                if (d != null) cal.time = d
                            } catch (_: Exception) {}

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Default.Event, contentDescription = "Calendar Picker", tint = VaultPrimaryLight)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:MM)") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            try {
                                val partsTime = time.split(":")
                                if (partsTime.size >= 2) {
                                    cal.set(Calendar.HOUR_OF_DAY, partsTime[0].toInt())
                                    cal.set(Calendar.MINUTE, partsTime[1].toInt())
                                }
                            } catch (_: Exception) {}

                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    time = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Time Picker", tint = VaultPrimaryLight)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = defaultTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Audio Toggle
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Attach Custom Audio File", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Use MP3/WAV/AAC recording", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = isCustomAudio,
                            onCheckedChange = { isCustomAudio = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VaultPrimary
                            )
                        )
                    }

                    if (isCustomAudio) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose File", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = customAudioName ?: "No file chosen",
                                fontSize = 12.sp,
                                color = if (customAudioUri != null) VaultPrimaryLight else TextMuted,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sound Tone Dropdown (when not custom)
            if (!isCustomAudio) {
                ExposedDropdownMenuBox(
                    expanded = toneExpanded,
                    onExpandedChange = { toneExpanded = it }
                ) {
                    OutlinedTextField(
                        value = soundTone.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Synthesized Tone") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toneExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = defaultTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = toneExpanded,
                        onDismissRequest = { toneExpanded = false },
                        containerColor = VaultSurfaceVariant
                    ) {
                        tones.forEach { tone ->
                            DropdownMenuItem(
                                text = { Text(tone.replaceFirstChar { it.uppercase() }, color = TextPrimary) },
                                onClick = {
                                    soundTone = tone
                                    toneExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Repeat Dropdown
            ExposedDropdownMenuBox(
                expanded = repeatExpanded,
                onExpandedChange = { repeatExpanded = it }
            ) {
                OutlinedTextField(
                    value = repeat.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Repeat Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = repeatExpanded,
                    onDismissRequest = { repeatExpanded = false },
                    containerColor = VaultSurfaceVariant
                ) {
                    repeatOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.replaceFirstChar { it.uppercase() }, color = TextPrimary) },
                            onClick = {
                                repeat = opt
                                repeatExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Volume", fontSize = 13.sp, color = TextPrimary)
                Text("${volume.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VaultPrimary)
            }

            Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = VaultPrimary,
                    activeTrackColor = VaultPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showToast("⚠️ Reminder name required")
                        return@Button
                    }
                    val updated = schedule.copy(
                        name = name.trim(),
                        soundTone = if (isCustomAudio) "custom" else soundTone,
                        customAudioUri = if (isCustomAudio) customAudioUri else null,
                        customAudioName = if (isCustomAudio) (customAudioName ?: "User Audio") else null,
                        dateTime = "${date.trim()}T${time.trim()}",
                        repeat = repeat,
                        volume = volume.toInt()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("update_schedule_button")
            ) {
                Text("Update Scheduled Reminder", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = TextMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
