package com.lenicon.floriledia.presenters

import com.lenicon.floriledia.contracts.AccountContract
import com.lenicon.floriledia.models.UserPreferences

class AccountPresenter(
    private var view: AccountContract.View?,
    private val repository: UserPreferences
) : AccountContract.Presenter {

    override fun loadUserData() {
        val (username, email, _) = repository.getSavedUser()
        val (scanCount, savedCount) = repository.getMetrics()

        view?.displayUserData(
            username ?: "Unknown Botanist",
            email ?: "No email associated",
            scanCount,
            savedCount
        )
    }

    override fun logout() {
        repository.clearSession()
        view?.showMessage("Logged out successfully")
        view?.navigateToLogin()
    }

    override fun detachView() {
        view = null
    }
}