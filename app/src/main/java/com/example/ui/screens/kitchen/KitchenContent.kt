package com.example.ui.screens.kitchen

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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.kitchen.RecipeEntity
import com.example.ui.VaultViewModel

@Composable
fun KitchenContent(
    viewModel: VaultViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipes by viewModel.recipes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    var selectedRecipeForDetail by remember { mutableStateOf<RecipeEntity?>(null) }
    var recipeToEdit by remember { mutableStateOf<RecipeEntity?>(null) }
    var showAddRecipeDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All" to "🔥 All",
        "Chicken" to "🍗 Chicken",
        "Mutton" to "🍖 Mutton",
        "Fish" to "🐟 Fish",
        "Vegetarian" to "🥬 Vegetarian",
        "Egg" to "🥚 Egg",
        "Dal" to "🍲 Dal",
        "Drinks" to "🍹 Drinks",
        "Bakers" to "🧁 Bakers"
    )

    // Filter recipes based on category, search, and favorites
    val filteredRecipes = remember(recipes, searchQuery, selectedCategory, showOnlyFavorites) {
        recipes.filter { recipe ->
            val matchesCategory = selectedCategory == "All" || recipe.category.equals(selectedCategory, ignoreCase = true)
            val matchesFavorite = !showOnlyFavorites || recipe.isFavorite
            val matchesSearch = if (searchQuery.isBlank()) true else {
                recipe.title.contains(searchQuery, ignoreCase = true) ||
                recipe.category.contains(searchQuery, ignoreCase = true) ||
                recipe.familyMemberName.contains(searchQuery, ignoreCase = true) ||
                recipe.description.contains(searchQuery, ignoreCase = true) ||
                recipe.ingredientsJson.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesFavorite && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KitchenTheme.CreamBackground)
    ) {
        // TOP ROW: Compact Title Info on Left | Dedicated Close Button (✕) on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KitchenTheme.CurryOrange.copy(alpha = 0.15f))
                        .border(1.dp, KitchenTheme.CurryOrangeLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = KitchenTheme.CurryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Kitchen & Curries",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KitchenTheme.TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(KitchenTheme.CurryOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${recipes.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Family dishes, steps & videos",
                        fontSize = 11.sp,
                        color = KitchenTheme.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Circular Close Button (✕)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(KitchenTheme.CreamSurface)
                    .border(1.dp, KitchenTheme.CreamBorder, CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = KitchenTheme.TextDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ACTION ROW: Search Bar (Weighted) + Prominent "+ Add" Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search curries, chef...", fontSize = 12.sp, color = KitchenTheme.TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KitchenTheme.CurryOrange, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = KitchenTheme.TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = defaultKitchenTextFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("kitchen_search_bar")
            )

            Button(
                onClick = { showAddRecipeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = KitchenTheme.CurryOrange),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("kitchen_add_recipe_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite Curries Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showOnlyFavorites) Color(0xFFFFE4E6) else KitchenTheme.CreamSurface)
                    .border(
                        width = 1.dp,
                        color = if (showOnlyFavorites) Color(0xFFF43F5E) else KitchenTheme.CreamBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showOnlyFavorites = !showOnlyFavorites }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("fav_filter_chip")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (showOnlyFavorites) Color(0xFFE11D48) else KitchenTheme.TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Favorite Curries",
                        fontSize = 11.sp,
                        fontWeight = if (showOnlyFavorites) FontWeight.Bold else FontWeight.Medium,
                        color = if (showOnlyFavorites) Color(0xFFE11D48) else KitchenTheme.TextDark
                    )
                }
            }

            // Categories
            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                val catColor = if (catKey == "All") KitchenTheme.CurryOrange else KitchenTheme.getCategoryColor(catKey)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) catColor else KitchenTheme.CreamSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) catColor else KitchenTheme.CreamBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCategory = catKey }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("category_chip_$catKey")
                ) {
                    Text(
                        text = catLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else KitchenTheme.TextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recipes List
        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(KitchenTheme.CreamSurface)
                    .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥘", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No recipes match '$searchQuery'"
                               else if (showOnlyFavorites) "No favorite curries saved yet"
                               else "No recipes in $selectedCategory",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KitchenTheme.TextDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add your family secret curries with ingredients, steps, and cooking videos!",
                        fontSize = 12.sp,
                        color = KitchenTheme.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank() || showOnlyFavorites || selectedCategory != "All") {
                                searchQuery = ""
                                showOnlyFavorites = false
                                selectedCategory = "All"
                            } else {
                                showAddRecipeDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KitchenTheme.CurryOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank() || showOnlyFavorites || selectedCategory != "All") "Clear Filters" else "+ Add First Recipe",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { selectedRecipeForDetail = recipe },
                        onToggleFavorite = {
                            viewModel.toggleFavoriteRecipe(recipe)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Recipe Detail Modal Dialog
    selectedRecipeForDetail?.let { currentRecipe ->
        RecipeDetailDialog(
            recipe = currentRecipe,
            onDismiss = { selectedRecipeForDetail = null },
            onEdit = {
                recipeToEdit = currentRecipe
                selectedRecipeForDetail = null
            },
            onDelete = {
                viewModel.deleteRecipe(currentRecipe)
                selectedRecipeForDetail = null
            },
            onToggleFavorite = {
                viewModel.toggleFavoriteRecipe(currentRecipe)
                selectedRecipeForDetail = currentRecipe.copy(isFavorite = !currentRecipe.isFavorite)
            }
        )
    }

    // Add or Edit Recipe Dialog
    if (showAddRecipeDialog || recipeToEdit != null) {
        AddEditRecipeDialog(
            initialRecipe = recipeToEdit,
            onDismiss = {
                showAddRecipeDialog = false
                recipeToEdit = null
            },
            onSave = { savedRecipe ->
                if (recipeToEdit != null) {
                    viewModel.updateRecipe(savedRecipe)
                } else {
                    viewModel.addRecipe(savedRecipe)
                }
                showAddRecipeDialog = false
                recipeToEdit = null
            }
        )
    }
}
