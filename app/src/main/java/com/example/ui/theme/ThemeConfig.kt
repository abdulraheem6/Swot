package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class AppThemeConfig(
    val id: String,
    val name: String,
    val previewEmoji: String,
    val isLight: Boolean = false,
    val primary: Color,
    val primaryLight: Color,
    val primaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHighlight: Color,
    val textPrimary: Color = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC),
    val textSecondary: Color = if (isLight) Color(0xFF334155) else Color(0xFFCBD5E1),
    val textMuted: Color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8),
    val borderSubtle: Color = if (isLight) Color(0x1F000000) else Color(0x2EFFFFFF),
    val borderAccent: Color = if (isLight) Color(0x402563EB) else Color(0x5538BDF8),
    val gradientColors: List<Color>,
    val description: String = ""
)

object ThemeRegistry {
    val themes = listOf(
        AppThemeConfig(
            id = "bright_light",
            name = "Bright Mode",
            previewEmoji = "☀️",
            isLight = true,
            primary = Color(0xFF2563EB), // Electric Royal Blue
            primaryLight = Color(0xFF3B82F6),
            primaryContainer = Color(0xFFEFF6FF), // Soft Blue tint
            background = Color(0xFFF8FAFC), // Pure Crisp Slate-50
            surface = Color(0xFFFFFFFF), // Pure White
            surfaceVariant = Color(0xFFF1F5F9), // Slate-100
            surfaceHighlight = Color(0xFFE2E8F0), // Slate-200
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF1E293B),
            textMuted = Color(0xFF475569),
            borderSubtle = Color(0x1F0F172A),
            borderAccent = Color(0x332563EB),
            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8)),
            description = "Crisp, bright daylight mode with clean high-contrast enterprise styling"
        ),
        AppThemeConfig(
            id = "dark_modern",
            name = "Dark Mode",
            previewEmoji = "🌙",
            isLight = false,
            primary = Color(0xFF38BDF8), // Electric Sky Blue
            primaryLight = Color(0xFF818CF8),
            primaryContainer = Color(0xFF1E293B),
            background = Color(0xFF0B0F19), // Deep Obsidian
            surface = Color(0xFF111827), // Sleek Card Surface
            surfaceVariant = Color(0xFF1E293B), // Slate-800
            surfaceHighlight = Color(0xFF334155), // Slate-700
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFCBD5E1),
            textMuted = Color(0xFF94A3B8),
            borderSubtle = Color(0x2EFFFFFF),
            borderAccent = Color(0x5538BDF8),
            gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8)),
            description = "Ultra-modern deep OLED dark mode with crystal-clear readability"
        )
    )

    fun getTheme(id: String): AppThemeConfig {
        val normalized = id.lowercase()
        return when {
            normalized.contains("dark") || 
            normalized in listOf("indigo", "blue", "gold", "titanium", "emerald", "pink", "orange") -> {
                themes[1] // Dark Mode
            }
            else -> {
                themes[0] // Bright Mode (Default)
            }
        }
    }
}

