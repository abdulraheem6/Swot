package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.EmergencyInfo
import com.example.ui.VaultViewModel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultBg
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyViewSheet(
    info: EmergencyInfo,
    onDismiss: () -> Unit,
    onEditClicked: () -> Unit,
    viewModel: VaultViewModel,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VaultSurface,
        modifier = Modifier.testTag("emergency_view_sheet")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(UrgentRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚨", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Emergency Info",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Accessible without PIN for first responders",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!info.isConfigured) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚨", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No emergency info set yet",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add blood group, allergies, medications and trusted contacts.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onEditClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Set Up Emergency Info", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Blood group & Allergies top row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (info.blood.isNotBlank()) {
                        EmergencyFieldCard(
                            label = "Blood Group",
                            value = info.blood,
                            isBig = true,
                            accentColor = UrgentRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (info.organ.isNotBlank()) {
                        EmergencyFieldCard(
                            label = "Organ Donor",
                            value = info.organ,
                            isBig = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (info.allergies.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EmergencyFieldCard(
                        label = "⚠️ Critical Allergies",
                        value = info.allergies,
                        accentColor = UrgentRed
                    )
                }

                if (info.conditions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EmergencyFieldCard(label = "Medical Conditions", value = info.conditions)
                }

                if (info.meds.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EmergencyFieldCard(label = "Current Medications", value = info.meds)
                }

                if (info.contactName.isNotBlank() || info.contactPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📞 EMERGENCY CONTACT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = info.contactName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (info.contactPhone.isNotBlank()) {
                                Text(
                                    text = info.contactPhone,
                                    fontSize = 13.sp,
                                    color = VaultPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${info.contactPhone}")
                                            }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Contact Phone", info.contactPhone))
                                            viewModel.showToast("📋 Phone copied to clipboard")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (info.doctor.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EmergencyFieldCard(label = "🏥 Doctor / Hospital", value = info.doctor)
                }

                if (info.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    EmergencyFieldCard(label = "Additional Notes", value = info.notes)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEditClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRedBg, contentColor = UrgentRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_emergency_btn")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✏️ Edit Emergency Info", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmergencyFieldCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBig: Boolean = false,
    accentColor: Color? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor ?: TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = if (isBig) 24.sp else 14.sp,
                fontWeight = if (isBig) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isBig) (accentColor ?: UrgentRed) else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyEditSheet(
    initialInfo: EmergencyInfo,
    onDismiss: () -> Unit,
    onSave: (EmergencyInfo) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var blood by remember { mutableStateOf(initialInfo.blood) }
    var allergies by remember { mutableStateOf(initialInfo.allergies) }
    var conditions by remember { mutableStateOf(initialInfo.conditions) }
    var meds by remember { mutableStateOf(initialInfo.meds) }
    var contactName by remember { mutableStateOf(initialInfo.contactName) }
    var contactPhone by remember { mutableStateOf(initialInfo.contactPhone) }
    var doctor by remember { mutableStateOf(initialInfo.doctor) }
    var organ by remember { mutableStateOf(initialInfo.organ) }
    var notes by remember { mutableStateOf(initialInfo.notes) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VaultSurface,
        modifier = Modifier.testTag("emergency_edit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Edit Emergency Info",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Visible from lock screen without PIN for emergencies.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = blood,
                onValueChange = { blood = it },
                label = { Text("Blood Group (e.g. O+, A-, B+)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("Allergies (e.g. Penicillin, peanuts)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = conditions,
                onValueChange = { conditions = it },
                label = { Text("Medical Conditions (e.g. Asthma, Diabetes)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = meds,
                onValueChange = { meds = it },
                label = { Text("Current Medications") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Emergency Contact Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Emergency Contact Phone") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text("Doctor / Hospital") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = organ,
                onValueChange = { organ = it },
                label = { Text("Organ Donor (Yes / No)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Critical Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSave(
                        EmergencyInfo(
                            blood = blood.trim(),
                            allergies = allergies.trim(),
                            conditions = conditions.trim(),
                            meds = meds.trim(),
                            contactName = contactName.trim(),
                            contactPhone = contactPhone.trim(),
                            doctor = doctor.trim(),
                            organ = organ.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_emergency_button")
            ) {
                Text("Save Emergency Info", fontWeight = FontWeight.Bold)
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

@Composable
fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = VaultPrimary,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = VaultPrimary,
    unfocusedLabelColor = TextMuted,
    focusedContainerColor = VaultSurfaceVariant,
    unfocusedContainerColor = VaultSurfaceVariant
)
