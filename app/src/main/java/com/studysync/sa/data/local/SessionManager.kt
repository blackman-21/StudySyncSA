package com.studysync.sa.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages user session and preferences securely using EncryptedSharedPreferences.
 */
class SessionManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "study_sync_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun setLanguage(language: String) {
        sharedPreferences.edit().putString("app_language", language).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("app_language", "en") ?: "en"
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
