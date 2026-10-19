package com.example.ui.screens.kitchen

data class FoodPhotoPreset(
    val id: String,
    val name: String,
    val category: String,
    val imageUrl: String
)

object FoodPhotoPresets {
    val presets = listOf(
        FoodPhotoPreset(
            id = "p_chicken_1",
            name = "Spicy Chicken Curry",
            category = "Chicken",
            imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_chicken_2",
            name = "Butter Chicken Masala",
            category = "Chicken",
            imageUrl = "https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_mutton_1",
            name = "Mutton Rogan Josh",
            category = "Mutton",
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_mutton_2",
            name = "Slow Cooked Meat Stew",
            category = "Mutton",
            imageUrl = "https://images.unsplash.com/photo-1541832676-9b763b0239ab?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_fish_1",
            name = "Malabar Fish Curry",
            category = "Fish",
            imageUrl = "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_fish_2",
            name = "Seafood Curry & Herbs",
            category = "Fish",
            imageUrl = "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_veg_1",
            name = "Paneer Butter Masala",
            category = "Vegetarian",
            imageUrl = "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_veg_2",
            name = "Mixed Vegetable Korma",
            category = "Vegetarian",
            imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_egg_1",
            name = "Dhaba Egg Masala",
            category = "Egg",
            imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_dal_1",
            name = "Garlic Tadka Dal",
            category = "Dal",
            imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_drinks_1",
            name = "Kesar Mango Lassi",
            category = "Drinks",
            imageUrl = "https://images.unsplash.com/photo-1543362906-acfc16c67564?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_drinks_2",
            name = "Masala Spiced Chai",
            category = "Drinks",
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_bakers_1",
            name = "Pistachio Rose Cake",
            category = "Bakers",
            imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=800&q=80"
        ),
        FoodPhotoPreset(
            id = "p_bakers_2",
            name = "Warm Cardamom Buns",
            category = "Bakers",
            imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80"
        )
    )

    fun getDefaultUrlForCategory(category: String): String {
        return presets.firstOrNull { it.category.equals(category, ignoreCase = true) }?.imageUrl
            ?: "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=800&q=80"
    }
}
