package com.podsetnikkapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.podsetnikkapp.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
        } catch (e: Exception) { AppThemeMode.DARK }
    )
    val themeMode = _themeMode.asStateFlow()

    fun setTheme(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(app) as T
        }
    }
}
