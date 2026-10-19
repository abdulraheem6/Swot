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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultViewModel
import com.example.ui.theme.LocalAppTheme

data class CategoryTileItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onTileClick: () -> Unit,
    val onAddClick: () -> Unit,
    val testTag: String
)

@Composable
fun CategoryTilesSection(
    viewModel: VaultViewModel,
    schedulesCount: Int,
    contactsCount: Int = 0,
    familyCount: Int = 0,
    servicesCount: Int = 0,
    secretsCount: Int,
    documentsCount: Int,
    urgentCount: Int,
    emergencyConfigured: Boolean,
    transactionsCount: Int = 0,
    totalMonthSpend: Double = 0.0,
    recipesCount: Int = 0,
    filterQuery: String = "",
    onClearFilter: () -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    val allTiles = listOf(
        CategoryTileItem(
            id = "kitchen",
            title = "Kitchen & Recipes",
            subtitle = if (recipesCount == 0) "Curries, steps & videos" else "$recipesCount family dishes & curries",
            tag = "CURRIES & RECIPES",
            icon = Icons.Default.RestaurantMenu,
            accentColor = Color(0xFFEA580C), // Rich Curry Orange
            onTileClick = { onSelectCategory("kitchen") },
            onAddClick = { viewModel.openAddRecipeDialog() },
            testTag = "tile_kitchen"
        ),
        CategoryTileItem(
            id = "money",
            title = "Money & Finance",
            subtitle = if (transactionsCount == 0) "Expenses, income, budget" else "$${String.format(java.util.Locale.US, "%.0f", totalMonthSpend)} spent • $transactionsCount records",
            tag = "EXP & BUDGET",
            icon = Icons.Default.AttachMoney,
            accentColor = Color(0xFF10B981), // Emerald
            onTileClick = { onSelectCategory("money") },
            onAddClick = { viewModel.openAddTransactionDialog("expense") },
            testTag = "tile_money"
        ),
        CategoryTileItem(
            id = "family",
            title = "Family Members",
            subtitle = if (familyCount == 0) "Add family & ICE" else "$familyCount members listed",
            tag = "FAMILY",
            icon = Icons.Default.People,
            accentColor = Color(0xFF10B981), // Emerald
            onTileClick = { onSelectCategory("family") },
            onAddClick = { viewModel.openAddContactDialog() },
            testTag = "tile_family"
        ),
        CategoryTileItem(
            id = "services",
            title = "Services Directory",
            subtitle = if (servicesCount == 0) "Plumber, electrician..." else "$servicesCount service pros",
            tag = "SERVICES",
            icon = Icons.Default.Build,
            accentColor = Color(0xFF0284C7), // Sky Blue
            onTileClick = { onSelectCategory("services") },
            onAddClick = { viewModel.openAddContactDialog() },
            testTag = "tile_services"
        ),
        CategoryTileItem(
            id = "passwords",
            title = "Pass & Secrets",
            subtitle = if (secretsCount == 0) "Logins, cards, APIs" else "$secretsCount credentials",
            tag = "AES-256",
            icon = Icons.Default.Lock,
            accentColor = Color(0xFF8B5CF6),
            onTileClick = { onSelectCategory("passwords") },
            onAddClick = { viewModel.openAddSecretDialog() },
            testTag = "tile_passwords"
        ),
        CategoryTileItem(
            id = "documents",
            title = "Documents & Files",
            subtitle = if (documentsCount == 0) "Passports, receipts" else "$documentsCount files stored",
            tag = "ENCRYPTED",
            icon = Icons.Default.Folder,
            accentColor = Color(0xFFF59E0B),
            onTileClick = { onSelectCategory("documents") },
            onAddClick = { viewModel.openAddDocumentDialog() },
            testTag = "tile_documents"
        ),
        CategoryTileItem(
            id = "audio_alarms",
            title = "Alarms & Audio",
            subtitle = if (schedulesCount == 0) "No active alarms" else "$schedulesCount reminders",
            tag = "LOUD ALARM",
            icon = Icons.Default.VolumeUp,
            accentColor = Color(0xFF3B82F6),
            onTileClick = { onSelectCategory("audio_alarms") },
            onAddClick = { viewModel.openAddScheduleDialog() },
            testTag = "tile_audio_alarms"
        ),
        CategoryTileItem(
            id = "urgent",
            title = "Urgent & Due",
            subtitle = if (urgentCount == 0) "All items safe" else "$urgentCount expiring soon",
            tag = "ACTION REQ",
            icon = Icons.Default.Warning,
            accentColor = Color(0xFFEF4444),
            onTileClick = { onSelectCategory("urgent") },
            onAddClick = { viewModel.openAddItemDialog() },
            testTag = "tile_urgent"
        ),
        CategoryTileItem(
            id = "emergency",
            title = "Emergency SOS",
            subtitle = if (emergencyConfigured) "ICE Medical Active" else "Configure ICE data",
            tag = "SOS LOCK",
            icon = Icons.Default.Emergency,
            accentColor = Color(0xFFEC4899),
            onTileClick = { onSelectCategory("emergency") },
            onAddClick = { viewModel.openEmergencyEditDialog() },
            testTag = "tile_emergency"
        )
    )

    val tiles = if (filterQuery.isBlank()) {
        allTiles
    } else {
        allTiles.filter {
            it.title.contains(filterQuery, ignoreCase = true) ||
            it.tag.contains(filterQuery, ignoreCase = true) ||
            it.subtitle.contains(filterQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (filterQuery.isBlank()) "FEATURE TILES" else "FILTERED TILES (${tiles.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = if (filterQuery.isBlank()) "Tap to view list & manage" else "Matching '$filterQuery'",
                fontSize = 11.sp,
                color = theme.textMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tiles.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No category tiles match \"$filterQuery\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for Family, Services, Passwords, Documents, Alarms, or Urgent",
                        fontSize = 11.sp,
                        color = theme.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.TextButton(onClick = onClearFilter) {
                        Text("Show All Tiles", color = theme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Grid of Tiles (2 columns)
            for (i in tiles.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryTileCard(
                        tile = tiles[i],
                        modifier = Modifier.weight(1f)
                    )

                    if (i + 1 < tiles.size) {
                        CategoryTileCard(
                            tile = tiles[i + 1],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (i + 2 < tiles.size) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryTileCard(
    tile: CategoryTileItem,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
            .clickable { tile.onTileClick() }
            .testTag(tile.testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon with subtle tinted background
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tile.accentColor.copy(alpha = 0.14f))
                        .border(1.dp, tile.accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = tile.title,
                        tint = tile.accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Quick Add "+" Action Button
                IconButton(
                    onClick = { tile.onAddClick() },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceVariant)
                        .border(1.dp, theme.borderSubtle, CircleShape)
                        .testTag("${tile.testTag}_add_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to ${tile.title}",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Title
            Text(
                text = tile.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Category Subtitle
            Text(
                text = tile.subtitle,
                fontSize = 11.sp,
                color = theme.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row with Tag and mini arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tile.accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tile.tag,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = tile.accentColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = theme.textMuted,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

