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

    // Manage background operations safely within presenter lifecycle constraints
    private val presenterJob = Job()
    private val presenterScope = CoroutineScope(Dispatchers.Main + presenterJob)

    override fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            view?.showMessage("Please fill in all fields")
            return
        }

        view?.showLoading()
        
        // Launch a coroutine to handle authentication and file state switching
        presenterScope.launch {
            // 1. Run the shared preferences validation on a background thread
            val isSuccess = withContext(Dispatchers.IO) {
                repository.loginUser(email, password)
            }

            view?.hideLoading()
            
            if (isSuccess) {
                // 2. Switch the active file storage sandbox directory safely
                StorageService.switchAccount(email)
                
                // 3. Move forward to your dashboard layout interfaces
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
                    // CRITICAL FIX: Ensure files map to the session profile before steering navigation
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
        // Cancel all ongoing coroutine processes to prevent background memory leaks
        presenterJob.cancel()
    }
}