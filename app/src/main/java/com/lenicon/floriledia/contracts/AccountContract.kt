package com.lenicon.floriledia.contracts

interface AccountContract {
    interface View {
        fun displayUserData(username: String, email: String, scanCount: Int, savedCount: Int)
        fun navigateToLogin()
        fun showMessage(message: String)
    }

    interface Presenter {
        fun loadUserData()
        fun incrementScanCount()
        fun logout()
        fun detachView()
    }
}