package com.lenicon.floriledia.presenters

import com.lenicon.floriledia.contracts.RegisterContract
import com.lenicon.floriledia.models.UserPreferences

class RegisterPresenter(
    private var view: RegisterContract.View?,
    private val repository: UserPreferences
) : RegisterContract.Presenter {

    override fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            view?.showMessage("Please fill in all fields")
            return
        }

        view?.showLoading()
        val registrationSuccessful = repository.registerUser(username, email, password)

        if (registrationSuccessful) {
            view?.showMessage("Registration successful! Please login.")
            view?.navigateToLogin()
        } else {
            view?.showMessage("This email address is already registered.")
        }
        view?.hideLoading()
    }

    fun detachView() { view = null }
}