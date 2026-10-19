package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioSchedule
import com.example.data.MemberContact
import com.example.data.SecureDocument
import com.example.data.SecretItem
import com.example.data.VaultItem
import com.example.ui.VaultViewModel
import com.example.ui.screens.EditScheduleSheet
import com.example.ui.screens.SecretItemRow
import com.example.ui.screens.VaultItemRow
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.WarningYellow
import com.example.ui.theme.WarningYellowBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailSheet(
    categoryId: String,
    onDismiss: () -> Unit,
    viewModel: VaultViewModel
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val items by viewModel.items.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val secrets by viewModel.secrets.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val emergency by viewModel.emergency.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    var searchQuery by remember(categoryId) { mutableStateOf("") }
    var editingSchedule by remember { mutableStateOf<AudioSchedule?>(null) }
    var editingContact by remember { mutableStateOf<MemberContact?>(null) }
    var selectedServiceCategoryFilter by remember { mutableStateOf("all") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.background,
        modifier = Modifier.testTag("category_detail_sheet_$categoryId")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            when (categoryId) {
                // KITCHEN & RECIPES TILE (Warm Cream & Curry-Orange)
                "kitchen" -> {
                    com.example.ui.screens.kitchen.KitchenContent(
                        viewModel = viewModel,
                        onDismiss = onDismiss
                    )
                }

                // MONEY & FINANCE TILE
                "money" -> {
                    MoneyFinanceContent(
                        viewModel = viewModel,
                        onDismiss = onDismiss
                    )
                }

                // 1. FAMILY MEMBERS TILE
                "family" -> {
                    val familyContacts = contacts.filter { it.category.equals("family", ignoreCase = true) }
                    val filteredFamily = remember(familyContacts, searchQuery) {
                        if (searchQuery.isBlank()) familyContacts
                        else familyContacts.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.phone.contains(searchQuery, ignoreCase = true) ||
                            it.specialty.contains(searchQuery, ignoreCase = true) ||
                            it.notes.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    CategoryHeaderSection(
                        title = "Family Members",
                        subtitle = "Personal contacts, loved ones & emergency ICE relations",
                        badge = "${familyContacts.size} Listed",
                        icon = Icons.Default.People,
                        accentColor = Color(0xFF10B981),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search family by name, relation or phone...",
                        addLabel = "Add Family",
                        onAddClick = { viewModel.openAddContactDialog() },
                        onDismiss = onDismiss
                    )

                    if (filteredFamily.isEmpty()) {
                        EmptyCategoryState(
                            emoji = if (searchQuery.isBlank()) "👨‍👩‍👧‍👦" else "🔍",
                            message = if (searchQuery.isBlank()) "No family members added yet" else "No family members match '$searchQuery'",
                            hint = if (searchQuery.isBlank()) "Register family members with phone numbers for rapid dialing & ICE safety" else "Try clearing your search query",
                            actionText = if (searchQuery.isBlank()) "+ Add Family Member" else "Clear Search",
                            onAction = {
                                if (searchQuery.isBlank()) viewModel.openAddContactDialog()
                                else searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(filteredFamily, key = { it.id }) { contact ->
                                ContactDetailCard(
                                    contact = contact,
                                    context = context,
                                    onEdit = { editingContact = contact },
                                    onDelete = { viewModel.deleteContact(contact.id, contact.name) }
                                )
                            }
                        }
                    }
                }

                // 2. SERVICES DIRECTORY TILE
                "services" -> {
                    val serviceContacts = contacts.filter { !it.category.equals("family", ignoreCase = true) }
                    val categoryFiltered = if (selectedServiceCategoryFilter == "all") {
                        serviceContacts
                    } else {
                        serviceContacts.filter { it.category.equals(selectedServiceCategoryFilter, ignoreCase = true) }
                    }
                    val filteredServices = remember(categoryFiltered, searchQuery) {
                        if (searchQuery.isBlank()) categoryFiltered
                        else categoryFiltered.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.specialty.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true) ||
                            it.phone.contains(searchQuery, ignoreCase = true) ||
                            it.notes.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    CategoryHeaderSection(
                        title = "Services Directory",
                        subtitle = "Plumbers, electricians, doctors, mechanics, barbers & pros",
                        badge = "${serviceContacts.size} Providers",
                        icon = Icons.Default.Build,
                        accentColor = Color(0xFF0284C7),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search services by trade, name or phone...",
                        addLabel = "Add Service",
                        onAddClick = { viewModel.openAddContactDialog() },
                        onDismiss = onDismiss
                    )

                    // Service Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All Chip
                        val isAllSelected = selectedServiceCategoryFilter == "all"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAllSelected) theme.primary else theme.surface)
                                .border(1.dp, if (isAllSelected) theme.primaryLight else theme.borderSubtle, RoundedCornerShape(20.dp))
                                .clickable { selectedServiceCategoryFilter = "all" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "All (${serviceContacts.size})",
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) Color.White else theme.textPrimary
                            )
                        }

                        // Filter chips for professional trades
                        CONTACT_CATEGORIES.filter { it.id != "family" }.forEach { cat ->
                            val count = serviceContacts.count { it.category.equals(cat.id, ignoreCase = true) }
                            val isSelected = selectedServiceCategoryFilter == cat.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) cat.bgColor else theme.surface)
                                    .border(1.dp, if (isSelected) cat.color else theme.borderSubtle, RoundedCornerShape(20.dp))
                                    .clickable { selectedServiceCategoryFilter = cat.id }
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

                    if (filteredServices.isEmpty()) {
                        EmptyCategoryState(
                            emoji = if (searchQuery.isBlank()) "🔧" else "🔍",
                            message = if (searchQuery.isBlank()) "No service providers found" else "No services match '$searchQuery'",
                            hint = if (searchQuery.isBlank()) "Register your local plumber, electrician, doctor or handyman" else "Try clearing your search query",
                            actionText = if (searchQuery.isBlank()) "+ Add Service Contact" else "Clear Search",
                            onAction = {
                                if (searchQuery.isBlank()) viewModel.openAddContactDialog()
                                else searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(filteredServices, key = { it.id }) { contact ->
                                ContactDetailCard(
                                    contact = contact,
                                    context = context,
                                    onEdit = { editingContact = contact },
                                    onDelete = { viewModel.deleteContact(contact.id, contact.name) }
                                )
                            }
                        }
                    }
                }

                // 3. COMBINED DIRECTORY (Fallback)
                "directory" -> {
                    val filteredAll = remember(contacts, searchQuery) {
                        if (searchQuery.isBlank()) contacts
                        else contacts.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.phone.contains(searchQuery, ignoreCase = true) ||
                            it.specialty.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    CategoryHeaderSection(
                        title = "Directory",
                        subtitle = "All family members, ICE & service contacts",
                        badge = "${contacts.size} Total",
                        icon = Icons.Default.People,
                        accentColor = Color(0xFF10B981),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search directory...",
                        addLabel = "Add Contact",
                        onAddClick = { viewModel.openAddContactDialog() },
                        onDismiss = onDismiss
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(filteredAll, key = { it.id }) { contact ->
                            ContactDetailCard(
                                contact = contact,
                                context = context,
                                onEdit = { editingContact = contact },
                                onDelete = { viewModel.deleteContact(contact.id, contact.name) }
                            )
                        }
                    }
                }

                // 4. ALARMS & AUDIO TILE
                "audio_alarms" -> {
                    val filteredSchedules = remember(schedules, searchQuery) {
                        if (searchQuery.isBlank()) schedules
                        else schedules.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.soundTone.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    CategoryHeaderSection(
                        title = "Alarms & Audio",
                        subtitle = "High-priority alarms, medication timers & custom audio tones",
                        badge = "${schedules.size} Active",
                        icon = Icons.Default.VolumeUp,
                        accentColor = Color(0xFF3B82F6),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search alarms by title or tone...",
                        addLabel = "Add Alarm",
                        onAddClick = { viewModel.openAddScheduleDialog() },
                        onDismiss = onDismiss
                    )

                    if (filteredSchedules.isEmpty()) {
                        EmptyCategoryState(
                            emoji = if (searchQuery.isBlank()) "⏰" else "🔍",
                            message = if (searchQuery.isBlank()) "No audio reminders set" else "No alarms match '$searchQuery'",
                            hint = if (searchQuery.isBlank()) "Tap '+ Add Reminder' to schedule medication, wake up or urgency alarms" else "Try clearing your search query",
                            actionText = if (searchQuery.isBlank()) "+ Add Reminder" else "Clear Search",
                            onAction = {
                                if (searchQuery.isBlank()) viewModel.openAddScheduleDialog()
                                else searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(filteredSchedules, key = { it.id }) { sched ->
                                ScheduleTileRow(
                                    schedule = sched,
                                    viewModel = viewModel,
                                    onEdit = { editingSchedule = sched },
                                    onDelete = { viewModel.deleteSchedule(sched.id) }
                                )
                            }
                        }
                    }
                }

                // 5. PASSWORDS & SECRETS TILE
                "passwords" -> {
                    val filteredSecrets = remember(secrets, searchQuery) {
                        if (searchQuery.isBlank()) secrets
                        else secrets.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.subtitle.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true) ||
                            it.notes.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    CategoryHeaderSection(
                        title = "Pass & Secrets",
                        subtitle = "Bank logins, WiFi passwords, cards & encrypted credentials",
                        badge = "${secrets.size} Stored",
                        icon = Icons.Default.Lock,
                        accentColor = Color(0xFF8B5CF6),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search credentials by title, type or note...",
                        addLabel = "Add Secret",
                        onAddClick = { viewModel.openAddSecretDialog() },
                        onDismiss = onDismiss
                    )

                    if (filteredSecrets.isEmpty()) {
                        EmptyCategoryState(
                            emoji = if (searchQuery.isBlank()) "🔐" else "🔍",
                            message = if (searchQuery.isBlank()) "No secrets stored yet" else "No credentials match '$searchQuery'",
                            hint = if (searchQuery.isBlank()) "Tap '+ Add Secret' to securely store credentials with AES-256" else "Try clearing your search query",
                            actionText = if (searchQuery.isBlank()) "+ Add Secret" else "Clear Search",
                            onAction = {
                                if (searchQuery.isBlank()) viewModel.openAddSecretDialog()
                                else searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(filteredSecrets, key = { it.id }) { secret ->
                                SecretItemRow(
                                    secret = secret,
                                    onClick = { viewModel.openSecretDetail(secret) }
                                )
                            }
                        }
                    }
                }

                // 6. DOCUMENTS & FILES TILE
                "documents" -> {
                    var selectedDocCategoryFilter by remember(categoryId) { mutableStateOf("all") }

                    CategoryHeaderSection(
                        title = "Documents & Files",
                        subtitle = "Passports, IDs, contracts, receipts & photos",
                        badge = "${documents.size} Files",
                        icon = Icons.Default.Folder,
                        accentColor = Color(0xFFF59E0B),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search documents by name or category...",
                        addLabel = "Add File",
                        onAddClick = { viewModel.openAddDocumentDialog() },
                        onDismiss = onDismiss
                    )

                                        // Document Grouping / Folder Organization Section
                    var showNewFolderInput by remember { mutableStateOf(false) }
                    var newFolderNameInput by remember { mutableStateOf("") }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, theme.primaryLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Folder Organization & Storage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                }
                                androidx.compose.material3.TextButton(
                                    onClick = { showNewFolderInput = !showNewFolderInput }
                                ) {
                                    Text(if (showNewFolderInput) "Cancel" else "+ New Folder", fontSize = 11.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (showNewFolderInput) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newFolderNameInput,
                                        onValueChange = { newFolderNameInput = it },
                                        placeholder = { Text("e.g. Tax_2026, Insurance, Permits") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = defaultTextFieldColors()
                                    )
                                    Button(
                                        onClick = {
                                            if (newFolderNameInput.isNotBlank()) {
                                                val folderId = newFolderNameInput.trim().lowercase().replace("\\s+".toRegex(), "_")
                                                selectedDocCategoryFilter = folderId
                                                newFolderNameInput = ""
                                                showNewFolderInput = false
                                                viewModel.showToast("📁 Created folder '$folderId'")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Create", fontSize = 11.sp)
                                    }
                                }
                                Text("Tip: When uploading files via '+ Add File', you can also select or create custom folders.", fontSize = 10.sp, color = theme.textMuted, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    // Document Grouping / Category Filter Chips including custom folders
                    val standardCatIds = listOf("photo", "video", "document", "id", "contract", "receipt", "medical")
                    val customCatIds = documents.map { it.category }.filter { it !in standardCatIds }.distinct()
                    val docCategories = listOf(
                        "all" to "All",
                        "photo" to "🖼️ Photos",
                        "video" to "🎥 Videos",
                        "document" to "📄 Documents",
                        "id" to "🪪 IDs",
                        "contract" to "📑 Contracts",
                        "receipt" to "🧾 Receipts",
                        "medical" to "🏥 Medical"
                    ) + customCatIds.map { it to "📁 ${it.replaceFirstChar { c -> c.uppercase() }}" }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        docCategories.forEach { (catId, catLabel) ->
                            val isSelected = selectedDocCategoryFilter == catId
                            val count = if (catId == "all") documents.size else documents.count { 
                                if (catId == "video") it.isVideo || it.category.equals("video", true)
                                else it.category.equals(catId, true)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) theme.primary else theme.surface)
                                    .border(1.dp, if (isSelected) theme.primaryLight else theme.borderSubtle, RoundedCornerShape(20.dp))
                                    .clickable { selectedDocCategoryFilter = catId }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (catId == "all") "$catLabel (${documents.size})" else "$catLabel ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else theme.textPrimary
                                )
                            }
                        }
                    }

                    val categoryFilteredDocs = if (selectedDocCategoryFilter == "all") {
                        documents
                    } else {
                        documents.filter { 
                            if (selectedDocCategoryFilter == "video") it.isVideo || it.category.equals("video", true)
                            else it.category.equals(selectedDocCategoryFilter, true)
                        }
                    }

                    val filteredDocuments = remember(categoryFilteredDocs, searchQuery) {
                        if (searchQuery.isBlank()) categoryFilteredDocs
                        else categoryFilteredDocs.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true) ||
                            it.notes.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredDocuments.isEmpty()) {
                        EmptyCategoryState(
                            emoji = if (searchQuery.isBlank()) "📁" else "🔍",
                            message = if (searchQuery.isBlank()) "No secure files uploaded" else "No files match '$searchQuery'",
                            hint = if (searchQuery.isBlank()) "Store national IDs, driver licenses or insurance PDFs securely" else "Try clearing your search query",
                            actionText = if (searchQuery.isBlank()) "+ Upload File" else "Clear Search",
                            onAction = {
                                if (searchQuery.isBlank()) viewModel.openAddDocumentDialog()
                                else searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(filteredDocuments, key = { it.id }) { doc ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.openDocumentDetail(doc) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(theme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                when {
                                                    doc.isVideo -> "🎥"
                                                    doc.category == "photo" -> "🖼️"
                                                    doc.category == "id" -> "🪪"
                                                    doc.category == "contract" -> "📑"
                                                    else -> "📁"
                                                },
                                                fontSize = 20.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = doc.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${doc.category.uppercase()} · ${if (doc.locked) "🔒 Protected" else "🔓 Open"}",
                                                fontSize = 11.sp,
                                                color = theme.textMuted
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = theme.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. URGENT & EXPIRING TILE
                "urgent" -> {
                    val rawUrgent = items.filter { it.category != "event" && viewModel.daysUntil(it.date) <= 2 }
                    val rawSoon = items.filter { it.category != "event" && viewModel.daysUntil(it.date) in 3..7 }

                    val urgentItems = remember(rawUrgent, searchQuery) {
                        if (searchQuery.isBlank()) rawUrgent
                        else rawUrgent.filter { it.name.contains(searchQuery, true) || it.category.contains(searchQuery, true) }
                    }
                    val soonItems = remember(rawSoon, searchQuery) {
                        if (searchQuery.isBlank()) rawSoon
                        else rawSoon.filter { it.name.contains(searchQuery, true) || it.category.contains(searchQuery, true) }
                    }

                    CategoryHeaderSection(
                        title = "Urgent & Due Tasks",
                        subtitle = "Passports, bills, warranty or certifications nearing due date",
                        badge = "${urgentItems.size + soonItems.size} Due",
                        icon = Icons.Default.Warning,
                        accentColor = UrgentRed,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Search urgent items by name or type...",
                        addLabel = "Add Item",
                        onAddClick = { viewModel.openAddItemDialog() },
                        onDismiss = onDismiss
                    )

                    if (urgentItems.isEmpty() && soonItems.isEmpty()) {
                        EmptyCategoryState(
                            emoji = if (searchQuery.isBlank()) "🟢" else "🔍",
                            message = if (searchQuery.isBlank()) "Everything is safe & up to date!" else "No urgent items match '$searchQuery'",
                            hint = if (searchQuery.isBlank()) "No urgent expirations in the upcoming 7 days" else "Try clearing your search query",
                            actionText = if (searchQuery.isBlank()) "+ Add Vault Item" else "Clear Search",
                            onAction = {
                                if (searchQuery.isBlank()) viewModel.openAddItemDialog()
                                else searchQuery = ""
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            if (urgentItems.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "🔴 CRITICAL (Due within 48h)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = UrgentRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                items(urgentItems, key = { "urg_${it.id}" }) { item ->
                                    UrgentTaskCard(
                                        item = item,
                                        viewModel = viewModel,
                                        onEdit = { viewModel.openEditItemDialog(item) },
                                        onDelete = { viewModel.deleteItem(item.id, item.name) }
                                    )
                                }
                            }

                            if (soonItems.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "🟡 EXPIRING THIS WEEK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningYellow
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                items(soonItems, key = { "soon_${it.id}" }) { item ->
                                    UrgentTaskCard(
                                        item = item,
                                        viewModel = viewModel,
                                        onEdit = { viewModel.openEditItemDialog(item) },
                                        onDelete = { viewModel.deleteItem(item.id, item.name) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. EMERGENCY SOS TILE
                "emergency" -> {
                    CategoryHeaderSection(
                        title = "Emergency SOS",
                        subtitle = "ICE medical contacts, blood group, allergies & beacon",
                        badge = if (emergency.isConfigured) "ICE READY" else "NOT CONFIGURED",
                        icon = Icons.Default.Emergency,
                        accentColor = Color(0xFFEC4899),
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        searchPlaceholder = "Filter emergency info...",
                        addLabel = "Configure",
                        onAddClick = { viewModel.openEmergencyEditDialog() },
                        onDismiss = onDismiss
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = theme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🚨 ICE Medical Profile",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (emergency.isConfigured) SafeGreenBg else UrgentRedBg)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (emergency.isConfigured) "ACTIVE" else "INCOMPLETE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (emergency.isConfigured) SafeGreen else UrgentRed
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Primary Contact: ${emergency.contactName.ifBlank { "Not configured" }}",
                                        fontSize = 13.sp,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "Emergency Phone: ${emergency.contactPhone.ifBlank { "None" }}",
                                        fontSize = 12.sp,
                                        color = theme.textMuted
                                    )
                                    Text(
                                        text = "Blood Group: ${emergency.blood.ifBlank { "Unknown" }} · Allergies: ${emergency.allergies.ifBlank { "None" }}",
                                        fontSize = 12.sp,
                                        color = theme.textMuted
                                    )
                                    if (emergency.notes.isNotBlank()) {
                                        Text(
                                            text = "Physician Notes: ${emergency.notes}",
                                            fontSize = 11.sp,
                                            color = theme.textMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (emergency.contactPhone.isNotBlank()) {
                                            Button(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${emergency.contactPhone}"))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreenBg),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call ICE", tint = SafeGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Call ICE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.openEmergencyEditDialog() },
                                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit ICE", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Editing contact sub-sheet
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

        // Editing schedule sub-sheet
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

/**
 * Modern, beautifully spaced 2-row Category Header.
 * Row 1: Title, Icon & Badge on Left | Distinct Close / Cancel Button (✕) on Right.
 * Row 2: Category Search Bar (Weight 1f) | "+ Add" Action Button.
 * Absolutely eliminates all overlapping of Add, Cancel, and Title across all screen sizes.
 */
@Composable
private fun CategoryHeaderSection(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchPlaceholder: String,
    addLabel: String = "Add",
    onAddClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        // TOP ROW: Icon + Title & Badge on Left | Circular Close (Cancel) Button on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.14f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = theme.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // CANCEL / CLOSE CIRCLE BUTTON (✕) - Dedicated spot at top right!
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceVariant)
                    .border(1.dp, theme.borderSubtle, CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel & Close",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SECOND ROW: Dedicated Category Search Bar + "+ Add" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = searchPlaceholder,
                        fontSize = 12.sp,
                        color = theme.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = theme.textMuted, modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = theme.textMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textPrimary,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = theme.borderSubtle,
                    focusedContainerColor = theme.surfaceVariant,
                    unfocusedContainerColor = theme.surfaceVariant
                ),
                singleLine = true
            )

            // Primary "+ Add" Action Button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(addLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * Modern, spacious Contact Detail Card.
 * Avatar + Name + Category Pill on Left | Distinct Edit (✏️) and Delete (🗑️) buttons at Top Right.
 * Call (📞) and WhatsApp (💬) are wide, spacious buttons on the Bottom Row.
 * Completely eliminates Edit/Delete/Call/Message collision and overlapping.
 */
@Composable
private fun ContactDetailCard(
    contact: MemberContact,
    context: Context,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = LocalAppTheme.current
    val catInfo = CONTACT_CATEGORIES.find { it.id == contact.category } ?: CONTACT_CATEGORIES.last()

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // TOP ROW: Avatar + Name & Category Pill on Left | Edit and Delete on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(catInfo.bgColor)
                        .border(1.dp, catInfo.color.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.avatarEmoji.ifBlank { catInfo.emoji }, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
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

                Spacer(modifier = Modifier.width(8.dp))

                // Dedicated, spaced Edit and Delete action buttons at Top Right
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
                            .clickable(onClick = onEdit),
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
                            .clickable(onClick = onDelete),
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

            // MIDDLE INFO: Specialty & Phone
            if (contact.specialty.isNotBlank() || contact.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 52.dp)) {
                    if (contact.specialty.isNotBlank()) {
                        Text(
                            text = contact.specialty,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.primaryLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (contact.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📞 ${contact.phone}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.textMuted
                        )
                    }
                }
            }

            // Address and Notes
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
                            color = theme.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (contact.notes.isNotBlank()) {
                        Text(
                            text = "📝 ${contact.notes}",
                            fontSize = 11.sp,
                            color = theme.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // BOTTOM ROW: Quick Action Call & WhatsApp - wide, spacious 50/50 buttons
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
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = SafeGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x2200E676)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "WhatsApp", tint = SafeGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCategoryState(
    emoji: String,
    message: String,
    hint: String,
    actionText: String,
    onAction: () -> Unit
) {
    val theme = LocalAppTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(message, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(hint, fontSize = 11.sp, color = theme.textMuted, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * Spacious Schedule Tile with dedicated Action buttons that never overlap.
 */
@Composable
private fun ScheduleTileRow(
    schedule: AudioSchedule,
    viewModel: VaultViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = LocalAppTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // TOP ROW: Clock + Info on Left | Edit & Delete on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏰", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${schedule.dateTime.replace("T", " ")} · ${schedule.soundTone}",
                        fontSize = 11.sp,
                        color = theme.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit and Delete buttons on Top Right
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
                            .clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Schedule",
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
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Schedule",
                            tint = UrgentRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM ROW: Test Tone & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.playAudioTone(schedule.soundTone, schedule.volume) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Test Tone", tint = Color(0xFF3B82F6), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Tone", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (schedule.enabled) Color(0x2210B981) else Color(0x1F64748B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (schedule.enabled) "ACTIVE" else "MUTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.enabled) SafeGreen else theme.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgentTaskCard(
    item: VaultItem,
    viewModel: VaultViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = LocalAppTheme.current
    val days = viewModel.daysUntil(item.date)
    val statusColor = when {
        days <= 2 -> UrgentRed
        days <= 7 -> WarningYellow
        else -> SafeGreen
    }
    val statusBg = when {
        days <= 2 -> UrgentRedBg
        days <= 7 -> WarningYellowBg
        else -> SafeGreenBg
    }
    val daysLabel = when {
        days < 0 -> "Expired (${-days}d ago)"
        days == 0 -> "Due Today!"
        days == 1 -> "Due Tomorrow"
        else -> "Due in $days days"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onEdit)
            .testTag("urgent_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category emoji, Title, Urgency pill, Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (item.category) {
                                "document" -> "📄"
                                "subscription" -> "💳"
                                "card" -> "🪪"
                                "event" -> "🎂"
                                else -> "🥛"
                            },
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.recurring) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🔁", fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = daysLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                // Edit and Delete Buttons on Top Right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.primaryContainer)
                            .border(1.dp, theme.primaryLight.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Task",
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
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = UrgentRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Due Date & Time info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 Due: ${item.date}${if (!item.audioTime.isNullOrBlank()) " at ${item.audioTime}" else ""}",
                    fontSize = 11.sp,
                    color = theme.textMuted
                )
                if (item.audioId != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.primaryContainer)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("⏰ Alarm Set", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                    }
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 ${item.notes}",
                    fontSize = 11.sp,
                    color = theme.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }

            // Bottom action buttons: Test Alarm + Mark Done
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.audioId != null) {
                    Button(
                        onClick = { viewModel.playAudioTone(item.audioId) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🔊 Test Alarm", fontSize = 11.sp, color = theme.primaryLight, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.repository.markDone(item.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreenBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Done", fontSize = 11.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
