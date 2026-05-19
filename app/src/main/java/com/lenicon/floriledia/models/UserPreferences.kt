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
        private const val KEY_PASSWORD = "saved_password" // Added password tracker
    }

    fun saveUser(username: String, email: String, password: String) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // Returns Triple containing Username, Email, Password
    fun getSavedUser(): Triple<String?, String?, String?> {
        return Triple(
            prefs.getString(KEY_USERNAME, null),
            prefs.getString(KEY_EMAIL, null),
            prefs.getString(KEY_PASSWORD, null)
        )
    }
}