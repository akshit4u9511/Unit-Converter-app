package com.example.unitconverter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// NOTE: The new custom colors (CyanPrimary, BlackBackground, etc.) are used here
// They are imported implicitly from the package scope (Color.kt).

private val DarkColorScheme = darkColorScheme(
    // --- BLACK AND CYAN PALETTE ---
    primary = CyanPrimary,          // Primary active color (Bright Cyan)
    onPrimary = Color.Black,        // Text color on primary background (Black for contrast)

    secondary = CyanDark,           // Secondary color (Darker Cyan)
    onSecondary = Color.Black,

    // Background and Surface (Black and near-black)
    background = BlackBackground,   // Pure Black
    onBackground = OnDark,          // White text
    surface = DarkSurface,          // Near-black cards/containers
    onSurface = OnDark,             // White text on cards

    // Color for FilledTonalButton when unselected (if SurfaceVariant is used)
    surfaceVariant = DarkSurface,
    onSurfaceVariant = Color.LightGray
)

private val LightColorScheme = lightColorScheme(
    // --- CYAN FOR PRIMARY IN LIGHT MODE ---
    primary = CyanPrimary,
    onPrimary = Color.Black,
    secondary = CyanDark,
    onSecondary = Color.Black,

    // Default light theme colors for the rest
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun UnitConverterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // FIX: Set dynamicColor to false to ensure the custom DarkColorScheme is always used
    // when the system is in dark mode, regardless of Android version.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // This dynamic color check is now effectively skipped due to dynamicColor = false
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // The app will now fall back to the custom scheme based on system setting
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use the status bar color that matches the background/surface
            window.statusBarColor = colorScheme.background.toArgb()
            // Set status bar icons to be light (visible on a dark status bar)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography is defined elsewhere
        content = content
    )
}
