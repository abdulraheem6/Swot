package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrashItem
import com.example.ui.VaultViewModel
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashSheet(
    trashItems: List<TrashItem>,
    onDismiss: () -> Unit,
    viewModel: VaultViewModel
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = VaultSurface,
        modifier = Modifier.testTag("trash_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🗑️ Trash",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Deleted items are kept for 30 days before permanent removal.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (trashItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Trash is empty",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(trashItems, key = { it.id }) { item ->
                        val daysAgo = ((System.currentTimeMillis() - item.deletedAt) / (24 * 60 * 60 * 1000)).toInt()

                        Card(
                            colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x22FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(getCategoryIcon(item.category), fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (daysAgo == 0) "Deleted today" else "Deleted $daysAgo days ago",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            viewModel.repository.restoreTrash(item.id)
                                            viewModel.showToast("↩ Restored '${item.name}'")
                                        }
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = SafeGreen)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.repository.purgeTrash(item.id)
                                            viewModel.showToast("🗑️ Permanently deleted")
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = "Purge", tint = UrgentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", color = TextMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun getCategoryIcon(cat: String): String {
    return when (cat.lowercase()) {
        "product" -> "🥛"
        "document" -> "📄"
        "subscription" -> "💳"
        "card" -> "🪪"
        "event" -> "🎂"
        "login" -> "🔑"
        "bank" -> "🏦"
        "wifi" -> "📶"
        "identity" -> "🪪"
        "api" -> "🔌"
        "recovery" -> "🔐"
        "medical" -> "🏥"
        "vehicle" -> "🚗"
        "crypto" -> "🪙"
        "note" -> "📝"
        else -> "📦"
    }
}
