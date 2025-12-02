package com.example.eventecho.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a single DataStore instance
val Context.dataStore by preferencesDataStore(name = "settings")

// Preference key
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

// Read dark mode preference
fun Context.readDarkMode(): Flow<Boolean> =
    dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false
    }

// Write dark mode preference
suspend fun Context.setDarkMode(enabled: Boolean) {
    dataStore.edit { prefs ->
        prefs[DARK_MODE_KEY] = enabled
    }
}