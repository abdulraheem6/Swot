package com.example.ui.screens.kitchen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.kitchen.RecipeEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeDialog(
    initialRecipe: RecipeEntity? = null,
    onDismiss: () -> Unit,
    onSave: (RecipeEntity) -> Unit
) {
    val isEditing = initialRecipe != null

    var title by remember { mutableStateOf(initialRecipe?.title ?: "") }
    var category by remember { mutableStateOf(initialRecipe?.category ?: "Chicken") }
    var description by remember { mutableStateOf(initialRecipe?.description ?: "") }
    var cookingTimeMinutes by remember { mutableIntStateOf(initialRecipe?.cookingTimeMinutes ?: 35) }
    var servings by remember { mutableIntStateOf(initialRecipe?.servings ?: 4) }
    var familyMemberName by remember { mutableStateOf(initialRecipe?.familyMemberName ?: "") }
    var photoUri by remember { mutableStateOf(initialRecipe?.photoUri ?: "") }
    var youtubeUrl by remember { mutableStateOf(initialRecipe?.youtubeUrl ?: "") }
    var localVideoUri by remember { mutableStateOf(initialRecipe?.localVideoUri ?: "") }
    var isFavorite by remember { mutableStateOf(initialRecipe?.isFavorite ?: false) }

    // Ingredients as newline-separated text for easy editing
    var ingredientsText by remember {
        mutableStateOf(
            initialRecipe?.getIngredientsList()?.joinToString("\n") ?: ""
        )
    }

    // Steps as newline-separated text
    var stepsText by remember {
        mutableStateOf(
            initialRecipe?.getStepsList()?.joinToString("\n") ?: ""
        )
    }

    // Photo picker launcher (Gallery or Camera)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    // Video picker launcher for recording/selecting local cooking clip
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            localVideoUri = uri.toString()
        }
    }

    val availableCategories = listOf(
        "Chicken", "Mutton", "Fish", "Vegetarian", "Egg", "Dal", "Drinks", "Bakers", "Other"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KitchenTheme.CreamBackground,
        modifier = Modifier.testTag("add_edit_recipe_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditing) "Edit Recipe" else "Add New Curry / Dish",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KitchenTheme.TextDark
                    )
                    Text(
                        text = "Store ingredients, steps, family chef & cooking video",
                        fontSize = 11.sp,
                        color = KitchenTheme.TextMuted
                    )
                }

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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Selection Area
            Text("📸 Food Photo / Preset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.CurryOrange)
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(KitchenTheme.CreamSurfaceAlt)
                    .border(1.5.dp, KitchenTheme.CreamBorderFocus, RoundedCornerShape(16.dp))
            ) {
                val displayUrl = if (photoUri.isNotBlank()) photoUri else FoodPhotoPresets.getDefaultUrlForCategory(category)
                AsyncImage(
                    model = displayUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Pick from phone button overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KitchenTheme.CurryOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("pick_photo_btn")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Photo Presets Carousel
            Spacer(modifier = Modifier.height(8.dp))
            Text("Or tap a delicious food preset:", fontSize = 10.sp, color = KitchenTheme.TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FoodPhotoPresets.presets.take(8).forEach { preset ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamCard),
                        modifier = Modifier
                            .size(64.dp)
                            .border(
                                width = if (photoUri == preset.imageUrl) 2.dp else 1.dp,
                                color = if (photoUri == preset.imageUrl) KitchenTheme.CurryOrange else KitchenTheme.CreamBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { photoUri = preset.imageUrl }
                    ) {
                        AsyncImage(
                            model = preset.imageUrl,
                            contentDescription = preset.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title") },
                placeholder = { Text("e.g. Amma's Spicy Chicken Curry") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("recipe_title_input"),
                colors = defaultKitchenTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Selector Chips
            Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.CurryOrange)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) KitchenTheme.getCategoryColor(cat) else KitchenTheme.CreamSurface)
                            .border(1.dp, if (isSelected) KitchenTheme.getCategoryColor(cat) else KitchenTheme.CreamBorder, RoundedCornerShape(12.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("cat_chip_$cat")
                    ) {
                        Text(
                            text = "${KitchenTheme.getCategoryEmoji(cat)} $cat",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else KitchenTheme.TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Family Member Name
            OutlinedTextField(
                value = familyMemberName,
                onValueChange = { familyMemberName = it },
                label = { Text("Family Member / Chef Name") },
                placeholder = { Text("e.g. Grandma Amma, Mom, Uncle Ramesh") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("recipe_chef_input"),
                colors = defaultKitchenTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cooking Time & Servings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cookingTimeMinutes.toString(),
                    onValueChange = { cookingTimeMinutes = it.toIntOrNull() ?: 0 },
                    label = { Text("Time (Minutes)") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = KitchenTheme.TurmericGold, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("recipe_time_input"),
                    colors = defaultKitchenTextFieldColors()
                )

                OutlinedTextField(
                    value = servings.toString(),
                    onValueChange = { servings = it.toIntOrNull() ?: 1 },
                    label = { Text("Servings") },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = KitchenTheme.CurryOrangeLight, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("recipe_servings_input"),
                    colors = defaultKitchenTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / Story
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Heritage Story") },
                placeholder = { Text("Notes about spice levels, special occasions, or memories...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = defaultKitchenTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients (Line by Line)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🥗 Ingredients (1 item per line)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.CurryOrange)
                TextButton(
                    onClick = {
                        ingredientsText = "1 kg Chicken\n2 Onions, sliced\n2 tbsp Ginger-Garlic paste\n1 tsp Turmeric\n2 tsp Kashmiri Chilli\n1 cup Coconut milk\nCurry leaves\nSalt to taste"
                    }
                ) {
                    Text("Load Sample", fontSize = 11.sp, color = KitchenTheme.TurmericGold)
                }
            }
            OutlinedTextField(
                value = ingredientsText,
                onValueChange = { ingredientsText = it },
                placeholder = { Text("1 kg Chicken\n2 Onions, sliced\n2 tbsp Ginger-Garlic paste\n1 tsp Turmeric\nSalt to taste") },
                modifier = Modifier.fillMaxWidth().testTag("recipe_ingredients_input"),
                minLines = 4,
                colors = defaultKitchenTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cooking Steps (Line by Line)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🍳 Step-by-Step Instructions (1 step per line)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.CurryOrange)
                TextButton(
                    onClick = {
                        stepsText = "Marinate chicken with turmeric, chilli, ginger-garlic and salt for 30 mins.\nHeat oil, crackle mustard seeds, curry leaves, and sauté sliced onions until golden.\nAdd marinated chicken and sear on high flame for 5 minutes.\nAdd coconut milk, cover and simmer on low heat for 20 minutes until tender.\nGarnish with fresh cilantro and serve hot."
                    }
                ) {
                    Text("Load Sample", fontSize = 11.sp, color = KitchenTheme.TurmericGold)
                }
            }
            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                placeholder = { Text("Step 1: Marinate meat\nStep 2: Sauté onions and spices\nStep 3: Simmer until tender") },
                modifier = Modifier.fillMaxWidth().testTag("recipe_steps_input"),
                minLines = 4,
                colors = defaultKitchenTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Media Links & Video Attachments
            Text("🎥 Attach Videos (YouTube & Phone Recordings)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.CurryOrange)
            Spacer(modifier = Modifier.height(8.dp))

            // YouTube URL
            OutlinedTextField(
                value = youtubeUrl,
                onValueChange = { youtubeUrl = it },
                label = { Text("YouTube Video Link (Optional)") },
                placeholder = { Text("https://www.youtube.com/watch?v=...") },
                leadingIcon = { Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("recipe_youtube_input"),
                colors = defaultKitchenTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Local Recorded Phone Video Attachment
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KitchenTheme.CreamSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = KitchenTheme.CurryOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Recorded Phone Video", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.TextDark)
                                Text(
                                    text = if (localVideoUri.isNotBlank()) "Video Attached (Plays in-app)" else "Attach cooking clip from camera / gallery",
                                    fontSize = 10.sp,
                                    color = if (localVideoUri.isNotBlank()) KitchenTheme.CorianderGreen else KitchenTheme.TextMuted
                                )
                            }
                        }

                        Button(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KitchenTheme.CurryOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("attach_video_btn")
                        ) {
                            Text(if (localVideoUri.isBlank()) "Attach Video" else "Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (localVideoUri.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(localVideoUri.takeLast(35), fontSize = 10.sp, color = KitchenTheme.TextMuted, maxLines = 1)
                            TextButton(onClick = { localVideoUri = "" }) {
                                Text("Remove Video", fontSize = 10.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Favorite Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KitchenTheme.CreamSurface)
                    .border(1.dp, KitchenTheme.CreamBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color(0xFFFF3366) else KitchenTheme.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Add to Favorite Curries", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KitchenTheme.TextDark)
                        Text("Pin to top of your kitchen collection", fontSize = 10.sp, color = KitchenTheme.TextMuted)
                    }
                }

                Switch(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = KitchenTheme.CurryOrange
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isBlank()) return@Button

                    val ingredientsList = ingredientsText.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
                    val stepsList = stepsText.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }

                    val finalPhoto = if (photoUri.isNotBlank()) photoUri else FoodPhotoPresets.getDefaultUrlForCategory(category)

                    val newRecipe = (initialRecipe ?: RecipeEntity(
                        id = UUID.randomUUID().toString(),
                        title = title.trim(),
                        category = category
                    )).copy(
                        title = title.trim(),
                        category = category,
                        description = description.trim(),
                        ingredientsJson = RecipeEntity.listToJson(ingredientsList),
                        stepsJson = RecipeEntity.listToJson(stepsList),
                        photoUri = finalPhoto,
                        cookingTimeMinutes = cookingTimeMinutes,
                        servings = servings,
                        familyMemberName = familyMemberName.trim(),
                        youtubeUrl = youtubeUrl.trim(),
                        localVideoUri = localVideoUri.trim(),
                        isFavorite = isFavorite
                    )

                    onSave(newRecipe)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = KitchenTheme.CurryOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_recipe_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Recipe" else "Save to Kitchen Vault",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun defaultKitchenTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = KitchenTheme.TextDark,
    unfocusedTextColor = KitchenTheme.TextDark,
    focusedBorderColor = KitchenTheme.CurryOrange,
    unfocusedBorderColor = KitchenTheme.CreamBorder,
    focusedLabelColor = KitchenTheme.CurryOrange,
    unfocusedLabelColor = KitchenTheme.TextMuted,
    focusedContainerColor = KitchenTheme.CreamCard,
    unfocusedContainerColor = KitchenTheme.CreamCard
)
