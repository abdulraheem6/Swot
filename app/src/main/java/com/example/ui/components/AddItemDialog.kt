package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.VaultItem
import com.example.ui.VaultViewModel
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    onDismiss: () -> Unit,
    onSave: (VaultItem) -> Unit,
    viewModel: VaultViewModel,
    prefilledScan: ScanResult? = null
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val defaultDate = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 3)
        dateFormat.format(cal.time)
    }

    var name by remember { mutableStateOf(prefilledScan?.name ?: "") }
    var category by remember { mutableStateOf(prefilledScan?.category ?: "product") }
    var date by remember { mutableStateOf(prefilledScan?.date ?: defaultDate) }
    var time by remember { mutableStateOf("09:00") }
    var notes by remember { mutableStateOf(prefilledScan?.notes ?: "") }
    var cardNumber by remember { mutableStateOf(prefilledScan?.cardNumber ?: "") }
    var recurring by remember { mutableStateOf(false) }
    var recurInterval by remember { mutableIntStateOf(7) }
    var locked by remember { mutableStateOf(false) }
    var requirePin by remember { mutableStateOf(true) }
    var require2fa by remember { mutableStateOf(false) }
    var customPin by remember { mutableStateOf("") }
    var custom2faKey by remember { mutableStateOf("") }
    var attachAudio by remember { mutableStateOf(true) } // Enabled by default for urgent reminders
    var soundTone by remember { mutableStateOf("alarm") }
    var isCustomAudio by remember { mutableStateOf(false) }
    var customAudioUri by remember { mutableStateOf<String?>(null) }
    var customAudioName by remember { mutableStateOf<String?>(null) }
    var attachedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    fun pickDate() {
        val cal = Calendar.getInstance()
        try {
            val parsed = dateFormat.parse(date)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                date = picked
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickTime() {
        val cal = Calendar.getInstance()
        var h = 9
        var m = 0
        try {
            val parts = time.split(":")
            if (parts.size >= 2) {
                h = parts[0].toIntOrNull() ?: 9
                m = parts[1].toIntOrNull() ?: 0
            }
        } catch (_: Exception) {}
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            },
            h,
            m,
            true
        ).show()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri
        }
    }

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customAudioUri = uri.toString()
            customAudioName = uri.lastPathSegment ?: "Custom Audio File"
        }
    }

    val categories = listOf(
        "product" to "🥛 Product Expiry",
        "document" to "📄 Document Renewal",
        "subscription" to "💳 Subscription Due",
        "card" to "🪪 ID / Card Expiry",
        "event" to "🎂 Event / Milestone"
    )

    val intervals = listOf(
        1 to "1 day",
        3 to "3 days",
        5 to "5 days",
        7 to "1 week",
        14 to "2 weeks",
        30 to "1 month",
        90 to "3 months",
        365 to "1 year"
    )

    val soundTones = listOf("alarm", "radar", "chime", "gentle", "digital")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        modifier = Modifier.testTag("add_item_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add Task / Vault Item",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Set due date, pick exact time & configure loud alarm alerts",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Item Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Task / Item Title") },
                placeholder = { Text("e.g. Passport Renewal, Electricity Bill, Whole Milk") },
                modifier = Modifier.fillMaxWidth().testTag("item_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.first == category }?.second ?: category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    containerColor = theme.surfaceVariant
                ) {
                    categories.forEach { (catId, catLabel) ->
                        DropdownMenuItem(
                            text = { Text(catLabel, color = theme.textPrimary) },
                            onClick = {
                                category = catId
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DATE & TIME PICKER SECTION (INTERACTIVE DIALOGS)
            Text(
                text = "📅 Due Date & Time (Tap to Pick)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Date Picker Field
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    readOnly = true,
                    label = { Text("Due Date") },
                    trailingIcon = {
                        IconButton(onClick = { pickDate() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = theme.primary)
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { pickDate() }
                        .testTag("item_date_input"),
                    colors = defaultTextFieldColors()
                )

                // Interactive Time Picker Field
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    readOnly = true,
                    label = { Text("Time") },
                    trailingIcon = {
                        IconButton(onClick = { pickTime() }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick Time", tint = theme.primary)
                        }
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .clickable { pickTime() }
                        .testTag("item_time_input"),
                    colors = defaultTextFieldColors()
                )
            }

            // Quick Date Presets Row
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Today" to 0,
                    "+1d" to 1,
                    "+3d" to 3,
                    "+1w" to 7,
                    "+1m" to 30,
                    "+1y" to 365
                ).forEach { (label, days) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primaryContainer)
                            .clickable {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, days)
                                date = dateFormat.format(cal.time)
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(label, fontSize = 11.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ALARM & LOUD AUDIO REMINDER SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (attachAudio) theme.primary.copy(alpha = 0.5f) else theme.borderSubtle, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (attachAudio) UrgentRedBg else Color(0x1A888888)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = if (attachAudio) UrgentRed else theme.textMuted, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("⏰ Loud Phone Alarm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                Text("Rings aloud & wakes screen like real alarm clock", fontSize = 10.sp, color = theme.textMuted)
                            }
                        }
                        Switch(
                            checked = attachAudio,
                            onCheckedChange = { attachAudio = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (attachAudio) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Alarm Sound Tone:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = theme.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            soundTones.forEach { toneKey ->
                                val isSelected = !isCustomAudio && soundTone == toneKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) theme.primary else theme.surface)
                                        .border(1.dp, if (isSelected) theme.primary else theme.borderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            isCustomAudio = false
                                            soundTone = toneKey
                                            viewModel.playAudioTone(toneKey)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toneKey.replaceFirstChar { it.uppercase() },
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else theme.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Test Tone Button
                        Button(
                            onClick = { viewModel.playAudioTone(soundTone) },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔊 Test Alarm Sound ($soundTone)", fontSize = 11.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recurring Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔁 Recurring Reminder", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                    Text("Auto-schedules next due date when marked done", fontSize = 11.sp, color = theme.textMuted)
                }
                Switch(
                    checked = recurring,
                    onCheckedChange = { recurring = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                )
            }

            if (recurring) {
                Spacer(modifier = Modifier.height(8.dp))
                var intervalExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = intervalExpanded,
                    onExpandedChange = { intervalExpanded = it }
                ) {
                    OutlinedTextField(
                        value = intervals.find { it.first == recurInterval }?.second ?: "$recurInterval days",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = defaultTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = intervalExpanded,
                        onDismissRequest = { intervalExpanded = false },
                        containerColor = theme.surfaceVariant
                    ) {
                        intervals.forEach { (days, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = theme.textPrimary) },
                                onClick = {
                                    recurInterval = days
                                    intervalExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (category == "card") {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card / Document ID") },
                    placeholder = { Text("•••• •••• •••• 1234") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Details (Optional)") },
                placeholder = { Text("Instructions, policy numbers, or specifics...") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Attach photo section
            if (attachedPhotoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF14141E))
                        .border(1.dp, theme.primary, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = attachedPhotoUri,
                        contentDescription = "Attached photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { attachedPhotoUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                documentPickerLauncher.launch(arrayOf("*/*"))
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Browse Doc", color = theme.primaryLight, fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo / Scan", color = theme.primaryLight, fontSize = 11.sp, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Lock / Biometric Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔐 Require Biometric / PIN", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                    Text("Guards access to this item with security challenge", fontSize = 11.sp, color = theme.textMuted)
                }
                Switch(
                    checked = locked,
                    onCheckedChange = { locked = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showToast("⚠️ Item name required")
                        return@Button
                    }
                    val finalAttachList = mutableListOf<String>()
                    attachedPhotoUri?.let { finalAttachList.add(it.toString()) }

                    val newItem = VaultItem(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        category = category,
                        date = date.trim(),
                        notes = notes.trim(),
                        cardNumber = cardNumber.trim(),
                        recurring = recurring,
                        recurInterval = if (recurring) recurInterval else 0,
                        locked = locked,
                        requirePin = locked,
                        require2fa = locked && require2fa,
                        customPin = customPin.trim(),
                        custom2faKey = custom2faKey.trim(),
                        audioId = if (attachAudio) {
                            if (isCustomAudio) customAudioUri else soundTone
                        } else null,
                        audioTime = if (attachAudio) time else null,
                        attachments = finalAttachList
                    )
                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_item_button")
            ) {
                Text("Save Task & Schedule Alarm", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Cancel", color = theme.textMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * EDIT TASK / VAULT ITEM SHEET
 * Provides complete editing functionality with interactive Date & Time pickers,
 * real alarm scheduling, tone testing, recurring frequency, and deletion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemSheet(
    item: VaultItem,
    onDismiss: () -> Unit,
    onSave: (VaultItem) -> Unit,
    onDelete: () -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var date by remember { mutableStateOf(item.date) }
    var time by remember { mutableStateOf(item.audioTime ?: "09:00") }
    var notes by remember { mutableStateOf(item.notes) }
    var cardNumber by remember { mutableStateOf(item.cardNumber) }
    var recurring by remember { mutableStateOf(item.recurring) }
    var recurInterval by remember { mutableIntStateOf(if (item.recurInterval > 0) item.recurInterval else 7) }
    var locked by remember { mutableStateOf(item.locked) }
    var attachAudio by remember { mutableStateOf(item.audioId != null) }
    var soundTone by remember { mutableStateOf(item.audioId ?: "alarm") }
    var isCustomAudio by remember { mutableStateOf(false) }
    var customAudioUri by remember { mutableStateOf<String?>(null) }
    var customAudioName by remember { mutableStateOf<String?>(null) }
    var attachedPhotoUri by remember { mutableStateOf<Uri?>(item.attachments.firstOrNull()?.let { Uri.parse(it) }) }

    fun pickDate() {
        val cal = Calendar.getInstance()
        try {
            val parsed = dateFormat.parse(date)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                date = picked
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickTime() {
        var h = 9
        var m = 0
        try {
            val parts = time.split(":")
            if (parts.size >= 2) {
                h = parts[0].toIntOrNull() ?: 9
                m = parts[1].toIntOrNull() ?: 0
            }
        } catch (_: Exception) {}
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            },
            h,
            m,
            true
        ).show()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri
        }
    }

    val categories = listOf(
        "product" to "🥛 Product Expiry",
        "document" to "📄 Document Renewal",
        "subscription" to "💳 Subscription Due",
        "card" to "🪪 ID / Card Expiry",
        "event" to "🎂 Event / Milestone"
    )

    val intervals = listOf(
        1 to "1 day",
        3 to "3 days",
        5 to "5 days",
        7 to "1 week",
        14 to "2 weeks",
        30 to "1 month",
        90 to "3 months",
        365 to "1 year"
    )

    val soundTones = listOf("alarm", "radar", "chime", "gentle", "digital")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        modifier = Modifier.testTag("edit_item_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Edit Task / Vault Item",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Modify due date, alarm schedule or details",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Item Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Task / Item Title") },
                modifier = Modifier.fillMaxWidth().testTag("edit_item_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.first == category }?.second ?: category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    containerColor = theme.surfaceVariant
                ) {
                    categories.forEach { (catId, catLabel) ->
                        DropdownMenuItem(
                            text = { Text(catLabel, color = theme.textPrimary) },
                            onClick = {
                                category = catId
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DATE & TIME PICKERS
            Text(
                text = "📅 Due Date & Time (Tap to Pick)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Date Picker Field
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    readOnly = true,
                    label = { Text("Due Date") },
                    trailingIcon = {
                        IconButton(onClick = { pickDate() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = theme.primary)
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { pickDate() }
                        .testTag("edit_item_date_input"),
                    colors = defaultTextFieldColors()
                )

                // Interactive Time Picker Field
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    readOnly = true,
                    label = { Text("Time") },
                    trailingIcon = {
                        IconButton(onClick = { pickTime() }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick Time", tint = theme.primary)
                        }
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .clickable { pickTime() }
                        .testTag("edit_item_time_input"),
                    colors = defaultTextFieldColors()
                )
            }

            // Quick Date Presets Row
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Today" to 0,
                    "+1d" to 1,
                    "+3d" to 3,
                    "+1w" to 7,
                    "+1m" to 30,
                    "+1y" to 365
                ).forEach { (label, days) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primaryContainer)
                            .clickable {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, days)
                                date = dateFormat.format(cal.time)
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(label, fontSize = 11.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ALARM & LOUD AUDIO REMINDER SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (attachAudio) theme.primary.copy(alpha = 0.5f) else theme.borderSubtle, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (attachAudio) UrgentRedBg else Color(0x1A888888)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = if (attachAudio) UrgentRed else theme.textMuted, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("⏰ Loud Phone Alarm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                Text("Rings aloud & wakes screen like real alarm clock", fontSize = 10.sp, color = theme.textMuted)
                            }
                        }
                        Switch(
                            checked = attachAudio,
                            onCheckedChange = { attachAudio = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (attachAudio) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Alarm Sound Tone:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = theme.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            soundTones.forEach { toneKey ->
                                val isSelected = !isCustomAudio && soundTone == toneKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) theme.primary else theme.surface)
                                        .border(1.dp, if (isSelected) theme.primary else theme.borderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            isCustomAudio = false
                                            soundTone = toneKey
                                            viewModel.playAudioTone(toneKey)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toneKey.replaceFirstChar { it.uppercase() },
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else theme.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Test Tone Button
                        Button(
                            onClick = { viewModel.playAudioTone(soundTone) },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔊 Test Alarm Sound ($soundTone)", fontSize = 11.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recurring Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔁 Recurring Reminder", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                    Text("Auto-schedules next due date when marked done", fontSize = 11.sp, color = theme.textMuted)
                }
                Switch(
                    checked = recurring,
                    onCheckedChange = { recurring = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                )
            }

            if (recurring) {
                Spacer(modifier = Modifier.height(8.dp))
                var intervalExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = intervalExpanded,
                    onExpandedChange = { intervalExpanded = it }
                ) {
                    OutlinedTextField(
                        value = intervals.find { it.first == recurInterval }?.second ?: "$recurInterval days",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = defaultTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = intervalExpanded,
                        onDismissRequest = { intervalExpanded = false },
                        containerColor = theme.surfaceVariant
                    ) {
                        intervals.forEach { (days, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = theme.textPrimary) },
                                onClick = {
                                    recurInterval = days
                                    intervalExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (category == "card") {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card / Document ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Details (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Attach photo section
            if (attachedPhotoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF14141E))
                        .border(1.dp, theme.primary, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = attachedPhotoUri,
                        contentDescription = "Attached photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { attachedPhotoUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                documentPickerLauncher.launch(arrayOf("*/*"))
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Browse Doc", color = theme.primaryLight, fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo / Scan", color = theme.primaryLight, fontSize = 11.sp, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Lock / Biometric Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔐 Require Biometric / PIN", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                    Text("Guards access to this item with security challenge", fontSize = 11.sp, color = theme.textMuted)
                }
                Switch(
                    checked = locked,
                    onCheckedChange = { locked = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showToast("⚠️ Item name required")
                        return@Button
                    }
                    val finalAttachList = mutableListOf<String>()
                    attachedPhotoUri?.let { finalAttachList.add(it.toString()) }

                    val updated = item.copy(
                        name = name.trim(),
                        category = category,
                        date = date.trim(),
                        notes = notes.trim(),
                        cardNumber = cardNumber.trim(),
                        recurring = recurring,
                        recurInterval = if (recurring) recurInterval else 0,
                        locked = locked,
                        requirePin = locked,
                        audioId = if (attachAudio) soundTone else null,
                        audioTime = if (attachAudio) time else null,
                        attachments = finalAttachList
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_edit_item_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = UrgentRedBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("delete_item_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Task", color = UrgentRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Cancel", color = theme.textMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
