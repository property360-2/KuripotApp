package com.malikhain.kuripot_app.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

object ThemeUtils {
    const val THEME_KEY = "theme"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"
    
    @Composable
    fun isDarkTheme(themePreference: String): Boolean {
        return when (themePreference) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> isSystemInDarkTheme()
            else -> isSystemInDarkTheme()
        }
    }
} 