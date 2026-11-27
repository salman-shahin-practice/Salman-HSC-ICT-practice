package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Calm, High-Contrast Academic Palette (Restrained Teal + Neutral Ink + Semantic Status Only)
val TealPrimary = Color(0xFF0F625B)
val TealOnPrimary = Color(0xFFFFFFFF)
val TealPrimaryContainer = Color(0xFFDDF3F0)
val TealOnPrimaryContainer = Color(0xFF062B28)

val EmeraldSecondary = Color(0xFF0D7A5F)
val EmeraldOnSecondary = Color(0xFFFFFFFF)
val EmeraldSecondaryContainer = Color(0xFFE0F5EE)
val EmeraldOnSecondaryContainer = Color(0xFF062E23)

val AmberTertiary = Color(0xFFB45309)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFEF3C7)
val AmberOnTertiaryContainer = Color(0xFF451A03)

val LightBackground = Color(0xFFF6F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF2F6)
val LightOnSurface = Color(0xFF0B1324)
val LightOnSurfaceVariant = Color(0xFF1E293B)
val LightOutline = Color(0xFFCBD5E1)

// Dark Theme Tokens (Crisp Contrast)
val TealDarkPrimary = Color(0xFF5EEAD4)
val TealDarkOnPrimary = Color(0xFF042F2E)
val TealDarkPrimaryContainer = Color(0xFF134E4A)
val TealDarkOnPrimaryContainer = Color(0xFFCCFBF1)

val EmeraldDarkSecondary = Color(0xFF6EE7B7)
val EmeraldDarkOnSecondary = Color(0xFF022C22)
val EmeraldDarkSecondaryContainer = Color(0xFF065F46)
val EmeraldDarkOnSecondaryContainer = Color(0xFFD1FAE5)

val AmberDarkTertiary = Color(0xFFFCD34D)
val AmberDarkOnTertiary = Color(0xFF451A03)
val AmberDarkTertiaryContainer = Color(0xFF78350F)
val AmberDarkOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkBackground = Color(0xFF0B111E)
val DarkSurface = Color(0xFF131D2E)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFFE2E8F0)
val DarkOutline = Color(0xFF334155)

// Status & Semantic Accents (High-Contrast)
val SuccessGreen = Color(0xFF15803D)
val WarningAmber = Color(0xFFB45309)
val ErrorCrimson = Color(0xFFB91C1C)
val InfoSky = Color(0xFF0369A1)

// Editor Theme Palettes
data class EditorColorPalette(
    val background: Color,
    val gutterBackground: Color,
    val gutterText: Color,
    val text: Color,
    val keyword: Color,
    val tagOrType: Color,
    val stringOrAttr: Color,
    val number: Color,
    val comment: Color,
    val symbolBarBg: Color
)

val DarkSlateEditorPalette = EditorColorPalette(
    background = Color(0xFF0F172A),
    gutterBackground = Color(0xFF1E293B),
    gutterText = Color(0xFF94A3B8),
    text = Color(0xFFF1F5F9),
    keyword = Color(0xFF38BDF8),
    tagOrType = Color(0xFF2DD4BF),
    stringOrAttr = Color(0xFFFBBF24),
    number = Color(0xFFA78BFA),
    comment = Color(0xFF94A3B8),
    symbolBarBg = Color(0xFF1E293B)
)

val ClassicLightEditorPalette = EditorColorPalette(
    background = Color(0xFFFAFAFA),
    gutterBackground = Color(0xFFF1F5F9),
    gutterText = Color(0xFF64748B),
    text = Color(0xFF0B1324),
    keyword = Color(0xFF0369A1),
    tagOrType = Color(0xFF0F625B),
    stringOrAttr = Color(0xFFB45309),
    number = Color(0xFF6D28D9),
    comment = Color(0xFF475569),
    symbolBarBg = Color(0xFFE2E8F0)
)

val SolarizedWarmEditorPalette = EditorColorPalette(
    background = Color(0xFFFDF6E3),
    gutterBackground = Color(0xFFEEE8D5),
    gutterText = Color(0xFF657B83),
    text = Color(0xFF073642),
    keyword = Color(0xFF268BD2),
    tagOrType = Color(0xFF1F7A73),
    stringOrAttr = Color(0xFFCB4B16),
    number = Color(0xFF6C71C4),
    comment = Color(0xFF657B83),
    symbolBarBg = Color(0xFFEEE8D5)
)
