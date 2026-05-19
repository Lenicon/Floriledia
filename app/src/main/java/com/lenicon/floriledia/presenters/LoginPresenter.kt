package com.lenicon.floriledia.presenters

import com.lenicon.floriledia.contracts.LoginContract
import com.lenicon.floriledia.models.UserPreferences

class LoginPresenter(
    private var view: LoginContract.View?,
    private val repository: UserPreferences
) : LoginContract.Presenter {

    override fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            view?.showMessage("Please fill in all fields")
            return
        }

        view?.showLoading()
        val (_, savedEmail, savedPassword) = repository.getSavedUser()

        view?.hideLoading()
        // Evaluate credentials against local storage profiles
        if (email == savedEmail && password == savedPassword) {
            view?.navigateToMain()
        } else {
            view?.showMessage("Invalid email or password configuration")
        }
    }

    override fun checkUserSession() {
        if (repository.isUserLoggedIn()) {
            view?.navigateToMain()
        }
    }

    fun detachView() { view = null }
}