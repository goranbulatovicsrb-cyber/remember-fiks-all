package com.podsetnikkapp.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import android.content.Context

enum class AppThemeMode(val label: String, val emoji: String) {
    DARK("Tamna", "🌙"),
    LIGHT("Svjetla", "☀️"),
    AMOLED("AMOLED Crna", "⬛"),
    DYNAMIC("Material You", "🎨"),
    BLUE("Plava tema", "🔵"),
    GREEN("Zelena tema", "🟢"),
    PINK("Roze tema", "🩷"),
    PURPLE("Ljubicasta tema", "🟣"),
    ORANGE("Narandzasta tema", "🟠"),
    RED("Crvena tema", "🔴")
}

object AppThemeColors {

    fun getColorScheme(mode: AppThemeMode, context: Context): ColorScheme {
        return when (mode) {
            AppThemeMode.DYNAMIC -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(context)
                } else {
                    darkPurpleScheme()
                }
            }
            AppThemeMode.LIGHT -> lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEADDFF),
                background = Color(0xFFFFFBFE),
                surface = Color(0xFFFFFBFE),
                onBackground = Color(0xFF1C1B1F),
                onSurface = Color(0xFF1C1B1F)
            )
            AppThemeMode.AMOLED -> darkColorScheme(
                primary = Color(0xFF9D4EDD),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF2D0B5A),
                background = Color(0xFF000000),
                surface = Color(0xFF0A0A0A),
                surfaceVariant = Color(0xFF111111),
                onBackground = Color.White,
                onSurface = Color.White,
                outline = Color(0xFF333333)
            )
            AppThemeMode.BLUE -> darkColorScheme(
                primary = Color(0xFF4CC9F0),
                onPrimary = Color(0xFF00344A),
                primaryContainer = Color(0xFF004D6B),
                background = Color(0xFF060E1A),
                surface = Color(0xFF0D1B2A),
                surfaceVariant = Color(0xFF112236),
                onBackground = Color.White,
                onSurface = Color.White,
                secondary = Color(0xFF00B4D8),
                outline = Color(0xFF1E3A5F)
            )
            AppThemeMode.GREEN -> darkColorScheme(
                primary = Color(0xFF4CAF50),
                onPrimary = Color(0xFF002106),
                primaryContainer = Color(0xFF003910),
                background = Color(0xFF06120A),
                surface = Color(0xFF0D1F12),
                surfaceVariant = Color(0xFF122918),
                onBackground = Color.White,
                onSurface = Color.White,
                secondary = Color(0xFF80CBC4),
                outline = Color(0xFF1B3D22)
            )
            AppThemeMode.PINK -> darkColorScheme(
                primary = Color(0xFFFF69B4),
                onPrimary = Color(0xFF3D0022),
                primaryContainer = Color(0xFF5C0034),
                background = Color(0xFF1A060F),
                surface = Color(0xFF250B18),
                surfaceVariant = Color(0xFF2E1020),
                onBackground = Color.White,
                onSurface = Color.White,
                secondary = Color(0xFFFF85C2),
                outline = Color(0xFF5C1A35)
            )
            AppThemeMode.ORANGE -> darkColorScheme(
                primary = Color(0xFFFF9800),
                onPrimary = Color(0xFF3D1C00),
                primaryContainer = Color(0xFF5C2C00),
                background = Color(0xFF1A0D00),
                surface = Color(0xFF251500),
                surfaceVariant = Color(0xFF2E1C00),
                onBackground = Color.White,
                onSurface = Color.White,
                secondary = Color(0xFFFFCC02),
                outline = Color(0xFF5C3300)
            )
            AppThemeMode.RED -> darkColorScheme(
                primary = Color(0xFFE53935),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF5C0A08),
                background = Color(0xFF1A0404),
                surface = Color(0xFF250808),
                surfaceVariant = Color(0xFF2E0D0D),
                onBackground = Color.White,
                onSurface = Color.White,
                secondary = Color(0xFFFF6B6B),
                outline = Color(0xFF5C1A1A)
            )
            else -> darkPurpleScheme()
        }
    }

    fun darkPurpleScheme() = darkColorScheme(
        primary = Color(0xFF9D4EDD),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF4F378B),
        background = Color(0xFF0F0F1A),
        surface = Color(0xFF1A1A2E),
        surfaceVariant = Color(0xFF16213E),
        onBackground = Color.White,
        onSurface = Color.White,
        secondary = Color(0xFF4CC9F0),
        outline = Color(0x33FFFFFF)
    )

    fun isLight(mode: AppThemeMode) = mode == AppThemeMode.LIGHT
}
