package com.hueiq.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hueiq.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Top-level singleton DataStore — one instance per app process
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Repository that reads/writes user data from local DataStore.
 * No network calls — all data stays on the device.
 */
class UserRepository(private val context: Context) {

    private object Keys {
        val USER_ID      = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL        = stringPreferencesKey("email")
        val PHOTO_URL    = stringPreferencesKey("photo_url")
        val THEME_MODE   = stringPreferencesKey("theme_mode")
    }

    /** Emits the saved UserData, or null if nobody is signed in. */
    val userFlow: Flow<UserData?> = context.userDataStore.data
        .catch { cause ->
            // If the file is corrupted, emit empty so we fall back to sign-in
            if (cause is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw cause
        }
        .map { prefs ->
            val userId = prefs[Keys.USER_ID] ?: return@map null
            UserData(
                userId      = userId,
                displayName = prefs[Keys.DISPLAY_NAME] ?: userId.substringBefore("@"),
                email       = prefs[Keys.EMAIL] ?: userId,
                photoUrl    = prefs[Keys.PHOTO_URL]
            )
        }

    /** Emits the current ThemeMode preference (defaults to SYSTEM). */
    val themeFlow: Flow<ThemeMode> = context.userDataStore.data
        .catch { cause ->
            if (cause is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw cause
        }
        .map { prefs ->
            when (prefs[Keys.THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK"  -> ThemeMode.DARK
                else    -> ThemeMode.SYSTEM
            }
        }

    /** Cycles SYSTEM → LIGHT → DARK → SYSTEM. */
    suspend fun cycleTheme(current: ThemeMode) {
        val next = when (current) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT  -> ThemeMode.DARK
            ThemeMode.DARK   -> ThemeMode.SYSTEM
        }
        context.userDataStore.edit { it[Keys.THEME_MODE] = next.name }
    }

    /** Persists user data after a successful Google Sign-In. */
    suspend fun saveUser(user: UserData) {
        context.userDataStore.edit { prefs ->
            prefs[Keys.USER_ID]     = user.userId
            prefs[Keys.DISPLAY_NAME] = user.displayName
            prefs[Keys.EMAIL]       = user.email
            user.photoUrl?.let { prefs[Keys.PHOTO_URL] = it }
        }
    }

    /** Clears all user data on sign-out. */
    suspend fun clearUser() {
        context.userDataStore.edit { it.clear() }
    }
}
