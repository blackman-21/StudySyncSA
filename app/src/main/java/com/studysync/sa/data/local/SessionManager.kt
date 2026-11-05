package com.studysync.sa.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages user session and preferences securely, handling EncryptedSharedPreferences safely.
 */
class SessionManager(context: Context) {
    private val tag = "SessionManager"

    // Standard SharedPreferences for app UI settings (language, theme) to allow safe attachBaseContext access
    private val settingsPrefs: SharedPreferences = context.getSharedPreferences("study_sync_settings", Context.MODE_PRIVATE)

    // Secure EncryptedSharedPreferences for sensitive auth tokens
    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "study_sync_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(tag, "EncryptedSharedPreferences creation failed. Falling back to standard preferences.", e)
            context.getSharedPreferences("study_sync_secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    fun saveAuthToken(token: String) {
        securePrefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return securePrefs.getString("auth_token", null)
    }

    fun setLanguage(language: String) {
        settingsPrefs.edit().putString("app_language", language).apply()
    }

    fun getLanguage(): String {
        return settingsPrefs.getString("app_language", "English") ?: "English"
    }

    fun clearSession() {
        securePrefs.edit().clear().apply()
        settingsPrefs.edit().clear().apply()
    }
}
