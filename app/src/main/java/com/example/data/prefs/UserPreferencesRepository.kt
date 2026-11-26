package com.example.data.prefs

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.EditorThemeMode
import com.example.data.model.UserRole
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hsc_ict_prefs")

data class UserSettings(
    val isDarkMode: Boolean = false,
    val editorFontSizeSp: Int = 14,
    val editorTheme: EditorThemeMode = EditorThemeMode.DARK_SLATE,
    val studentName: String = "HSC শিক্ষার্থী",
    val activeRole: UserRole = UserRole.STUDENT,
    val hasAdminPinConfigured: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val lastSyncedAt: Long = 0L,
    val instructorPhotoPath: String? = null,
    val instructorPhotoVersion: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
        val EDITOR_THEME = stringPreferencesKey("editor_theme")
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val ACTIVE_ROLE = stringPreferencesKey("active_role")
        val ADMIN_PIN_HASH = stringPreferencesKey("admin_pin_hash")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
        val INSTRUCTOR_PHOTO_PATH = stringPreferencesKey("instructor_photo_path")
        val INSTRUCTOR_PHOTO_VERSION = longPreferencesKey("instructor_photo_version")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: false,
            editorFontSizeSp = (prefs[Keys.EDITOR_FONT_SIZE] ?: 14).coerceIn(12, 22),
            editorTheme = prefs[Keys.EDITOR_THEME]?.let { raw ->
                EditorThemeMode.entries.firstOrNull { it.name == raw }
            } ?: EditorThemeMode.DARK_SLATE,
            studentName = prefs[Keys.STUDENT_NAME]?.takeIf { it.isNotBlank() } ?: "HSC শিক্ষার্থী",
            activeRole = prefs[Keys.ACTIVE_ROLE]?.let { raw ->
                UserRole.entries.firstOrNull { it.name == raw }
            } ?: UserRole.STUDENT,
            hasAdminPinConfigured = !prefs[Keys.ADMIN_PIN_HASH].isNullOrBlank(),
            autoSyncEnabled = prefs[Keys.AUTO_SYNC] ?: true,
            lastSyncedAt = prefs[Keys.LAST_SYNCED_AT] ?: 0L,
            instructorPhotoPath = prefs[Keys.INSTRUCTOR_PHOTO_PATH]?.takeIf {
                it.isNotBlank() && File(it).exists()
            },
            instructorPhotoVersion = prefs[Keys.INSTRUCTOR_PHOTO_VERSION] ?: 0L
        )
    }

    suspend fun saveInstructorPhotoFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val dest = File(context.filesDir, "salman_shahin_portrait.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            context.dataStore.edit { prefs ->
                prefs[Keys.INSTRUCTOR_PHOTO_PATH] = dest.absolutePath
                prefs[Keys.INSTRUCTOR_PHOTO_VERSION] = System.currentTimeMillis()
            }
            true
        }.getOrDefault(false)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setEditorFontSize(sizeSp: Int) {
        context.dataStore.edit { it[Keys.EDITOR_FONT_SIZE] = sizeSp.coerceIn(12, 22) }
    }

    suspend fun setEditorTheme(theme: EditorThemeMode) {
        context.dataStore.edit { it[Keys.EDITOR_THEME] = theme.name }
    }

    suspend fun setStudentName(name: String) {
        context.dataStore.edit { it[Keys.STUDENT_NAME] = name.trim().take(40) }
    }

    suspend fun setAutoSync(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SYNC] = enabled }
    }

    suspend fun updateLastSyncedAt(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNCED_AT] = timestamp }
    }

    suspend fun switchToStudent() {
        context.dataStore.edit { it[Keys.ACTIVE_ROLE] = UserRole.STUDENT.name }
    }

    suspend fun setupAndUnlockAdmin(pin: String): Boolean {
        val cleaned = pin.trim()
        if (cleaned.length < 4) return false
        val hash = hashPin(cleaned)
        context.dataStore.edit { prefs ->
            prefs[Keys.ADMIN_PIN_HASH] = hash
            prefs[Keys.ACTIVE_ROLE] = UserRole.ADMIN.name
        }
        return true
    }

    suspend fun verifyAndUnlockAdmin(pin: String): Boolean {
        val hash = hashPin(pin.trim())
        var matched = false
        context.dataStore.edit { prefs ->
            val stored = prefs[Keys.ADMIN_PIN_HASH]
            if (stored != null && stored == hash) {
                prefs[Keys.ACTIVE_ROLE] = UserRole.ADMIN.name
                matched = true
            }
        }
        return matched
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("hsc_ict_admin_v1:$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
