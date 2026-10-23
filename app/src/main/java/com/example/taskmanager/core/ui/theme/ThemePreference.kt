package com.example.taskmanager.core.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore by preferencesDataStore("settings")

@Singleton
class ThemePreference @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = stringPreferencesKey("theme_mode")

    val mode: Flow<ThemeMode> = context.settingsStore.data.map { prefs ->
        prefs[key]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun set(mode: ThemeMode) {
        context.settingsStore.edit { it[key] = mode.name }
    }
}
