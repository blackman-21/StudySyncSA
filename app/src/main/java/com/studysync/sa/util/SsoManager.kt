package com.studysync.sa.util

import android.content.Context
import android.util.Log

data class SsoUser(
    val id: String,
    val name: String,
    val email: String,
    val provider: String,
    val photoUrl: String? = null
)

/**
 * Single Sign-On (SSO) Manager handling authentication via Google / Institutional SSO providers.
 */
class SsoManager(private val context: Context) {
    private val TAG = "SsoManager"

    interface SsoCallback {
        fun onSuccess(user: SsoUser)
        fun onError(error: String)
    }

    /**
     * Initiates Google Single Sign-On flow.
     */
    fun signInWithGoogle(callback: SsoCallback) {
        try {
            Log.d(TAG, "Initiating Google Single Sign-On...")
            // Simulated institutional/Google SSO sign-in payload for demonstration & offline capability
            val ssoUser = SsoUser(
                id = "google_sso_${System.currentTimeMillis()}",
                name = "Google Student",
                email = "student.sso@studysync.ac.za",
                provider = "Google SSO",
                photoUrl = null
            )
            callback.onSuccess(ssoUser)
        } catch (e: Exception) {
            Log.e(TAG, "SSO Sign-in failed", e)
            callback.onError(e.localizedMessage ?: "Single Sign-On failed. Please try again.")
        }
    }
}
