package com.lenicon.floriledia.presenters

import com.lenicon.floriledia.contracts.AccountContract
import com.lenicon.floriledia.models.UserPreferences
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountPresenter(
    private var view: AccountContract.View?,
    private val repository: UserPreferences
) : AccountContract.Presenter {

    private val presenterJob = Job()
    private val presenterScope = CoroutineScope(Dispatchers.Main + presenterJob)

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
        presenterScope.launch {
            // 1. Wipe the global file sandbox pointers and memory caches back to guest defaults
            withContext(Dispatchers.IO) {
                StorageService.logout()
            }
            
            // 2. Clear the shared preferences user session authentication tokens
            repository.clearSession()
            
            // 3. Inform the view to clean up layout interfaces and route back to login interface
            view?.showMessage("Logged out successfully")
            view?.navigateToLogin()
        }
    }

    override fun detachView() {
        view = null
        presenterJob.cancel() // Cancel ongoing coroutines to prevent memory leaks or crashes on close
    }
}