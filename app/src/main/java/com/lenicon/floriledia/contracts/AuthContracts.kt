package com.lenicon.floriledia.contracts

interface LoginContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showMessage(message: String)
        fun navigateToMain()
        fun navigateToRegister()
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun checkUserSession()
    }
}

interface RegisterContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showMessage(message: String)
        fun navigateToLogin()
    }

    interface Presenter {
        fun register(username: String, email: String, password: String)
    }
}