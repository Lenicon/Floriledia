package com.lenicon.floriledia.presenters

import com.lenicon.floriledia.contracts.LoginContract
import com.lenicon.floriledia.models.UserPreferences
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private var view: LoginContract.View?,
    private val repository: UserPreferences
) : LoginContract.Presenter {

    private val presenterJob = Job()
    private val presenterScope = CoroutineScope(Dispatchers.Main + presenterJob)

    override fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            view?.showMessage("Please fill in all fields")
            return
        }

        view?.showLoading()
        
        presenterScope.launch {
            val isSuccess = withContext(Dispatchers.IO) {
                repository.loginUser(email, password)
            }

            view?.hideLoading()
            
            if (isSuccess) {
                StorageService.switchAccount(email)
                view?.navigateToMain()
            } else {
                view?.showMessage("Invalid email or password configuration")
            }
        }
    }

    override fun checkUserSession() {
        if (repository.isUserLoggedIn()) {
            val (_, email, _) = repository.getSavedUser()
            
            if (email != null) {
                presenterScope.launch {
                    StorageService.switchAccount(email)
                    view?.navigateToMain()
                }
            } else {
                view?.navigateToMain()
            }
        }
    }

    fun detachView() { 
        view = null 
        presenterJob.cancel()
    }
}