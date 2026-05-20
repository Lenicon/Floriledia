package com.lenicon.floriledia.models

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "floriledia_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "saved_username"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
        
        // New metric keys
        private const val KEY_TOTAL_SCANS = "total_scans"
        private const val KEY_SAVED_PLANTS = "saved_plants"
    }

    fun saveUser(username: String, email: String, password: String) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            // Initialize metrics to 0 on new registration if not set
            if (!prefs.contains(KEY_TOTAL_SCANS)) putInt(KEY_TOTAL_SCANS, 0)
            if (!prefs.contains(KEY_SAVED_PLANTS)) putInt(KEY_SAVED_PLANTS, 0)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getSavedUser(): Triple<String?, String?, String?> {
        return Triple(
            prefs.getString(KEY_USERNAME, null),
            prefs.getString(KEY_EMAIL, null),
            prefs.getString(KEY_PASSWORD, null)
        )
    }

    // New helper to fetch metrics
    fun getMetrics(): Pair<Int, Int> {
        return Pair(
            prefs.getInt(KEY_TOTAL_SCANS, 0),
            prefs.getInt(KEY_SAVED_PLANTS, 0)
        )
    }

    // New session handling function for logout
    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}