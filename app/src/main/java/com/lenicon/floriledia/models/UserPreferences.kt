package com.lenicon.floriledia.models

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "floriledia_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER_EMAIL = "current_logged_in_email"
        
        private const val SUFFIX_USERNAME = "_username"
        private const val SUFFIX_PASSWORD = "_password"
        private const val SUFFIX_TOTAL_SCANS = "_total_scans"
        private const val SUFFIX_SAVED_PLANTS = "_saved_plants"
    }

    fun registerUser(username: String, email: String, password: String): Boolean {
        if (emailExists(email)) return false
        
        prefs.edit().apply {
            putString("user_${email}${SUFFIX_USERNAME}", username)
            putString("user_${email}${SUFFIX_PASSWORD}", password)
            putInt("user_${email}${SUFFIX_TOTAL_SCANS}", 0)
            putInt("user_${email}${SUFFIX_SAVED_PLANTS}", 0)
            apply()
        }
        return true
    }

    fun loginUser(email: String, password: String): Boolean {
        val savedPassword = prefs.getString("user_${email}${SUFFIX_PASSWORD}", null)
        if (savedPassword != null && savedPassword == password) {
            prefs.edit().apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_CURRENT_USER_EMAIL, email)
                apply()
            }
            return true
        }
        return false
    }

    fun emailExists(email: String): Boolean {
        return prefs.contains("user_${email}${SUFFIX_PASSWORD}")
    }

    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getSavedUser(): Triple<String?, String?, String?> {
        val currentEmail = prefs.getString(KEY_CURRENT_USER_EMAIL, null) ?: return Triple(null, null, null)
        val username = prefs.getString("user_${currentEmail}${SUFFIX_USERNAME}", null)
        val password = prefs.getString("user_${currentEmail}${SUFFIX_PASSWORD}", null)
        
        return Triple(username, currentEmail, password)
    }

    fun getMetrics(): Pair<Int, Int> {
        val currentEmail = prefs.getString(KEY_CURRENT_USER_EMAIL, null) ?: return Pair(0, 0)
        return Pair(
            prefs.getInt("user_${currentEmail}${SUFFIX_TOTAL_SCANS}", 0),
            prefs.getInt("user_${currentEmail}${SUFFIX_SAVED_PLANTS}", 0)
        )
    }

    fun incrementScans() {
        val currentEmail = prefs.getString(KEY_CURRENT_USER_EMAIL, null) ?: return
        val currentScans = prefs.getInt("user_${currentEmail}${SUFFIX_TOTAL_SCANS}", 0)
        prefs.edit().putInt("user_${currentEmail}${SUFFIX_TOTAL_SCANS}", currentScans + 1).apply()
    }

    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_CURRENT_USER_EMAIL, null)
            apply()
        }
    }
}