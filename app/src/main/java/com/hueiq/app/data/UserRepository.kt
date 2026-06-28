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
        val SAVED_COLORS = stringPreferencesKey("saved_colors")
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

    /**
     * Emits the list of colors the user has saved from the scanner.
     * Encoded as "name|r|g|b" entries joined by ";".
     */
    val savedColorsFlow: Flow<List<SavedColor>> = context.userDataStore.data
        .catch { cause ->
            if (cause is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw cause
        }
        .map { prefs ->
            val raw = prefs[Keys.SAVED_COLORS] ?: return@map emptyList()
            raw.split(";").filter { it.isNotBlank() }.mapNotNull { entry ->
                val p = entry.split("|")
                if (p.size == 4) {
                    val r = p[1].toIntOrNull() ?: return@mapNotNull null
                    val g = p[2].toIntOrNull() ?: return@mapNotNull null
                    val b = p[3].toIntOrNull() ?: return@mapNotNull null
                    SavedColor(p[0], r, g, b, "#%02X%02X%02X".format(r, g, b))
                } else null
            }
        }

    /** Appends a color to the saved list; ignores duplicates by RGB value. */
    suspend fun saveColor(color: SavedColor) {
        context.userDataStore.edit { prefs ->
            val existing = prefs[Keys.SAVED_COLORS] ?: ""
            val isDuplicate = existing.split(";").filter { it.isNotBlank() }.any { entry ->
                val p = entry.split("|")
                p.size == 4 && p[1] == color.r.toString() &&
                        p[2] == color.g.toString() && p[3] == color.b.toString()
            }
            if (!isDuplicate) {
                val encoded = "${color.name}|${color.r}|${color.g}|${color.b}"
                prefs[Keys.SAVED_COLORS] = if (existing.isBlank()) encoded else "$existing;$encoded"
            }
        }
    }

    /** Removes a saved color by its hex value. */
    suspend fun removeColor(hex: String) {
        context.userDataStore.edit { prefs ->
            val existing = prefs[Keys.SAVED_COLORS] ?: return@edit
            val filtered = existing.split(";").filter { it.isNotBlank() }.filter { entry ->
                val p = entry.split("|")
                if (p.size == 4) {
                    val r = p[1].toIntOrNull() ?: return@filter true
                    val g = p[2].toIntOrNull() ?: return@filter true
                    val b = p[3].toIntOrNull() ?: return@filter true
                    "#%02X%02X%02X".format(r, g, b) != hex.uppercase()
                } else true
            }
            prefs[Keys.SAVED_COLORS] = filtered.joinToString(";")
        }
    }

    /** Clears session data on sign-out. Theme and saved colors are preserved. */
    suspend fun clearUser() {
        context.userDataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.DISPLAY_NAME)
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.PHOTO_URL)
        }
    }
}
