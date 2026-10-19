package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppSettings
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant

@Composable
fun ProfileDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (
        username: String,
        organization: String,
        department: String,
        employeeId: String,
        securityClearance: String,
        workEmail: String
    ) -> Unit
) {
    val theme = LocalAppTheme.current
    var username by remember { mutableStateOf(settings.username) }
    var organization by remember { mutableStateOf(settings.organization) }
    var department by remember { mutableStateOf(settings.department) }
    var employeeId by remember { mutableStateOf(settings.employeeId) }
    var securityClearance by remember { mutableStateOf(settings.securityClearance) }
    var workEmail by remember { mutableStateOf(settings.workEmail) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = theme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("profile_dialog")
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(theme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (username.isNotBlank()) username.take(1).uppercase() else "U",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryLight
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("User Profile & Identity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Manage user credentials & identity", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Badge Summary Chip
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderAccent, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VERIFIED SECURITY CLEARANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FIPS 140-3 Hardware Enclave · Zero-knowledge local device cryptography.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Username / Employee Name
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryLight) },
                    modifier = Modifier.fillMaxWidth().testTag("profile_username_input"),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Organization
                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text("Organization / Workspace") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = theme.primaryLight) },
                    modifier = Modifier.fillMaxWidth().testTag("profile_org_input"),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Employee ID
                OutlinedTextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    label = { Text("Employee ID / Badge Number") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = theme.primaryLight) },
                    placeholder = { Text("e.g. EMP-9482-SEC") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_emp_id_input"),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Department
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Business Unit") },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = theme.primaryLight) },
                    placeholder = { Text("e.g. Cyber Defense & Cloud Operations") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Security Clearance Level
                OutlinedTextField(
                    value = securityClearance,
                    onValueChange = { securityClearance = it },
                    label = { Text("Security Clearance Level") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryLight) },
                    placeholder = { Text("e.g. Level 4 - Confidential Enclave") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Work Email
                OutlinedTextField(
                    value = workEmail,
                    onValueChange = { workEmail = it },
                    label = { Text("Corporate Work Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = theme.primaryLight) },
                    placeholder = { Text("e.g. name@company.com") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextMuted)
                    }

                    Button(
                        onClick = {
                            if (username.isNotBlank()) {
                                onSave(
                                    username.trim(),
                                    organization.trim(),
                                    department.trim(),
                                    employeeId.trim(),
                                    securityClearance.trim(),
                                    workEmail.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("save_profile_btn")
                    ) {
                        Text("Save Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
