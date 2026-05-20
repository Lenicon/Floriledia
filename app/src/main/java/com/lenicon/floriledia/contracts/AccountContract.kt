package com.lenicon.floriledia.contracts

import com.lenicon.floriledia.models.UserPreferences

interface AccountContract {
    interface View {
        fun displayUserData(username: String, email: String, scanCount: Int, savedCount: Int)
        fun navigateToLogin()
        fun showMessage(message: String)
    }

    interface Presenter {
        fun loadUserData()
        fun logout()
        fun detachView()
    }
}