package com.example.ui.screens

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecureDocument
import com.example.data.SecretItem
import com.example.data.VaultItem
import com.example.ui.VaultViewModel
import com.example.ui.components.CategoryTilesSection
import com.example.ui.components.CategoryDetailSheet
import com.example.ui.components.ContactsDirectorySection
import com.example.ui.components.defaultTextFieldColors
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultBg
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultPrimaryContainer
import com.example.ui.theme.VaultPrimaryLight
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import com.example.ui.theme.WarningYellow
import com.example.ui.theme.WarningYellowBg
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val secrets by viewModel.secrets.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val emergency by viewModel.emergency.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val trash by viewModel.trash.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val activeMoneyMonth by viewModel.activeMoneyMonth.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val theme = LocalAppTheme.current

    val totalMonthSpend = remember(transactions, activeMoneyMonth) {
        transactions.filter { it.type == "expense" && it.date.startsWith(activeMoneyMonth) }.sumOf { it.amount }
    }
    val monthTransactionsCount = remember(transactions, activeMoneyMonth) {
        transactions.count { it.date.startsWith(activeMoneyMonth) }
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Filter items & secrets if search query is active
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.notes.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredSecrets = remember(secrets, searchQuery) {
        if (searchQuery.isBlank()) secrets
        else secrets.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val activeUrgencyTab by viewModel.activeUrgencyTab.collectAsState()
    var selectedCategoryForDetail by remember { mutableStateOf<String?>(null) }

    val previewSecrets = remember(secrets) {
        secrets.sortedByDescending { it.favorite }.take(3)
    }

    val urgentItems = remember(items) {
        items.filter { it.category != "event" && viewModel.daysUntil(it.date) <= 2 }
            .sortedBy { it.date }
    }

    val soonItems = remember(items) {
        items.filter { it.category != "event" && viewModel.daysUntil(it.date) in 3..7 }
            .sortedBy { it.date }
    }

    val safeItems = remember(items) {
        items.filter { it.category != "event" && viewModel.daysUntil(it.date) > 7 }
            .sortedBy { it.date }
    }

    val expiringItems = remember(items) {
        items.filter { it.category != "event" && viewModel.daysUntil(it.date) <= 30 }
            .sortedBy { it.date }
    }

    val recurringItems = remember(items) { items.filter { it.recurring } }

    val eventItems = remember(items) { items.filter { it.category == "event" } }

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
            // Header bar
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "$greeting, ${settings.username}",
                            fontSize = 12.sp,
                            color = theme.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "My", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            Text(text = "Vault", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            com.example.ui.components.RegimeBadge(
                                regime = settings.securityRegime,
                                onClick = { viewModel.openRegimeDialog() }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Bright / Dark Theme Toggle
                        IconButton(
                            onClick = {
                                val nextTheme = if (theme.isLight) "dark_modern" else "bright_light"
                                viewModel.setTheme(nextTheme)
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surfaceVariant)
                                .border(1.dp, theme.borderSubtle, RoundedCornerShape(10.dp))
                                .testTag("home_theme_toggle_btn")
                        ) {
                            Text(
                                text = if (theme.isLight) "☀️" else "🌙",
                                fontSize = 16.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openTrashDialog() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surfaceVariant)
                                .border(1.dp, theme.borderSubtle, RoundedCornerShape(10.dp))
                                .testTag("home_trash_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (trash.isNotEmpty()) {
                                        Badge(containerColor = UrgentRed) {
                                            Text("${trash.size}", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Trash", tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }

                        IconButton(
                            onClick = { viewModel.openProfileDialog() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.primaryContainer)
                                .border(1.dp, theme.primaryLight, RoundedCornerShape(10.dp))
                                .testTag("home_profile_btn")
                        ) {
                            Text(
                                text = settings.username.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryLight,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Search Bar - Filters available tiles by title
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search category tiles (Family, Services, Secrets...)", fontSize = 13.sp, color = theme.textMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.textMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = theme.textMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.borderSubtle,
                        focusedContainerColor = theme.surfaceVariant,
                        unfocusedContainerColor = theme.surfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Category Tiles Grid Dashboard (Family, Services, Secrets, Docs, Alarms, Urgent, SOS)
            item {
                val familyCount = remember(contacts) { contacts.count { it.category.equals("family", ignoreCase = true) } }
                val servicesCount = remember(contacts) { contacts.count { !it.category.equals("family", ignoreCase = true) } }

                CategoryTilesSection(
                    viewModel = viewModel,
                    schedulesCount = schedules.size,
                    contactsCount = contacts.size,
                    familyCount = familyCount,
                    servicesCount = servicesCount,
                    secretsCount = secrets.size,
                    documentsCount = documents.size,
                    urgentCount = urgentItems.size,
                    emergencyConfigured = emergency.isConfigured,
                    transactionsCount = monthTransactionsCount,
                    totalMonthSpend = totalMonthSpend,
                    recipesCount = recipes.size,
                    filterQuery = searchQuery,
                    onClearFilter = { viewModel.setSearchQuery("") },
                    onSelectCategory = { catId -> selectedCategoryForDetail = catId }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Identity & Clearance Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderAccent, RoundedCornerShape(16.dp))
                        .testTag("profile_identity_card")
                ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = settings.organization.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreen
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(settings.employeeId, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = settings.username,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "${settings.department} · ${settings.securityClearance}",
                                        fontSize = 11.sp,
                                        color = theme.textMuted,
                                        maxLines = 1
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.primaryContainer)
                                            .clickable { viewModel.openProfileDialog() }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("👤 Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x1F00E676))
                                            .clickable { viewModel.openAuditLogsDialog() }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("🛡️ Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Welcome to SecureVault Hub",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = "Tap any category tile above to view full lists, manage entries, add records or configure reminders.",
                                    fontSize = 11.sp,
                                    color = theme.textMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            if (selectedCategoryForDetail != null) {
                CategoryDetailSheet(
                    categoryId = selectedCategoryForDetail!!,
                    onDismiss = { selectedCategoryForDetail = null },
                    viewModel = viewModel
                )
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = { viewModel.openAddItemDialog() },
                containerColor = VaultPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp)
                    .testTag("fab_add_item")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(28.dp))
            }
        }
    }

@Composable
fun VaultItemRow(
    item: VaultItem,
    viewModel: VaultViewModel,
    onEdit: (() -> Unit)? = null,
    onClick: () -> Unit = { viewModel.openEditItemDialog(item) }
) {
    val theme = LocalAppTheme.current
    val days = viewModel.daysUntil(item.date)
    val statusColor = when {
        days <= 2 -> UrgentRed
        days <= 7 -> WarningYellow
        else -> SafeGreen
    }

    val daysLabel = when {
        days < 0 -> "Expired"
        days == 0 -> "Today"
        days == 1 -> "Tomorrow"
        else -> "$days days"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("item_card_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Urgency line on left
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Category Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.recurring) SafeGreenBg else Color(0x1F6C63FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryIcon(item.category), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textPrimary
                    )
                    if (item.recurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SafeGreenBg)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("🔁 ${item.recurInterval}d", fontSize = 8.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (item.locked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🔐", fontSize = 10.sp)
                    }
                    if (item.audioId != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🔊", fontSize = 10.sp)
                    }
                    if (item.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3300E676))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("📷 Photo", fontSize = 8.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.date, fontSize = 11.sp, color = theme.textMuted)
                    Text(" · ", fontSize = 11.sp, color = theme.textMuted)
                    Text(text = daysLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            // Edit, Mark Done & Delete actions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.primaryContainer)
                        .clickable { (onEdit ?: { viewModel.openEditItemDialog(item) }).invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Task", tint = theme.primaryLight, modifier = Modifier.size(15.dp))
                }

                // Mark Done
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SafeGreenBg)
                        .clickable { viewModel.repository.markDone(item.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = SafeGreen, modifier = Modifier.size(15.dp))
                }

                // Delete
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UrgentRedBg)
                        .clickable { viewModel.deleteItem(item.id, item.name) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
fun SecretItemRow(
    secret: SecretItem,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("secret_card_${secret.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x266C63FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryIcon(secret.category), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = secret.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textPrimary
                    )
                    if (secret.favorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⭐", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (secret.requirePin && secret.require2fa) UrgentRedBg
                                else if (secret.requirePin) Color(0x266C63FF)
                                else if (secret.require2fa) Color(0x26FFAA00)
                                else Color(0x14FFFFFF)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = when {
                                secret.requirePin && secret.require2fa -> "🛡️ PIN+2FA"
                                secret.requirePin -> "🔒 PIN"
                                secret.require2fa -> "🔑 2FA"
                                else -> "🔓 Open"
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                secret.requirePin && secret.require2fa -> UrgentRed
                                secret.requirePin -> Color(0xFF9E95FF)
                                secret.require2fa -> Color(0xFFFFB74D)
                                else -> theme.textMuted
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = secret.subtitle.ifEmpty { secret.category.replaceFirstChar { it.uppercase() } },
                        fontSize = 11.sp,
                        color = theme.textMuted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!secret.expirationDate.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val isExp = secret.isExpired
                        val isNear = secret.isNearExpiration
                        val badgeBg = when {
                            isExp -> UrgentRedBg
                            isNear -> Color(0x33FFA500)
                            else -> Color(0x1A8B5CF6)
                        }
                        val badgeColor = when {
                            isExp -> UrgentRed
                            isNear -> Color(0xFFFFA500)
                            else -> Color(0xFFA78BFA)
                        }
                        val badgeText = when {
                            isExp -> "⌛ Expired"
                            isNear -> "⚠️ ${secret.daysUntilExpiration ?: 0}d left"
                            else -> "⏰ ${secret.expirationDate}"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = badgeText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                                if (secret.expirationAlertEnabled) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "🔔",
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.textMuted)
        }
    }
}

@Composable
private fun CounterCard(
    count: Int,
    label: String,
    containerColor: Color,
    borderColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) Color.White else TextSecondary)
        }
    }
}

@Composable
private fun UrgencyFilterTab(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) activeColor else VaultSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) activeColor else BorderSubtle,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextMuted
        )
    }
}
