package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

val VaultBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.background

val VaultSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.surface

val VaultSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.surfaceVariant

val VaultSurfaceHighlight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.surfaceHighlight

val VaultPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.primary

val VaultPrimaryLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.primaryLight

val VaultPrimaryDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.primary

val VaultPrimaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.primaryContainer

val UrgentRed = Color(0xFFE11D48)
val UrgentRedBg = Color(0x22E11D48)
val WarningYellow = Color(0xFFD97706)
val WarningYellowBg = Color(0x22D97706)
val SafeGreen = Color(0xFF10B981)
val SafeGreenBg = Color(0x2610B981)

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.textMuted

val BorderSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.borderSubtle

val BorderAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.borderAccent
