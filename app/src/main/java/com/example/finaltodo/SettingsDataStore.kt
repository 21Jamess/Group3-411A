package com.example.finaltodo

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

class SettingsDataStore(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map {
        preferences -> preferences[DARK_MODE_KEY] ?: false
    }

    suspend fun saveDarkModeSetting(isDarkMode: Boolean) {
        context.dataStore.edit { settings -> settings[DARK_MODE_KEY] = isDarkMode
        }
    }
}