package com.yogeshpaliyal.comrade.ui.theme

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    private val _currentTheme = mutableStateOf(ThemeMode.SYSTEM)
    val currentTheme: State<ThemeMode> = _currentTheme

    fun init(context: Context) {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedTheme = preferences.getString("theme_mode", ThemeMode.SYSTEM.name)
        _currentTheme.value = ThemeMode.valueOf(savedTheme ?: ThemeMode.SYSTEM.name)
    }

    fun setTheme(context: Context, themeMode: ThemeMode) {
        _currentTheme.value = themeMode
        // Also save to preferences for persistence across app restarts
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        preferences.edit().putString("theme_mode", themeMode.name).apply()
    }
}
