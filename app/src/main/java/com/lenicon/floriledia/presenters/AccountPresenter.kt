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
        presenterScope.launch {
            val (username, email, _) = repository.getSavedUser()
            val (scanCount, _) = repository.getMetrics()

            val savedPlantsCount = withContext(Dispatchers.IO) {
                StorageService.getAllSavedPlants().size
            }

            view?.displayUserData(
                username ?: "Unknown Botanist",
                email ?: "No email associated",
                scanCount,
                savedPlantsCount
            )
        }
    }

    override fun incrementScanCount() {
        repository.incrementScans()
    }

    override fun logout() {
        presenterScope.launch {
            withContext(Dispatchers.IO) {
                StorageService.logout()
            }
            repository.clearSession()
            view?.showMessage("Logged out successfully")
            view?.navigateToLogin()
        }
    }

    override fun detachView() {
        view = null
        presenterJob.cancel()
    }
}