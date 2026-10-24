package io.github.humayanx.lastdrop.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

enum class RetentionPeriod(val days: Int, val label: String) {
    DAYS_7(7, "7 Days"),
    DAYS_14(14, "14 Days"),
    DAYS_30(30, "30 Days")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val retentionPeriod: RetentionPeriod = RetentionPeriod.DAYS_30,
    val setupCompleted: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val RETENTION_PERIOD = stringPreferencesKey("retention_period")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { preferences ->
        val themeModeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val retentionName = preferences[PreferencesKeys.RETENTION_PERIOD] ?: RetentionPeriod.DAYS_30.name

        val themeMode = runCatching { ThemeMode.valueOf(themeModeName) }.getOrDefault(ThemeMode.SYSTEM)
        val retention = runCatching { RetentionPeriod.valueOf(retentionName) }.getOrDefault(RetentionPeriod.DAYS_30)

        UserPreferences(
            themeMode = themeMode,
            retentionPeriod = retention,
            setupCompleted = preferences[PreferencesKeys.SETUP_COMPLETED] ?: false
        )
    }

    suspend fun completeSetup() {
        context.dataStore.edit { it[PreferencesKeys.SETUP_COMPLETED] = true }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setRetentionPeriod(period: RetentionPeriod) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RETENTION_PERIOD] = period.name
        }
    }
}
