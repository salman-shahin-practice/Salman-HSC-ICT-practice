package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Academic Teal & Emerald & Warm Gold Palette
val TealPrimary = Color(0xFF0F766E)
val TealOnPrimary = Color(0xFFFFFFFF)
val TealPrimaryContainer = Color(0xFFCCFBF1)
val TealOnPrimaryContainer = Color(0xFF042F2E)

val EmeraldSecondary = Color(0xFF059669)
val EmeraldOnSecondary = Color(0xFFFFFFFF)
val EmeraldSecondaryContainer = Color(0xFFD1FAE5)
val EmeraldOnSecondaryContainer = Color(0xFF022C22)

val AmberTertiary = Color(0xFFD97706)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFEF3C7)
val AmberOnTertiaryContainer = Color(0xFF451A03)

val LightBackground = Color(0xFFF5F8F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE7F0EF)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)

// Dark Theme Tokens
val TealDarkPrimary = Color(0xFF2DD4BF)
val TealDarkOnPrimary = Color(0xFF042F2E)
val TealDarkPrimaryContainer = Color(0xFF115E59)
val TealDarkOnPrimaryContainer = Color(0xFFCCFBF1)

val EmeraldDarkSecondary = Color(0xFF34D399)
val EmeraldDarkOnSecondary = Color(0xFF022C22)
val EmeraldDarkSecondaryContainer = Color(0xFF065F46)
val EmeraldDarkOnSecondaryContainer = Color(0xFFD1FAE5)

val AmberDarkTertiary = Color(0xFFFBBF24)
val AmberDarkOnTertiary = Color(0xFF451A03)
val AmberDarkTertiaryContainer = Color(0xFF78350F)
val AmberDarkOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkBackground = Color(0xFF09101D)
val DarkSurface = Color(0xFF111B2D)
val DarkSurfaceVariant = Color(0xFF1B2940)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Status & Semantic Accents
val SuccessGreen = Color(0xFF16A34A)
val WarningAmber = Color(0xFFF59E0B)
val ErrorCrimson = Color(0xFFDC2626)
val InfoSky = Color(0xFF0284C7)

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
    gutterText = Color(0xFF64748B),
    text = Color(0xFFE2E8F0),
    keyword = Color(0xFF38BDF8),
    tagOrType = Color(0xFF2DD4BF),
    stringOrAttr = Color(0xFFFBBF24),
    number = Color(0xFFA78BFA),
    comment = Color(0xFF64748B),
    symbolBarBg = Color(0xFF1E293B)
)

val ClassicLightEditorPalette = EditorColorPalette(
    background = Color(0xFFFAFAFA),
    gutterBackground = Color(0xFFF1F5F9),
    gutterText = Color(0xFF94A3B8),
    text = Color(0xFF0F172A),
    keyword = Color(0xFF0284C7),
    tagOrType = Color(0xFF0F766E),
    stringOrAttr = Color(0xFFB45309),
    number = Color(0xFF7C3AED),
    comment = Color(0xFF64748B),
    symbolBarBg = Color(0xFFE2E8F0)
)

val SolarizedWarmEditorPalette = EditorColorPalette(
    background = Color(0xFFFDF6E3),
    gutterBackground = Color(0xFFEEE8D5),
    gutterText = Color(0xFF93A1A1),
    text = Color(0xFF073642),
    keyword = Color(0xFF268BD2),
    tagOrType = Color(0xFF2AA198),
    stringOrAttr = Color(0xFFCB4B16),
    number = Color(0xFF6C71C4),
    comment = Color(0xFF93A1A1),
    symbolBarBg = Color(0xFFEEE8D5)
)
