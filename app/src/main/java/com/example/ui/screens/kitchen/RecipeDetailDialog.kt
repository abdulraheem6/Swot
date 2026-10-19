package com.example.ui.screens.kitchen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.kitchen.RecipeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailDialog(
    recipe: RecipeEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val ingredients = remember(recipe.ingredientsJson) { recipe.getIngredientsList() }
    val steps = remember(recipe.stepsJson) { recipe.getStepsList() }

    // Checked ingredients for interactive cooking checklist
    val checkedIngredients = remember { mutableStateListOf<Int>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KitchenTheme.CreamBackground,
        modifier = Modifier.testTag("recipe_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            // Top Navigation & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KitchenTheme.CreamSurface)
                        .border(1.dp, KitchenTheme.CreamBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = KitchenTheme.TextDark)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Favorite Toggle
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(KitchenTheme.CreamSurface)
                            .border(1.dp, KitchenTheme.CreamBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (recipe.isFavorite) Color(0xFFFF3366) else KitchenTheme.TextDark
                        )
                    }

                    // Edit Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(KitchenTheme.CreamSurface)
                            .border(1.dp, KitchenTheme.CreamBorder, CircleShape)
                            .testTag("edit_recipe_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = KitchenTheme.CurryOrange)
                    }

                    // Delete Button
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFFFCDD2), CircleShape)
                            .testTag("delete_recipe_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Food Photo
            val imageUrl = if (recipe.photoUri.isNotBlank()) recipe.photoUri
            else FoodPhotoPresets.getDefaultUrlForCategory(recipe.category)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(KitchenTheme.CreamSurfaceAlt)
                    .border(1.5.dp, KitchenTheme.CreamBorder, RoundedCornerShape(22.dp))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Category pill on hero photo
                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KitchenTheme.getCategoryColor(recipe.category))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${KitchenTheme.getCategoryEmoji(recipe.category)} ${recipe.category.uppercase()}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Family Member Tag
            if (recipe.familyMemberName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👨‍🍳 Cherished Recipe by: ",
                        fontSize = 12.sp,
                        color = KitchenTheme.TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = recipe.familyMemberName,
                        fontSize = 13.sp,
                        color = KitchenTheme.CurryOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Title
            Text(
                text = recipe.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KitchenTheme.TextDark,
                lineHeight = 30.sp
            )

            if (recipe.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.description,
                    fontSize = 14.sp,
                    color = KitchenTheme.TextMuted,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row (Cooking Time & Servings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Time Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamSurface),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KitchenTheme.TurmericGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = KitchenTheme.TurmericGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cooking Time", fontSize = 10.sp, color = KitchenTheme.TextMuted, fontWeight = FontWeight.Medium)
                            Text("${recipe.cookingTimeMinutes} Mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.TextDark)
                        }
                    }
                }

                // Servings Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamSurface),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KitchenTheme.CurryOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = KitchenTheme.CurryOrange, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Servings", fontSize = 10.sp, color = KitchenTheme.TextMuted, fontWeight = FontWeight.Medium)
                            Text("${recipe.servings} Servings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.TextDark)
                        }
                    }
                }
            }

            // In-App Local Video Player Section
            if (recipe.localVideoUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "📹 IN-APP COOKING VIDEO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KitchenTheme.CurryOrange,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LocalVideoPlayer(videoUriString = recipe.localVideoUri)
            }

            // YouTube Video Link Section
            if (recipe.youtubeUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(16.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.youtubeUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .testTag("open_youtube_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFDC2626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Watch on YouTube", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                Text(recipe.youtubeUrl, fontSize = 11.sp, color = Color(0xFFB91C1C), maxLines = 1)
                            }
                        }
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ingredients Section (Checklist Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🥗 INGREDIENTS (${ingredients.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = KitchenTheme.CurryOrange,
                    letterSpacing = 0.5.sp
                )
                if (checkedIngredients.isNotEmpty()) {
                    TextButton(onClick = { checkedIngredients.clear() }) {
                        Text("Reset checks", fontSize = 11.sp, color = KitchenTheme.TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (ingredients.isEmpty()) {
                        Text("No ingredients specified.", fontSize = 13.sp, color = KitchenTheme.TextMuted)
                    } else {
                        ingredients.forEachIndexed { index, ingredient ->
                            val isChecked = checkedIngredients.contains(index)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) checkedIngredients.remove(index)
                                        else checkedIngredients.add(index)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isChecked) KitchenTheme.CorianderGreen else KitchenTheme.CreamSurface)
                                        .border(1.5.dp, if (isChecked) KitchenTheme.CorianderGreen else KitchenTheme.CreamBorderFocus, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = ingredient,
                                    fontSize = 13.sp,
                                    color = if (isChecked) KitchenTheme.TextSubtle else KitchenTheme.TextDark,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step-by-Step Cooking Instructions
            Text(
                text = "🍳 COOKING STEPS (${steps.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = KitchenTheme.CurryOrange,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (steps.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamCard),
                    modifier = Modifier.fillMaxWidth().border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(18.dp))
                ) {
                    Text("No cooking steps specified.", fontSize = 13.sp, color = KitchenTheme.TextMuted, modifier = Modifier.padding(16.dp))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    steps.forEachIndexed { index, step ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(KitchenTheme.CurryOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KitchenTheme.CurryOrange
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = step,
                                    fontSize = 13.sp,
                                    color = KitchenTheme.TextDark,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Recipe?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${recipe.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
