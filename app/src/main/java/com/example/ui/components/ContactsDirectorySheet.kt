package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MemberContact
import com.example.ui.VaultViewModel
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultPrimaryLight
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import java.util.UUID

data class ContactCategoryInfo(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
)

val CONTACT_CATEGORIES = listOf(
    ContactCategoryInfo("family", "Family Member", "👨‍👩‍👧", Color(0xFF4DFF91), Color(0x264DFF91)),
    ContactCategoryInfo("electrician", "Electrician", "⚡", Color(0xFFFFB84D), Color(0x26FFB84D)),
    ContactCategoryInfo("plumber", "Plumber", "🔧", Color(0xFF4DBBFF), Color(0x264DBBFF)),
    ContactCategoryInfo("barber", "Barber / Salon", "💈", Color(0xFFE040FB), Color(0x26E040FB)),
    ContactCategoryInfo("doctor", "Doctor / Medical", "🩺", Color(0xFFFF5252), Color(0x26FF5252)),
    ContactCategoryInfo("mechanic", "Auto Mechanic", "🚗", Color(0xFFFF9100), Color(0x26FF9100)),
    ContactCategoryInfo("contractor", "Contractor", "🔨", Color(0xFF00E676), Color(0x2600E676)),
    ContactCategoryInfo("other", "Service / Other", "👤", Color(0xFF90CAF9), Color(0x2690CAF9))
)

fun getContactCategoryInfo(catId: String): ContactCategoryInfo {
    return CONTACT_CATEGORIES.find { it.id.equals(catId, ignoreCase = true) }
        ?: CONTACT_CATEGORIES.last()
}

@Composable
fun ContactsDirectorySection(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val allContacts by viewModel.contacts.collectAsState()
    val activeCategory by viewModel.contactCategoryFilter.collectAsState()

    val filteredContacts = if (activeCategory == "all") {
        allContacts
    } else {
        allContacts.filter { it.category.equals(activeCategory, ignoreCase = true) }
    }

    var editingContact by remember { mutableStateOf<MemberContact?>(null) }

    if (editingContact != null) {
        EditContactSheet(
            contact = editingContact!!,
            onDismiss = { editingContact = null },
            onSave = { updated ->
                viewModel.updateContact(updated)
                editingContact = null
            },
            viewModel = viewModel
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👥 Service & Family Directory",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${allContacts.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryLight
                            )
                        }
                    }
                    Text(
                        text = "Plumbers, electricians, family, barbers & quick contacts.",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }

                Button(
                    onClick = { viewModel.openAddContactDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_contact_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                val isAll = activeCategory == "all"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAll) theme.primary else theme.surface)
                        .border(1.dp, if (isAll) theme.primaryLight else theme.borderSubtle, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setContactCategoryFilter("all") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "All (${allContacts.size})",
                        fontSize = 11.sp,
                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAll) Color.White else theme.textPrimary
                    )
                }

                CONTACT_CATEGORIES.forEach { cat ->
                    val count = allContacts.count { it.category.equals(cat.id, ignoreCase = true) }
                    val isSelected = activeCategory == cat.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) cat.bgColor else theme.surface)
                            .border(1.dp, if (isSelected) cat.color else theme.borderSubtle, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setContactCategoryFilter(cat.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${cat.name} ($count)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) cat.color else theme.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredContacts.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No contacts in this category.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Tap 'Add' to register your plumber, electrician, family member or barber.",
                            fontSize = 11.sp,
                            color = theme.textMuted
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredContacts.forEach { contact ->
                        val catInfo = getContactCategoryInfo(contact.category)
                        var isExpanded by remember { mutableStateOf(false) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar circle with category emoji
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(catInfo.bgColor)
                                            .border(1.dp, catInfo.color.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.avatarEmoji.ifBlank { catInfo.emoji },
                                            fontSize = 20.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = contact.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.textPrimary,
                                                maxLines = 1
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(catInfo.bgColor)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = catInfo.name,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = catInfo.color
                                                )
                                            }
                                        }

                                        if (contact.specialty.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = contact.specialty,
                                                fontSize = 11.sp,
                                                color = theme.primaryLight,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        }

                                        if (contact.phone.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "📞 ${contact.phone}",
                                                fontSize = 12.sp,
                                                color = theme.textMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Top-right Edit & Delete Buttons with clean spacing
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(theme.surfaceVariant)
                                                .border(1.dp, theme.borderSubtle, RoundedCornerShape(8.dp))
                                                .clickable { editingContact = contact },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Contact",
                                                tint = theme.primaryLight,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(UrgentRedBg)
                                                .border(1.dp, UrgentRed.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                .clickable { viewModel.deleteContact(contact.id, contact.name) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Contact",
                                                tint = UrgentRed,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                if (contact.notes.isNotBlank() || contact.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.surfaceVariant)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        if (contact.address.isNotBlank()) {
                                            Text(
                                                text = "📍 ${contact.address}",
                                                fontSize = 11.sp,
                                                color = theme.textMuted
                                            )
                                        }
                                        if (contact.notes.isNotBlank()) {
                                            Text(
                                                text = "📝 ${contact.notes}",
                                                fontSize = 11.sp,
                                                color = theme.textMuted
                                            )
                                        }
                                    }
                                }

                                // Bottom Row: Call and WhatsApp buttons
                                if (contact.phone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreenBg),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Call,
                                                contentDescription = "Call",
                                                tint = SafeGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                        }

                                        Button(
                                            onClick = {
                                                try {
                                                    val url = "https://api.whatsapp.com/send?phone=${contact.phone}&text=Hello%20${contact.name}"
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}"))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2600E676)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Message,
                                                contentDescription = "WhatsApp Message",
                                                tint = SafeGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Message", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(
    onDismiss: () -> Unit,
    onSave: (MemberContact) -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("family") }
    var specialty by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var avatarEmoji by remember { mutableStateOf("👨‍👩‍👧") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        modifier = Modifier.testTag("add_contact_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Contact Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Text(
                text = "Register plumbers, electricians, barbers, family & service contacts.",
                fontSize = 11.sp,
                color = theme.textMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            val currentCat = getContactCategoryInfo(category)

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${currentCat.emoji} ${currentCat.name}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Contact Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    containerColor = theme.surfaceVariant
                ) {
                    CONTACT_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.emoji} ${cat.name}", color = theme.textPrimary) },
                            onClick = {
                                category = cat.id
                                avatarEmoji = cat.emoji
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Contact Name") },
                placeholder = { Text("e.g. David Miller, Apex Electric, Marco's Barbershop") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty / Relationship") },
                placeholder = { Text("e.g. Master Electrician, Brother, Hair Stylist, 24/7 Plumber") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                placeholder = { Text("e.g. +1 (555) 019-2834") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = theme.primaryLight)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_phone_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Optional)") },
                placeholder = { Text("e.g. service@contractor.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Service Area (Optional)") },
                placeholder = { Text("e.g. 120 Main St, Suite 4B") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Availability") },
                placeholder = { Text("e.g. Has spare keys, available on weekends, license #89423") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showToast("⚠️ Contact name required")
                        return@Button
                    }
                    val newContact = MemberContact(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        category = category,
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        notes = notes.trim(),
                        specialty = specialty.trim(),
                        avatarEmoji = avatarEmoji
                    )
                    onSave(newContact)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_contact_btn")
            ) {
                Text("Save Contact Profile", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = theme.textMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactSheet(
    contact: MemberContact,
    onDismiss: () -> Unit,
    onSave: (MemberContact) -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current

    var name by remember { mutableStateOf(contact.name) }
    var category by remember { mutableStateOf(contact.category) }
    var specialty by remember { mutableStateOf(contact.specialty) }
    var phone by remember { mutableStateOf(contact.phone) }
    var email by remember { mutableStateOf(contact.email) }
    var address by remember { mutableStateOf(contact.address) }
    var notes by remember { mutableStateOf(contact.notes) }
    var avatarEmoji by remember { mutableStateOf(contact.avatarEmoji) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        modifier = Modifier.testTag("edit_contact_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Edit Contact Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Text(
                text = "Update directory entry details for ${contact.name}.",
                fontSize = 11.sp,
                color = theme.textMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            val currentCat = getContactCategoryInfo(category)

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${currentCat.emoji} ${currentCat.name}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Contact Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = defaultTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    containerColor = theme.surfaceVariant
                ) {
                    CONTACT_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.emoji} ${cat.name}", color = theme.textPrimary) },
                            onClick = {
                                category = cat.id
                                avatarEmoji = cat.emoji
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Contact Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_contact_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty / Relationship") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = theme.primaryLight)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_contact_phone_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Service Area (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Availability") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showToast("⚠️ Contact name required")
                        return@Button
                    }
                    val updated = contact.copy(
                        name = name.trim(),
                        category = category,
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        notes = notes.trim(),
                        specialty = specialty.trim(),
                        avatarEmoji = avatarEmoji
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("update_contact_btn")
            ) {
                Text("Update Contact Profile", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = theme.textMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
