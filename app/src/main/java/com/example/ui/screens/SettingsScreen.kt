package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.VaultViewModel
import com.example.ui.components.defaultTextFieldColors
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemeRegistry
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VaultBg
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import com.example.ui.theme.WarningYellow

@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val theme = LocalAppTheme.current

    var showClearConfirm by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showExportPinDialog by remember { mutableStateOf(false) }
    var exportPinInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Settings & Security",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Hardware KeyStore · Offline zero-knowledge privacy.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // User Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.primary, RoundedCornerShape(16.dp))
                    .clickable { viewModel.openProfileDialog() }
                    .testTag("user_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(theme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (settings.username.isNotBlank()) settings.username.take(1).uppercase() else "U",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryLight
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = settings.username,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${settings.organization} · ${settings.employeeId}",
                            fontSize = 12.sp,
                            color = theme.primaryLight
                        )
                        Text(
                            text = "${settings.department} · ${settings.securityClearance}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THEME SELECTION SECTION - Industry Standard Bright & Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APP APPEARANCE & THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Industry standard",
                    fontSize = 11.sp,
                    color = theme.textMuted
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeRegistry.themes.forEach { t ->
                    val isSelected = (theme.isLight && t.isLight) || (!theme.isLight && !t.isLight)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) t.primaryContainer else theme.surface
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) t.primary else theme.borderSubtle,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setTheme(t.id) }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.previewEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = t.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) t.primary else theme.textPrimary
                                    )
                                    Text(
                                        text = if (t.isLight) "Clean Enterprise" else "OLED Obsidian",
                                        fontSize = 10.sp,
                                        color = theme.textMuted
                                    )
                                }
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(t.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Toggles Card
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "Push Notifications",
                        subtitle = "Expiry & event deadline reminders",
                        checked = settings.notifs,
                        onCheckedChange = {
                            viewModel.repository.updateSettings(settings.copy(notifs = it))
                            viewModel.showToast(if (it) "Notifications enabled" else "Notifications disabled")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Audio Reminders",
                        subtitle = "Play synthesized sounds at scheduled times",
                        checked = settings.audioEnabled,
                        onCheckedChange = {
                            viewModel.repository.updateSettings(settings.copy(audioEnabled = it))
                            viewModel.showToast(if (it) "Audio reminders enabled" else "Audio reminders disabled")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Widget Preview",
                        subtitle = "Show next expiring item on home dashboard",
                        checked = settings.widget,
                        onCheckedChange = {
                            viewModel.repository.updateSettings(settings.copy(widget = it))
                            viewModel.showToast(if (it) "Widget preview enabled" else "Widget preview disabled")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Clipboard Auto-Clear",
                        subtitle = "Wipe copied secrets after 30 seconds",
                        checked = settings.clipboardClear,
                        onCheckedChange = {
                            viewModel.repository.updateSettings(settings.copy(clipboardClear = it))
                            viewModel.showToast(if (it) "Clipboard auto-clear enabled" else "Clipboard auto-clear disabled")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "VAULT SECURITY & STORAGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Login PIN (6 Digits)
            SettingsActionButton(
                icon = Icons.Default.LockReset,
                title = "Login PIN (6 Digits)",
                subtitle = "Active: •••••• · Reset with original PIN",
                accentColor = theme.primaryLight,
                onClick = { viewModel.openResetPinDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Secure Documents & Media
            SettingsActionButton(
                icon = Icons.Default.FolderShared,
                title = "Secure Documents & Photos",
                subtitle = "Manage ${documents.size} encrypted documents and photos",
                accentColor = theme.primary,
                onClick = { viewModel.openAddDocumentDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Security Audit Trail
            SettingsActionButton(
                icon = Icons.Default.Security,
                title = "Security Audit Trail",
                subtitle = "View cryptographically verified operations & access logs",
                accentColor = SafeGreen,
                onClick = { viewModel.openAuditLogsDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User Profile & Identity
            SettingsActionButton(
                icon = Icons.Default.Person,
                title = "User Profile & Identity",
                subtitle = "${settings.username} · ${settings.employeeId} · ${settings.securityClearance}",
                accentColor = theme.primaryLight,
                onClick = { viewModel.openProfileDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsActionButton(
                icon = Icons.Default.MedicalServices,
                title = "Edit Emergency Info",
                subtitle = "Update blood group, allergies & medical contacts",
                accentColor = UrgentRed,
                onClick = { viewModel.openEmergencyEditDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsActionButton(
                icon = Icons.Default.Share,
                title = "Export Encrypted Backup",
                subtitle = "Share full vault JSON backup securely with PIN encryption",
                accentColor = SafeGreen,
                onClick = { showExportPinDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsActionButton(
                icon = Icons.Default.Download,
                title = "Import Backup",
                subtitle = "Restore vault items, secrets & schedules from JSON",
                accentColor = SafeGreen,
                onClick = { showImportSheet = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsActionButton(
                icon = Icons.Default.Delete,
                title = "Open Trash (30-day recovery)",
                subtitle = "View and restore recently deleted items",
                accentColor = WarningYellow,
                onClick = { viewModel.openTrashDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsActionButton(
                icon = Icons.Default.LockReset,
                title = "Lock Vault Now",
                subtitle = "Immediately return to PIN lock screen",
                accentColor = theme.primaryLight,
                onClick = { viewModel.lockVault() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsActionButton(
                icon = Icons.Default.DeleteForever,
                title = "Delete All Data from Device",
                subtitle = "Permanent wipe of items, secrets and settings",
                accentColor = UrgentRed,
                onClick = { showClearConfirm = true }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Wipe Confirmation Dialog
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Wipe All Vault Data?", fontWeight = FontWeight.Bold, color = UrgentRed) },
                text = {
                    Text(
                        "This will permanently delete all vault items, secrets, documents, emergency records, audio schedules and settings from this device. This cannot be undone.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.repository.clearAllData()
                            showClearConfirm = false
                            viewModel.showToast("🗑️ All data deleted from device")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                    ) {
                        Text("Delete Everything", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = VaultSurface
            )
        }

        // Import Backup Sheet
        if (showImportSheet) {
            ImportBackupSheet(
                onDismiss = { showImportSheet = false },
                onImport = { json, pin ->
                    val success = viewModel.repository.importBackupJson(json, pin)
                    if (success) {
                        viewModel.showToast("✅ Backup restored successfully")
                        showImportSheet = false
                    } else {
                        viewModel.showToast("⚠️ Invalid backup JSON or incorrect decryption PIN")
                    }
                }
            )
        }

        // Export PIN Dialog
        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null) {
                try {
                    val json = viewModel.repository.exportBackupJson(exportPinInput.trim())
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                    viewModel.showToast("✅ Backup successfully saved to chosen folder")
                } catch (e: Exception) {
                    viewModel.showToast("⚠️ Failed to save backup: ${e.localizedMessage}")
                }
            }
            showExportPinDialog = false
            exportPinInput = ""
        }

        if (showExportPinDialog) {
            AlertDialog(
                onDismissRequest = { showExportPinDialog = false },
                title = { Text("Set Backup Encryption PIN & Destination", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Column {
                        Text("Enter a PIN/Password to encrypt your exported backup. You will need this PIN when restoring from this backup.", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = exportPinInput,
                            onValueChange = { exportPinInput = it },
                            label = { Text("Backup PIN / Password") },
                            placeholder = { Text("e.g. 123456 or secure password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = defaultTextFieldColors()
                        )
                    }
                },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val fileName = "MyVault_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                                exportLauncher.launch(fileName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📁 Choose Folder & Save...", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val path = viewModel.repository.exportBackupToFile(exportPinInput.trim())
                                if (path != null) {
                                    val file = File(path)
                                    val fileUri = Uri.fromFile(file)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_STREAM, fileUri)
                                        type = "application/json"
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Encrypted MyVault Backup")
                                    try {
                                        context.startActivity(shareIntent)
                                        viewModel.showToast("📤 Backup saved and ready to share")
                                    } catch (e: Exception) {
                                        viewModel.showToast("✅ Backup saved to: ${file.name}")
                                    }
                                } else {
                                    viewModel.showToast("⚠️ Failed to export backup file")
                                }
                                showExportPinDialog = false
                                exportPinInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📤 Export & Quick Share", fontWeight = FontWeight.Bold, color = theme.primary)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportPinDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = VaultSurface
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val theme = LocalAppTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = theme.primary
            )
        )
    }
}

@Composable
private fun SettingsActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportBackupSheet(
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    val context = LocalContext.current
    var jsonText by remember { mutableStateOf("") }
    var decryptPin by remember { mutableStateOf("") }
    val theme = LocalAppTheme.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader().use { it?.readText() } ?: ""
                jsonText = text
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = VaultSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "📥 Import Vault Backup",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Select a backup file or paste JSON and enter the decryption PIN below.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { filePickerLauncher.launch("application/json") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📁 Choose Backup File from Device")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = clipboard.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        jsonText = clip.getItemAt(0).text.toString()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 Paste from Clipboard")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = jsonText,
                onValueChange = { jsonText = it },
                label = { Text("Backup JSON") },
                placeholder = { Text("Paste JSON string here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = decryptPin,
                onValueChange = { decryptPin = it },
                label = { Text("Decryption PIN / Password (if encrypted)") },
                placeholder = { Text("Enter backup PIN if required") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onImport(jsonText.trim(), decryptPin.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Backup", fontWeight = FontWeight.Bold)
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
