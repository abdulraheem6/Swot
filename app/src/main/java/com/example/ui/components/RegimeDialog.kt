package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.VaultViewModel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant

@Composable
fun RegimeBadge(
    regime: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val (label, icon, bg, textColor, borderColor) = when (regime) {
        "strict_enclave" -> Quintuple(
            "STRICT ENCLAVE (PIN+2FA)",
            "🛡️",
            UrgentRedBg,
            UrgentRed,
            UrgentRed.copy(alpha = 0.5f)
        )
        "high_security" -> Quintuple(
            "HIGH DEFENSE (PIN ALL)",
            "⚡",
            Color(0x26FFAA00),
            Color(0xFFFFB74D),
            Color(0xFFFFB74D).copy(alpha = 0.5f)
        )
        else -> Quintuple(
            "STANDARD REGIME",
            "🔒",
            theme.primaryContainer,
            theme.primaryLight,
            theme.primary.copy(alpha = 0.4f)
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

@Composable
fun RegimeDialog(
    viewModel: VaultViewModel,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    val settings by viewModel.settings.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val secrets by viewModel.secrets.collectAsState()
    val items by viewModel.items.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedRegime by remember { mutableStateOf(settings.securityRegime) }
    var resetTimeInput by remember { mutableStateOf(settings.timeOfReset) }
    var master2faInput by remember { mutableStateOf(settings.master2faKey) }

    val photosAndVideosCount = remember(documents) { documents.count { it.category == "photo" || it.isVideo || it.category == "video" } }
    val regularDocsCount = remember(documents) { documents.count { it.category != "photo" && !it.isVideo && it.category != "video" } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("regime_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Security Regime & Defense",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "CyberVault access governance policy",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time of Reset info card (Answers user question)
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TIME OF RESET", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFB74D))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SafeGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(settings.timeOfReset, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The Time of Reset (${settings.timeOfReset}) is the automated daily re-synchronization cycle. At this hour, temporary decryption tokens expire, defense access counts refresh, and strict biometric/PIN re-authentication is re-armed.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("00:00 UTC", "06:00 UTC", "12:00 UTC", "Midnight").forEach { t ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (settings.timeOfReset == t) theme.primaryContainer else Color(0x14FFFFFF))
                                        .clickable {
                                            viewModel.setTimeOfReset(t)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(t, fontSize = 10.sp, color = if (settings.timeOfReset == t) theme.primaryLight else TextMuted, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("CHOOSE ACTIVE REGIME", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryLight)
                Spacer(modifier = Modifier.height(6.dp))

                // Regime 1: Standard
                RegimeSelectionCard(
                    title = "Standard Regime (Balanced)",
                    badge = "STANDARD",
                    badgeColor = theme.primaryLight,
                    badgeBg = theme.primaryContainer,
                    description = "PIN & 2FA are configurable individually per photo, document, secret, or video.",
                    isSelected = selectedRegime == "standard",
                    onClick = {
                        selectedRegime = "standard"
                        viewModel.setSecurityRegime("standard")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Regime 2: High Defense
                RegimeSelectionCard(
                    title = "High Defense Regime (Fortified)",
                    badge = "HIGH DEFENSE",
                    badgeColor = Color(0xFFFFB74D),
                    badgeBg = Color(0x26FFAA00),
                    description = "PIN is enforced across ALL stored files & secrets. 2FA is strongly recommended.",
                    isSelected = selectedRegime == "high_security",
                    onClick = {
                        selectedRegime = "high_security"
                        viewModel.setSecurityRegime("high_security")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Regime 3: Strict Enclave
                RegimeSelectionCard(
                    title = "Strict Enclave Regime (Maximum)",
                    badge = "STRICT ENCLAVE",
                    badgeColor = UrgentRed,
                    badgeBg = UrgentRedBg,
                    description = "MAXIMUM SECURITY: ALL photos, documents, secrets, and videos MUST open with BOTH PIN & 2FA!",
                    isSelected = selectedRegime == "strict_enclave",
                    onClick = {
                        selectedRegime = "strict_enclave"
                        viewModel.setSecurityRegime("strict_enclave")
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Real-Time Protection Audit Counts
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ACTIVE PROTECTION INVENTORY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryLight)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🖼️/🎥 Photos & Videos Stored:", fontSize = 12.sp, color = TextMuted)
                            Text("$photosAndVideosCount items (PIN/2FA Ready)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("📄 Documents Stored:", fontSize = 12.sp, color = TextMuted)
                            Text("$regularDocsCount items (PIN/2FA Ready)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🔐 Secrets & Credentials:", fontSize = 12.sp, color = TextMuted)
                            Text("${secrets.size} items (PIN/2FA Ready)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Master 2FA Code Display / Config
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MASTER 2FA TOKEN / KEY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(settings.master2faKey, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                            Text("Default 6-digit code for 2FA decryption challenges", fontSize = 10.sp, color = TextMuted)
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(settings.master2faKey))
                                viewModel.showToast("📋 Copied Master 2FA Code: ${settings.master2faKey}")
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy 2FA", tint = theme.primaryLight)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mass Enforce Button
                Button(
                    onClick = {
                        viewModel.enforceRegimeOnAll(requirePin = true, require2fa = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("enforce_all_btn")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⚡ Enforce PIN & 2FA on All Stored Media", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun RegimeSelectionCard(
    title: String,
    badge: String,
    badgeColor: Color,
    badgeBg: Color,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) theme.surfaceVariant else VaultSurface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.primary else BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = badgeColor)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
        }
    }
}
