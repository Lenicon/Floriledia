package com.lenicon.floriledia.presenters

import androidx.lifecycle.LifecycleCoroutineScope
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.lenicon.floriledia.contracts.ResultContract

class ResultPresenter(
    private var plantResult: PlantResult,
    private val lifecycleScope: LifecycleCoroutineScope
) : ResultContract.Presenter {

    private var view: ResultContract.View? = null
    private var isSaving = false

    override fun attachView(view: ResultContract.View) {
        this.view = view
        // Immediately project the loaded analytical model states onto the view interface
        this.view?.showPlantDetails(plantResult)
    }

    override fun detachView() {
        this.view = null
    }

    override fun savePlant(nickname: String, notes: String) {
        if (isSaving) return
        isSaving = true
        view?.updateSaveButtonState(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Apply optional user mutations
                plantResult.nickname = nickname.ifBlank { "Unnamed Plant" }
                plantResult.notes = notes

                // Process internal picture location persistence changes cleanly 
                val finalizedPaths = mutableListOf<String>()
                for (tempPath in plantResult.imagePaths) {
                    if (tempPath.isNotBlank() && !StorageService.isSaved(tempPath)) {
                        val permanentPath = StorageService.saveImagePermanently(tempPath)
                        StorageService.markAsSaved(permanentPath)
                        finalizedPaths.add(permanentPath)
                    } else {
                        finalizedPaths.add(tempPath)
                    }
                }
                
                // Assign new storage paths back to data payload model object
                plantResult.imagePaths = finalizedPaths

                // Append the entry structured inside the active sandbox database shard file
                StorageService.savePlant(plantResult)

                withContext(Dispatchers.Main) {
                    view?.showMessage("Plant saved to collection successfully!")
                    view?.navigateBack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Failed to save entry: ${e.localizedMessage}")
                    isSaving = false
                    view?.updateSaveButtonState(false)
                }
            }
        }
    }

    override fun discard() {
        // Simple navigation cancellation
        view?.navigateBack()
    }

    override fun onWikipediaClicked() {
        if (plantResult.scientificName.isNotBlank()) {
            // Generate standard clean generic mobile lookup redirect links cleanly
            val formattedQuery = plantResult.scientificName.replace(" ", "_")
            val targetUrl = "https://en.wikipedia.org/wiki/$formattedQuery"
            view?.openWikipediaLink(targetUrl)
        } else {
            view?.showMessage("Wikipedia search link unavailable for this specimen.")
        }
    }
}