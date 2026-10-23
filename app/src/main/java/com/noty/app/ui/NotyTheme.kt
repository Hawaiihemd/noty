package com.noty.app.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.noty.app.utils.ThemeManager
// ─── Theme ───────────────────────────────────────────────────────────────────

@Composable
fun NotyTheme(
    themeMode: ThemeManager.ThemeMode = ThemeManager.ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeManager.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeManager.ThemeMode.LIGHT -> false
        ThemeManager.ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFFBFC2FF),
            secondary = Color(0xFF4CDADA),
            surface = Color(0xFF1A1B1E),
            background = Color(0xFF1A1B1E)
        )
        else -> lightColorScheme(
            primary = Color(0xFF4F5CD3),
            secondary = Color(0xFF006A6A),
            surface = Color(0xFFFDFBFF),
            background = Color(0xFFFDFBFF)
        )
    }

    // The status and navigation bar icons are the one piece of chrome Compose does not draw,
    // so they have to be told which way to go. Previously this came free from
    // AppCompatDelegate.setDefaultNightMode() changing the resource configuration; now that
    // the theme is a plain parameter, the window would keep the *system's* light/dark icons
    // and a user on "Dark" under a light system would get dark icons on a dark bar.
    val view = LocalView.current
    // as? rather than as: there is no window to configure in a @Preview, and none either
    // if this theme is ever hosted by a ComposeView outside an Activity.
    val window = (view.context as? Activity)?.window
    if (window != null && !view.isInEditMode) {
        SideEffect {
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
