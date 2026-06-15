package com.example

import androidx.compose.ui.graphics.Color

enum class DashboardTheme(
    val title: String,
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glowColor: Color,
    val isLight: Boolean
) {
    IMMERSIVE(
        "Immersive Blue", Color(0xFF050608), Color(0xFF0F1115), Color(0xFF3B82F6), Color(0xFF10B981), Color.White, Color(0xFF64748B), Color(0xFF3B82F6), false
    ),
    CYBERPUNK(
        "Cyberpunk Neon", Color(0xFF0D0221), Color(0xFF1A0A33), Color(0xFF00FFCC), Color(0xFFFF007F), Color(0xFFE0E0E0), Color(0xFFB399FF), Color(0xFFFF007F), false
    ),
    RACING(
        "Racing Red", Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFFEF4444), Color(0xFFF97316), Color.White, Color(0xFFAAAAAA), Color(0xFFEF4444), false
    ),
    MINIMAL_LIGHT(
        "Minimal Light", Color(0xFFF1F5F9), Color(0xFFFFFFFF), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF0F172A), Color(0xFF64748B), Color.Transparent, true
    ),
    OLED_NIGHT(
        "OLED Night", Color.Black, Color(0xFF0A0A0A), Color(0xFFF97316), Color(0xFFEAB308), Color.White, Color(0xFF94A3B8), Color(0xFFF97316), false
    )
}
