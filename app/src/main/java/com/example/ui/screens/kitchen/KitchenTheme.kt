package com.example.ui.screens.kitchen

import androidx.compose.ui.graphics.Color

object KitchenTheme {
    // Warm Cream Backgrounds
    val CreamBackground = Color(0xFFFFFDF8)      // Ultra-clean warm cream
    val CreamSurface = Color(0xFFFFF7ED)         // Soft warm butter cream
    val CreamSurfaceAlt = Color(0xFFFEF3C7)      // Golden cream
    val CreamCard = Color(0xFFFFFFFF)            // Pure card surface with warm tint
    val CreamBorder = Color(0xFFFFEDD5)          // Subtle orange-cream border
    val CreamBorderFocus = Color(0xFFF97316)     // Vibrant warm border

    // Curry-Orange Accents
    val CurryOrange = Color(0xFFEA580C)          // Rich authentic curry orange
    val CurryOrangeDark = Color(0xFFC2410C)      // Deep paprika orange-red
    val CurryOrangeLight = Color(0xFFFB923C)     // Saffron bright orange
    val TurmericGold = Color(0xFFD97706)         // Rich turmeric gold
    val TurmericGoldLight = Color(0xFFFBBF24)    // Golden spice highlight
    val CorianderGreen = Color(0xFF15803D)       // Fresh herb green

    // High Contrast Typography for warm theme
    val TextDark = Color(0xFF1C1917)             // Warm stone black for maximum clarity
    val TextMuted = Color(0xFF78716C)            // Warm stone gray
    val TextSubtle = Color(0xFFA8A29E)           // Light stone gray

    // Category Color Palette for colorful pills
    fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "chicken" -> Color(0xFFEA580C)
            "mutton" -> Color(0xFFB91C1C)
            "fish" -> Color(0xFF0284C7)
            "vegetarian" -> Color(0xFF16A34A)
            "egg" -> Color(0xFFD97706)
            "dal" -> Color(0xFFEAB308)
            "drinks" -> Color(0xFFEC4899)
            "bakers" -> Color(0xFF9333EA)
            else -> Color(0xFFF97316)
        }
    }

    fun getCategoryEmoji(category: String): String {
        return when (category.lowercase()) {
            "chicken" -> "🍗"
            "mutton" -> "🍖"
            "fish" -> "🐟"
            "vegetarian" -> "🥬"
            "egg" -> "🥚"
            "dal" -> "🍲"
            "drinks" -> "🍹"
            "bakers" -> "🧁"
            else -> "🥘"
        }
    }
}
