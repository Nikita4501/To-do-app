package com.example.todoapp.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object Keys {
    val NIGHT_MODE = intPreferencesKey("night_mode")
}

class SettingsRepository(private val context: Context) {

    val nightMode: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[Keys.NIGHT_MODE] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM }

    suspend fun setNightMode(mode: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.NIGHT_MODE] = mode }
    }
}
