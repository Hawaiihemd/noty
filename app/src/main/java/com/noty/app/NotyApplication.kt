package com.noty.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.noty.app.utils.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val themeManager = ThemeManager(this)
        CoroutineScope(Dispatchers.Main).launch {
            themeManager.themeFlow.collect { theme ->
                applyTheme(theme)
            }
        }
    }
    
    private fun applyTheme(themeMode: ThemeManager.ThemeMode) {
        val mode = when (themeMode) {
            ThemeManager.ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeManager.ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeManager.ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
