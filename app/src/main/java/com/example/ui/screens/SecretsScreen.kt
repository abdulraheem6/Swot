package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.audio.AudioTonePlayer
import java.text.SimpleDateFormat
import java.util.Calendar
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecretItem
import com.example.ui.VaultViewModel
import com.example.ui.components.defaultTextFieldColors
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultBg
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import java.util.UUID

data class CategoryDefinition(val id: String, val icon: String, val label: String)

val SECRET_CATEGORIES = listOf(
    CategoryDefinition("all", "🗂️", "All"),
    CategoryDefinition("login", "🔑", "Logins"),
    CategoryDefinition("bank", "🏦", "Bank"),
    CategoryDefinition("card", "💳", "Cards"),
    CategoryDefinition("wifi", "📶", "WiFi"),
    CategoryDefinition("identity", "🪪", "IDs"),
    CategoryDefinition("api", "🔌", "APIs"),
    CategoryDefinition("recovery", "🔐", "Recovery"),
    CategoryDefinition("medical", "🏥", "Medical"),
    CategoryDefinition("vehicle", "🚗", "Vehicle"),
    CategoryDefinition("crypto", "🪙", "Crypto"),
    CategoryDefinition("note", "📝", "Notes")
)

data class FieldDef(val key: String, val label: String, val isSecret: Boolean = false)

fun getFieldsForCategory(cat: String): List<FieldDef> {
    return when (cat) {
        "login" -> listOf(
            FieldDef("username", "Username / Email"),
            FieldDef("password", "Password", isSecret = true),
            FieldDef("url", "Website / App"),
            FieldDef("totpSecret", "2FA Secret Key", isSecret = true)
        )
        "bank" -> listOf(
            FieldDef("bankName", "Bank Name"),
            FieldDef("accountNumber", "Account Number"),
            FieldDef("ifsc", "IFSC / Routing / SWIFT"),
            FieldDef("branch", "Branch"),
            FieldDef("netbankingUser", "Netbanking Username")
        )
        "card" -> listOf(
            FieldDef("cardNumber", "Card Number"),
            FieldDef("cardHolder", "Cardholder Name"),
            FieldDef("expiry", "Expiry (MM/YY)"),
            FieldDef("cvv", "CVV / Security Code", isSecret = true),
            FieldDef("pin", "Card PIN", isSecret = true)
        )
        "wifi" -> listOf(
            FieldDef("ssid", "Network Name (SSID)"),
            FieldDef("password", "WiFi Password", isSecret = true),
            FieldDef("security", "Security (WPA2/WPA3)")
        )
        "identity" -> listOf(
            FieldDef("docType", "Document Type (Passport / ID / License)"),
            FieldDef("docNumber", "Document Number"),
            FieldDef("issued", "Issued Date"),
            FieldDef("expiry", "Expiry Date"),
            FieldDef("authority", "Issuing Authority")
        )
        "api" -> listOf(
            FieldDef("service", "Service / Platform"),
            FieldDef("apiKey", "API Key / Token", isSecret = true),
            FieldDef("secret", "Secret Key", isSecret = true),
            FieldDef("env", "Environment (Production / Staging)")
        )
        "recovery" -> listOf(
            FieldDef("service", "Service / Account"),
            FieldDef("codes", "Backup Recovery Codes (comma-separated)", isSecret = true)
        )
        "medical" -> listOf(
            FieldDef("insurance", "Policy Number / Insurance"),
            FieldDef("doctor", "Primary Physician"),
            FieldDef("hospital", "Emergency Hospital")
        )
        "vehicle" -> listOf(
            FieldDef("make", "Make / Model"),
            FieldDef("plate", "License Plate"),
            FieldDef("vin", "VIN / Chassis Number"),
            FieldDef("insurance", "Vehicle Insurance #")
        )
        "crypto" -> listOf(
            FieldDef("wallet", "Wallet Name"),
            FieldDef("seed", "Seed Phrase / Private Key (12/24 words)", isSecret = true),
            FieldDef("address", "Public Wallet Address")
        )
        "note" -> listOf(
            FieldDef("content", "Secure Note Content")
        )
        else -> listOf(
            FieldDef("content", "Details")
        )
    }
}

@Composable
fun SecretsScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val secrets by viewModel.secrets.collectAsState()
    val activeCategory by viewModel.activeSecretCategory.collectAsState()

    val filtered = remember(secrets, activeCategory) {
        if (activeCategory == "all") secrets
        else secrets.filter { it.category == activeCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔐 Secrets Vault",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        com.example.ui.components.RegimeBadge(
                            regime = viewModel.settings.collectAsState().value.securityRegime,
                            onClick = { viewModel.openRegimeDialog() }
                        )
                    }
                    Text(
                        text = "AES-256 encrypted on-device. Store credentials & notes.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.openAddSecretDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_secret_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Tiles (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SECRET_CATEGORIES.forEach { cat ->
                    val selected = cat.id == activeCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) theme.primaryContainer else VaultSurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (selected) theme.primary else BorderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setActiveSecretCategory(cat.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔐", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No secrets in this category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Add' to store encrypted credentials.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.openAddSecretDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_add_secret_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Secret", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { secret ->
                        SecretItemRow(
                            secret = secret,
                            onClick = { viewModel.openSecretDetail(secret) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }
        }

        // Floating Action Button for prominent Add Secret option
        FloatingActionButton(
            onClick = { viewModel.openAddSecretDialog() },
            containerColor = theme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_secret")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Secret")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Secret", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSecretSheet(
    onDismiss: () -> Unit,
    onSave: (SecretItem) -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    var category by remember { mutableStateOf("login") }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var favorite by remember { mutableStateOf(false) }

    var requirePin by remember { mutableStateOf(true) }
    var customPin by remember { mutableStateOf("") }
    var require2fa by remember { mutableStateOf(false) }
    var custom2faKey by remember { mutableStateOf("") }

    val fieldValues = remember { mutableStateMapOf<String, String>() }
    val customFields = remember { mutableStateMapOf<String, String>() }

    var hasExpiration by remember { mutableStateOf(false) }
    var expirationDate by remember { mutableStateOf("") }
    var expirationTime by remember { mutableStateOf("09:00") }
    var expirationAlertEnabled by remember { mutableStateOf(true) }
    var expirationAudioTone by remember { mutableStateOf("chime") }
    var customAudioUri by remember { mutableStateOf<String?>(null) }
    var customAudioName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "secret_alarm_${System.currentTimeMillis()}.mp3"
            val savedPath = viewModel.saveAudioFile(uri, fileName)
            customAudioUri = savedPath
            customAudioName = fileName
            expirationAudioTone = "custom"
            viewModel.showToast("🎵 Loaded custom audio: $fileName")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioTonePlayer.stop()
        }
    }

    val availableTones = remember {
        listOf(
            "chime" to "🔔 Chime",
            "radar" to "📡 Radar",
            "gentle" to "🎵 Gentle",
            "alarm" to "🚨 Alarm",
            "digital" to "⚡ Digital"
        )
    }

    fun pickExpirationDate() {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = if (expirationDate.isNotBlank()) sdf.parse(expirationDate) else null
            if (parsed != null) {
                cal.time = parsed
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 30)
            }
        } catch (_: Exception) {}
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                expirationDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickExpirationTime() {
        val cal = Calendar.getInstance()
        var h = 9
        var m = 0
        try {
            val parts = expirationTime.split(":")
            if (parts.size >= 2) {
                h = parts[0].toIntOrNull() ?: 9
                m = parts[1].toIntOrNull() ?: 0
            }
        } catch (_: Exception) {}
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                expirationTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            },
            h,
            m,
            true
        ).show()
    }

    var customKeyInput by remember { mutableStateOf("") }
    var customValueInput by remember { mutableStateOf("") }
    var showCustomInputRow by remember { mutableStateOf(false) }

    val fields = remember(category) {
        fieldValues.clear()
        getFieldsForCategory(category)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = VaultSurface,
        modifier = Modifier.testTag("add_secret_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Secret",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Encrypted with zero-knowledge on-device AES-256.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector
            var catExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = catExpanded,
                onExpandedChange = { catExpanded = it }
            ) {
                OutlinedTextField(
                    value = SECRET_CATEGORIES.find { it.id == category }?.let { "${it.icon} ${it.label}" } ?: category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false },
                    containerColor = VaultSurfaceVariant
                ) {
                    SECRET_CATEGORIES.filter { it.id != "all" }.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.icon} ${c.label}", color = TextPrimary) },
                            onClick = {
                                category = c.id
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Gmail, Chase Savings, Home WiFi") },
                modifier = Modifier.fillMaxWidth().testTag("secret_title_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic fields
            fields.forEach { f ->
                var isVisible by remember { mutableStateOf(!f.isSecret) }
                val currentVal = fieldValues[f.key] ?: ""

                OutlinedTextField(
                    value = currentVal,
                    onValueChange = { fieldValues[f.key] = it },
                    label = { Text(f.label) },
                    trailingIcon = {
                        if (f.isSecret) {
                            IconButton(onClick = { isVisible = !isVisible }) {
                                Icon(
                                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = TextMuted
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Custom Arbitrary Fields Section
            if (customFields.isNotEmpty()) {
                Text(
                    text = "CUSTOM FIELDS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                customFields.forEach { (k, v) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = v,
                            onValueChange = { customFields[k] = it },
                            label = { Text(k) },
                            modifier = Modifier.weight(1f),
                            colors = defaultTextFieldColors()
                        )
                        IconButton(onClick = { customFields.remove(k) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = UrgentRed)
                        }
                    }
                }
            }

            if (showCustomInputRow) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customKeyInput,
                        onValueChange = { customKeyInput = it },
                        label = { Text("Field Name") },
                        placeholder = { Text("e.g. PIN, Port") },
                        modifier = Modifier.weight(1f),
                        colors = defaultTextFieldColors()
                    )
                    OutlinedTextField(
                        value = customValueInput,
                        onValueChange = { customValueInput = it },
                        label = { Text("Value") },
                        modifier = Modifier.weight(1.2f),
                        colors = defaultTextFieldColors()
                    )
                    Button(
                        onClick = {
                            if (customKeyInput.isNotBlank()) {
                                customFields[customKeyInput.trim()] = customValueInput.trim()
                                customKeyInput = ""
                                customValueInput = ""
                                showCustomInputRow = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showCustomInputRow = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Custom Field (PIN, Port, Backup, etc.)", fontSize = 12.sp, color = theme.primaryLight)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Favorite Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("⭐ Mark as Favorite", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Pinned at the top of secrets list", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = favorite,
                    onCheckedChange = { favorite = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECURITY ACCESS GATES SECTION (PIN & 2FA)
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SECRET ACCESS CONTROL", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryLight)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (requirePin && require2fa) UrgentRedBg else if (requirePin) theme.primaryContainer else SafeGreenBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    requirePin && require2fa -> "🛡️ PIN + 2FA"
                                    requirePin -> "🔒 PIN"
                                    require2fa -> "🔑 2FA"
                                    else -> "🔓 Direct"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    requirePin && require2fa -> UrgentRed
                                    requirePin -> theme.primaryLight
                                    require2fa -> Color(0xFFFFB74D)
                                    else -> SafeGreen
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require PIN to View", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Demands PIN before decrypting values", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = requirePin,
                            onCheckedChange = { requirePin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (requirePin) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customPin,
                            onValueChange = { if (it.length <= 6) customPin = it },
                            label = { Text("Custom Secret PIN (Optional - Defaults to Master PIN)") },
                            placeholder = { Text("Leave blank for Master PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = defaultTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require 2FA Authentication", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Two-Factor Authentication code required", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = require2fa,
                            onCheckedChange = { require2fa = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (require2fa) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = custom2faKey,
                            onValueChange = { custom2faKey = it },
                            label = { Text("Custom 2FA Key (Optional - Defaults to Master '123456')") },
                            placeholder = { Text("Leave blank for Master 2FA code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = defaultTextFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expiration & Custom Audio Alert Card (Optional)
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (hasExpiration) theme.primary else BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏳ Expiration & Audio Alert", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x268B5CF6))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("OPTIONAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                                }
                            }
                            Text(
                                text = "Set expiration date/time with custom audio tone alert",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = hasExpiration,
                            onCheckedChange = { 
                                hasExpiration = it
                                if (it && expirationDate.isBlank()) {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_MONTH, 30)
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    expirationDate = sdf.format(cal.time)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (hasExpiration) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Date and Time Pickers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { pickExpirationDate() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Expiry Date", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = if (expirationDate.isNotBlank()) expirationDate else "Select Date",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { pickExpirationTime() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Alert Time", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = expirationTime,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audio Alert Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Custom Audio Alert Tone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Plays sound when expiration arrives", fontSize = 10.sp, color = TextMuted)
                            }
                            Switch(
                                checked = expirationAlertEnabled,
                                onCheckedChange = { expirationAlertEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                            )
                        }

                        if (expirationAlertEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select Sound Tone or Pick From Disk:", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableTones.forEach { (toneKey, toneLabel) ->
                                    val isSelected = expirationAudioTone == toneKey
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) theme.primary else VaultSurface)
                                            .border(1.dp, if (isSelected) theme.primary else BorderSubtle, RoundedCornerShape(8.dp))
                                            .clickable {
                                                expirationAudioTone = toneKey
                                                AudioTonePlayer.playTone(toneKey, 85)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = toneLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }

                                // Custom from Disk Chip
                                val isCustomSelected = expirationAudioTone == "custom"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCustomSelected) theme.primary else VaultSurface)
                                        .border(1.dp, if (isCustomSelected) theme.primary else BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (customAudioUri == null) {
                                                audioPickerLauncher.launch("audio/*")
                                            } else {
                                                expirationAudioTone = "custom"
                                                AudioTonePlayer.playCustomAudio(context, customAudioUri!!, 85)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (customAudioName != null) "📁 ${customAudioName!!.take(12)}…" else "📂 Disk File",
                                        fontSize = 11.sp,
                                        fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCustomSelected) Color.White else TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Audio from Disk Details & Picker Button
                            if (customAudioUri != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = VaultSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🎵 Custom Audio File", fontSize = 10.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                                            Text(customAudioName ?: "audio_custom.mp3", fontSize = 11.sp, color = TextPrimary, maxLines = 1)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedButton(
                                                onClick = { audioPickerLauncher.launch("audio/*") },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Change", fontSize = 10.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    if (AudioTonePlayer.isPlaying()) {
                                                        AudioTonePlayer.stop()
                                                    } else {
                                                        AudioTonePlayer.playCustomAudio(context, customAudioUri!!, 85)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(if (AudioTonePlayer.isPlaying()) "⏹ Stop" else "▶ Play", fontSize = 10.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    AudioTonePlayer.stop()
                                                    customAudioUri = null
                                                    customAudioName = null
                                                    expirationAudioTone = "chime"
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("✕", fontSize = 10.sp, color = UrgentRed)
                                            }
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { audioPickerLauncher.launch("audio/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📂 Select Audio File From Disk (MP3/WAV/AAC)", fontSize = 11.sp, color = theme.primaryLight)
                                }
                            }

                            if (expirationAudioTone != "custom") {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { AudioTonePlayer.playTone(expirationAudioTone, 85) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔊 Preview Alert Tone ($expirationAudioTone)", fontSize = 11.sp, color = theme.primaryLight)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        viewModel.showToast("⚠️ Title required")
                        return@Button
                    }
                    val subtitle = fieldValues["username"]
                        ?: fieldValues["bankName"]
                        ?: fieldValues["ssid"]
                        ?: fieldValues["cardNumber"]?.let { "•••• ${it.takeLast(4)}" }
                        ?: fieldValues["service"]
                        ?: ""

                    val newSecret = SecretItem(
                        id = UUID.randomUUID().toString(),
                        category = category,
                        title = title.trim(),
                        subtitle = subtitle,
                        favorite = favorite,
                        requirePin = requirePin,
                        require2fa = require2fa,
                        customPin = customPin.trim(),
                        custom2faKey = custom2faKey.trim(),
                        fields = fieldValues.toMap(),
                        customFields = customFields.toMap(),
                        notes = notes.trim(),
                        expirationDate = if (hasExpiration && expirationDate.isNotBlank()) expirationDate.trim() else null,
                        expirationTime = if (hasExpiration && expirationDate.isNotBlank()) expirationTime.trim() else null,
                        expirationAlertEnabled = hasExpiration && expirationAlertEnabled,
                        expirationAudioTone = expirationAudioTone,
                        expirationCustomAudioUri = if (hasExpiration && expirationAudioTone == "custom") customAudioUri else null,
                        expirationCustomAudioName = if (hasExpiration && expirationAudioTone == "custom") customAudioName else null
                    )
                    onSave(newSecret)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_secret_button")
            ) {
                Text("🔐 Save Encrypted", fontWeight = FontWeight.Bold)
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
fun SecretDetailSheet(
    secret: SecretItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    val effectiveRequirePin = remember(secret, settings.securityRegime) {
        secret.requirePin || settings.securityRegime == "high_security" || settings.securityRegime == "strict_enclave"
    }
    val effectiveRequire2fa = remember(secret, settings.securityRegime) {
        secret.require2fa || settings.securityRegime == "strict_enclave"
    }

    var isUnlocked by remember { mutableStateOf(!effectiveRequirePin && !effectiveRequire2fa) }
    var enteredPassword by remember { mutableStateOf("") }
    var entered2faKey by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // In-dialog editing of PIN / 2FA options
    var showSecurityEdit by remember { mutableStateOf(false) }
    var editRequirePin by remember { mutableStateOf(secret.requirePin) }
    var editRequire2fa by remember { mutableStateOf(secret.require2fa) }
    var editCustomPin by remember { mutableStateOf(secret.customPin) }
    var editCustom2faKey by remember { mutableStateOf(secret.custom2faKey) }

    var editHasExpiration by remember { mutableStateOf(!secret.expirationDate.isNullOrBlank()) }
    var editExpirationDate by remember { mutableStateOf(secret.expirationDate ?: "") }
    var editExpirationTime by remember { mutableStateOf(secret.expirationTime ?: "09:00") }
    var editExpirationAlertEnabled by remember { mutableStateOf(secret.expirationAlertEnabled) }
    var editExpirationAudioTone by remember { mutableStateOf(secret.expirationAudioTone) }
    var editCustomAudioUri by remember { mutableStateOf<String?>(secret.expirationCustomAudioUri) }
    var editCustomAudioName by remember { mutableStateOf(secret.expirationCustomAudioName) }

    // State for editing secret details (title, notes, favorite, fields, custom fields)
    var isEditingDetails by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(secret.title) }
    var editNotes by remember { mutableStateOf(secret.notes) }
    var editFavorite by remember { mutableStateOf(secret.favorite) }
    val editFields = remember { mutableStateMapOf<String, String>().apply { putAll(secret.fields) } }
    val editCustomFields = remember { mutableStateMapOf<String, String>().apply { putAll(secret.customFields) } }
    var editCustomKeyInput by remember { mutableStateOf("") }
    var editCustomValueInput by remember { mutableStateOf("") }
    var showEditCustomInputRow by remember { mutableStateOf(false) }

    val editAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "secret_alarm_${System.currentTimeMillis()}.mp3"
            val savedPath = viewModel.saveAudioFile(uri, fileName)
            editCustomAudioUri = savedPath
            editCustomAudioName = fileName
            editExpirationAudioTone = "custom"
            editExpirationAlertEnabled = true
            val updated = secret.copy(
                expirationAlertEnabled = true,
                expirationAudioTone = "custom",
                expirationCustomAudioUri = savedPath,
                expirationCustomAudioName = fileName
            )
            viewModel.updateSecret(updated)
            viewModel.showToast("🎵 Loaded custom audio from disk: $fileName")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioTonePlayer.stop()
        }
    }

    val availableTones = remember {
        listOf(
            "chime" to "🔔 Chime",
            "radar" to "📡 Radar",
            "gentle" to "🎵 Gentle",
            "alarm" to "🚨 Alarm",
            "digital" to "⚡ Digital"
        )
    }

    fun pickEditExpirationDate() {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = if (editExpirationDate.isNotBlank()) sdf.parse(editExpirationDate) else null
            if (parsed != null) {
                cal.time = parsed
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 30)
            }
        } catch (_: Exception) {}
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                editExpirationDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickEditExpirationTime() {
        val cal = Calendar.getInstance()
        var h = 9
        var m = 0
        try {
            val parts = editExpirationTime.split(":")
            if (parts.size >= 2) {
                h = parts[0].toIntOrNull() ?: 9
                m = parts[1].toIntOrNull() ?: 0
            }
        } catch (_: Exception) {}
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                editExpirationTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            },
            h,
            m,
            true
        ).show()
    }
    
    val fields = remember(secret.category) { getFieldsForCategory(secret.category) }
    val hiddenState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            fields.forEach { if (it.isSecret) put(it.key, true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        modifier = Modifier.testTag("secret_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryIcon(secret.category), fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isEditingDetails) editTitle else secret.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (secret.favorite || editFavorite) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("⭐", fontSize = 14.sp)
                            }
                        }
                        Text(
                            text = "${secret.category.replaceFirstChar { it.uppercase() }} · ${if (isEditingDetails) "Editing Details" else "Encrypted Vault"}",
                            fontSize = 11.sp,
                            color = if (isEditingDetails) theme.primaryLight else TextMuted
                        )
                    }
                }

                if (isUnlocked) {
                    OutlinedButton(
                        onClick = { isEditingDetails = !isEditingDetails },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isEditingDetails) "Cancel Edit" else "✏️ Edit Details", fontSize = 11.sp, color = theme.primaryLight)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isUnlocked) {
                // SECURITY GATE (PIN & 2FA VERIFICATION)
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.5.dp, theme.primary, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Locked Secret", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = when {
                                effectiveRequirePin && effectiveRequire2fa -> "Enforces BOTH PIN & 2FA Authentication to view"
                                effectiveRequirePin -> "Enter your 4-digit PIN to view secret credentials"
                                else -> "Enter 2FA security key to decrypt values"
                            },
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (effectiveRequirePin) {
                            OutlinedTextField(
                                value = enteredPassword,
                                onValueChange = { enteredPassword = it; authError = null },
                                label = { Text("PIN / Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextMuted)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = defaultTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (effectiveRequire2fa) {
                            OutlinedTextField(
                                value = entered2faKey,
                                onValueChange = { entered2faKey = it; authError = null },
                                label = { Text("2FA Security Code") },
                                placeholder = { Text("e.g. 123456") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = defaultTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x1AFFFFFF))
                                        .clickable {
                                            entered2faKey = secret.custom2faKey.ifBlank { settings.master2faKey }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "💡 Demo 2FA: ${secret.custom2faKey.ifBlank { settings.master2faKey }}",
                                        fontSize = 10.sp,
                                        color = SafeGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (authError != null) Text(authError!!, color = UrgentRed, fontSize = 12.sp, modifier = Modifier.padding(top=8.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val (verified, err) = viewModel.verifyPinAnd2fa(
                                    enteredPin = enteredPassword,
                                    entered2fa = entered2faKey,
                                    requirePin = effectiveRequirePin,
                                    require2fa = effectiveRequire2fa,
                                    customPin = secret.customPin,
                                    custom2faKey = secret.custom2faKey
                                )
                                if (verified) {
                                    isUnlocked = true
                                    viewModel.addAuditLog("SECRET_DECRYPT", "Decrypted '${secret.title}' (PIN/2FA verified)")
                                    viewModel.showToast("🔓 Secret decrypted")
                                } else {
                                    authError = err ?: "Incorrect authentication code."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock & View Credentials")
                        }
                    }
                }
            } else if (isEditingDetails) {
                // EDIT DETAILS FORM
                Card(
                    colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.primary, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("EDIT SECRET DETAILS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = defaultTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ Mark as Favorite", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Switch(
                                checked = editFavorite,
                                onCheckedChange = { editFavorite = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("FIELDS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                        Spacer(modifier = Modifier.height(4.dp))

                        fields.forEach { f ->
                            var isFieldVisible by remember { mutableStateOf(!f.isSecret) }
                            val currentVal = editFields[f.key] ?: ""

                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { editFields[f.key] = it },
                                label = { Text(f.label) },
                                trailingIcon = {
                                    if (f.isSecret) {
                                        IconButton(onClick = { isFieldVisible = !isFieldVisible }) {
                                            Icon(
                                                imageVector = if (isFieldVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = TextMuted
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = defaultTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (editCustomFields.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("CUSTOM FIELDS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                            Spacer(modifier = Modifier.height(4.dp))

                            editCustomFields.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = v,
                                        onValueChange = { editCustomFields[k] = it },
                                        label = { Text(k) },
                                        modifier = Modifier.weight(1f),
                                        colors = defaultTextFieldColors()
                                    )
                                    IconButton(onClick = { editCustomFields.remove(k) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = UrgentRed)
                                    }
                                }
                            }
                        }

                        if (showEditCustomInputRow) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editCustomKeyInput,
                                    onValueChange = { editCustomKeyInput = it },
                                    label = { Text("Field Name") },
                                    modifier = Modifier.weight(1f),
                                    colors = defaultTextFieldColors()
                                )
                                OutlinedTextField(
                                    value = editCustomValueInput,
                                    onValueChange = { editCustomValueInput = it },
                                    label = { Text("Value") },
                                    modifier = Modifier.weight(1.2f),
                                    colors = defaultTextFieldColors()
                                )
                                Button(
                                    onClick = {
                                        if (editCustomKeyInput.isNotBlank()) {
                                            editCustomFields[editCustomKeyInput.trim()] = editCustomValueInput.trim()
                                            editCustomKeyInput = ""
                                            editCustomValueInput = ""
                                            showEditCustomInputRow = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = { showEditCustomInputRow = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Custom Field", fontSize = 11.sp, color = theme.primaryLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editNotes,
                            onValueChange = { editNotes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = defaultTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (editTitle.isBlank()) {
                                    viewModel.showToast("⚠️ Title required")
                                    return@Button
                                }
                                val subtitle = editFields["username"]
                                    ?: editFields["bankName"]
                                    ?: editFields["ssid"]
                                    ?: editFields["cardNumber"]?.let { "•••• ${it.takeLast(4)}" }
                                    ?: editFields["service"]
                                    ?: ""

                                val updated = secret.copy(
                                    title = editTitle.trim(),
                                    subtitle = subtitle,
                                    favorite = editFavorite,
                                    fields = editFields.toMap(),
                                    customFields = editCustomFields.toMap(),
                                    notes = editNotes.trim()
                                )
                                viewModel.updateSecret(updated)
                                isEditingDetails = false
                                viewModel.showToast("✏️ Updated secret details successfully")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Secret Details", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Display standard fields
                fields.forEach { f ->
                val value = secret.fields[f.key] ?: ""
                if (value.isNotBlank()) {
                    val isHidden = hiddenState[f.key] == true

                    Card(
                        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = f.label.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (f.isSecret) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0x1AFFFFFF))
                                                .clickable { hiddenState[f.key] = !isHidden }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isHidden) "👁 Show" else "🙈 Hide",
                                                fontSize = 10.sp,
                                                color = theme.primaryLight
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(theme.primaryContainer)
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText(f.label, value))
                                                viewModel.showToast("📋 ${f.label} copied to clipboard")
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("📋 Copy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isHidden) "••••••••••••" else value,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Display custom fields if any
            if (secret.customFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("CUSTOM FIELDS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                Spacer(modifier = Modifier.height(4.dp))

                secret.customFields.forEach { (k, v) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(k.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.primaryContainer)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText(k, v))
                                            viewModel.showToast("📋 $k copied to clipboard")
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("📋 Copy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = v,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            if (secret.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📝 NOTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(secret.notes, fontSize = 13.sp, color = TextPrimary)
                    }
                }
            }

            // Expiration & Audio Alert Status Card
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        secret.isExpired -> UrgentRedBg
                        secret.isNearExpiration -> Color(0x26FFA500)
                        !secret.expirationDate.isNullOrBlank() -> Color(0x1F8B5CF6)
                        else -> VaultSurfaceVariant
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when {
                            secret.isExpired -> UrgentRed
                            secret.isNearExpiration -> Color(0xFFFFA500)
                            !secret.expirationDate.isNullOrBlank() -> Color(0xFF8B5CF6)
                            else -> BorderSubtle
                        },
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏳", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PASSWORD EXPIRATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    secret.isExpired -> UrgentRed
                                    secret.isNearExpiration -> Color(0xFFFFA500)
                                    !secret.expirationDate.isNullOrBlank() -> Color(0xFFA78BFA)
                                    else -> TextMuted
                                }
                            )
                        }

                        if (!secret.expirationDate.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            secret.isExpired -> UrgentRed
                                            secret.isNearExpiration -> Color(0xFFFFA500)
                                            else -> Color(0xFF8B5CF6)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when {
                                        secret.isExpired -> "EXPIRED"
                                        secret.isNearExpiration -> "${secret.daysUntilExpiration ?: 0}D LEFT"
                                        else -> "ACTIVE"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!secret.expirationDate.isNullOrBlank()) {
                        Text(
                            text = "Expires: ${secret.expirationDate} at ${secret.expirationTime ?: "09:00"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        if (secret.expirationAlertEnabled) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val isCustom = secret.expirationAudioTone == "custom"
                            val toneDisplay = if (isCustom && !secret.expirationCustomAudioName.isNullOrBlank()) {
                                "🎵 Disk Audio: ${secret.expirationCustomAudioName}"
                            } else {
                                "Alert Tone: ${secret.expirationAudioTone.replaceFirstChar { it.uppercase() }} 🔔"
                            }
                            Text(
                                text = toneDisplay,
                                fontSize = 11.sp,
                                color = if (isCustom) theme.primaryLight else TextMuted,
                                fontWeight = if (isCustom) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (AudioTonePlayer.isPlaying()) {
                                            AudioTonePlayer.stop()
                                        } else {
                                            if (secret.expirationAudioTone == "custom" && !secret.expirationCustomAudioUri.isNullOrBlank()) {
                                                AudioTonePlayer.playCustomAudio(context, secret.expirationCustomAudioUri, 85)
                                            } else {
                                                AudioTonePlayer.playTone(secret.expirationAudioTone, 85)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (AudioTonePlayer.isPlaying()) "⏹ Stop Sound" else "🔊 Test Sound", fontSize = 10.sp, color = theme.primaryLight)
                                }
                                OutlinedButton(
                                    onClick = { editAudioPickerLauncher.launch("audio/*") },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Text("📂 Audio from Disk", fontSize = 10.sp, color = theme.primaryLight)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No expiration date set.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            OutlinedButton(
                                onClick = { 
                                    showSecurityEdit = true
                                    editHasExpiration = true
                                    if (editExpirationDate.isBlank()) {
                                        val cal = Calendar.getInstance()
                                        cal.add(Calendar.DAY_OF_MONTH, 30)
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        editExpirationDate = sdf.format(cal.time)
                                    }
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+ Set Expiration", fontSize = 10.sp, color = theme.primaryLight)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // EDIT SECURITY PROTECTION & EXPIRATION FOR THIS SECRET
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSecurityEdit = !showSecurityEdit },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Security, PIN & Expiration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(if (showSecurityEdit) "▲ Hide" else "▼ Edit", fontSize = 11.sp, color = theme.primaryLight)
                    }

                    if (showSecurityEdit) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Require PIN to open", fontSize = 12.sp, color = TextPrimary)
                            Switch(
                                checked = editRequirePin,
                                onCheckedChange = { editRequirePin = it }
                            )
                        }
                        if (editRequirePin) {
                            OutlinedTextField(
                                value = editCustomPin,
                                onValueChange = { editCustomPin = it },
                                label = { Text("Custom Secret PIN (or blank for Master PIN)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = defaultTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Require 2FA to open", fontSize = 12.sp, color = TextPrimary)
                            Switch(
                                checked = editRequire2fa,
                                onCheckedChange = { editRequire2fa = it }
                            )
                        }
                        if (editRequire2fa) {
                            OutlinedTextField(
                                value = editCustom2faKey,
                                onValueChange = { editCustom2faKey = it },
                                label = { Text("Custom 2FA Token (or blank for Master '123456')") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = defaultTextFieldColors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Expiration Settings in Edit Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Password Expiration Alert", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Set expiry schedule & custom audio tone", fontSize = 10.sp, color = TextMuted)
                            }
                            Switch(
                                checked = editHasExpiration,
                                onCheckedChange = { 
                                    editHasExpiration = it
                                    if (it && editExpirationDate.isBlank()) {
                                        val cal = Calendar.getInstance()
                                        cal.add(Calendar.DAY_OF_MONTH, 30)
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        editExpirationDate = sdf.format(cal.time)
                                    }
                                }
                            )
                        }

                        if (editHasExpiration) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { pickEditExpirationDate() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Date", fontSize = 9.sp, color = TextMuted)
                                        Text(if (editExpirationDate.isNotBlank()) editExpirationDate else "Pick Date", fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { pickEditExpirationTime() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Time", fontSize = 9.sp, color = TextMuted)
                                        Text(editExpirationTime, fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sound Alert Tone", fontSize = 11.sp, color = TextPrimary)
                                Switch(
                                    checked = editExpirationAlertEnabled,
                                    onCheckedChange = { editExpirationAlertEnabled = it }
                                )
                            }

                            if (editExpirationAlertEnabled) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableTones.forEach { (toneKey, toneLabel) ->
                                        val isSel = editExpirationAudioTone == toneKey
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) theme.primary else VaultSurface)
                                                .border(1.dp, if (isSel) theme.primary else BorderSubtle, RoundedCornerShape(6.dp))
                                                .clickable {
                                                    editExpirationAudioTone = toneKey
                                                    AudioTonePlayer.playTone(toneKey, 85)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = toneLabel,
                                                fontSize = 10.sp,
                                                color = if (isSel) Color.White else TextPrimary
                                            )
                                        }
                                    }

                                    // Custom from Disk Chip
                                    val isEditCustomSelected = editExpirationAudioTone == "custom"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isEditCustomSelected) theme.primary else VaultSurface)
                                            .border(1.dp, if (isEditCustomSelected) theme.primary else BorderSubtle, RoundedCornerShape(6.dp))
                                            .clickable {
                                                if (editCustomAudioUri == null) {
                                                    editAudioPickerLauncher.launch("audio/*")
                                                } else {
                                                    editExpirationAudioTone = "custom"
                                                    AudioTonePlayer.playCustomAudio(context, editCustomAudioUri!!, 85)
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (editCustomAudioName != null) "📁 ${editCustomAudioName!!.take(10)}…" else "📂 Disk File",
                                            fontSize = 10.sp,
                                            fontWeight = if (isEditCustomSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isEditCustomSelected) Color.White else TextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Disk Audio Card or Selection button in Edit Dialog
                                if (editCustomAudioUri != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = VaultSurface),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("🎵 Custom Audio File", fontSize = 10.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                                                Text(editCustomAudioName ?: "audio_custom.mp3", fontSize = 11.sp, color = TextPrimary, maxLines = 1)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                OutlinedButton(
                                                    onClick = { editAudioPickerLauncher.launch("audio/*") },
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("Change", fontSize = 10.sp)
                                                }
                                                Button(
                                                    onClick = {
                                                        if (AudioTonePlayer.isPlaying()) {
                                                            AudioTonePlayer.stop()
                                                        } else {
                                                            AudioTonePlayer.playCustomAudio(context, editCustomAudioUri!!, 85)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(if (AudioTonePlayer.isPlaying()) "⏹ Stop" else "▶ Play", fontSize = 10.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        AudioTonePlayer.stop()
                                                        editCustomAudioUri = null
                                                        editCustomAudioName = null
                                                        editExpirationAudioTone = "chime"
                                                    },
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("✕", fontSize = 10.sp, color = UrgentRed)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { editAudioPickerLauncher.launch("audio/*") },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("📂 Select Audio File From Disk (MP3/WAV/AAC)", fontSize = 11.sp, color = theme.primaryLight)
                                    }
                                }

                                if (editExpirationAudioTone != "custom") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { AudioTonePlayer.playTone(editExpirationAudioTone, 85) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("🔊 Preview Alert Tone ($editExpirationAudioTone)", fontSize = 11.sp, color = theme.primaryLight)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val updated = secret.copy(
                                    requirePin = editRequirePin,
                                    require2fa = editRequire2fa,
                                    customPin = editCustomPin.trim(),
                                    custom2faKey = editCustom2faKey.trim(),
                                    expirationDate = if (editHasExpiration && editExpirationDate.isNotBlank()) editExpirationDate.trim() else null,
                                    expirationTime = if (editHasExpiration && editExpirationDate.isNotBlank()) editExpirationTime.trim() else null,
                                    expirationAlertEnabled = editHasExpiration && editExpirationAlertEnabled,
                                    expirationAudioTone = editExpirationAudioTone,
                                    expirationCustomAudioUri = if (editHasExpiration && editExpirationAudioTone == "custom") editCustomAudioUri else null,
                                    expirationCustomAudioName = if (editHasExpiration && editExpirationAudioTone == "custom") editCustomAudioName else null
                                )
                                viewModel.updateSecret(updated)
                                showSecurityEdit = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Security & Expiration Settings")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close", color = TextMuted)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRedBg, contentColor = UrgentRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("delete_secret_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
