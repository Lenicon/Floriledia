package com.lenicon.floriledia.presenters

import androidx.lifecycle.LifecycleCoroutineScope
import com.lenicon.floriledia.contracts.PlantDetailsContract
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantDetailsPresenter(
    private var plantResult: PlantResult,
    private val mode: PlantDetailsContract.Mode,
    private val lifecycleScope: LifecycleCoroutineScope
) : PlantDetailsContract.Presenter {

    private var view: PlantDetailsContract.View? = null
    private var isProcessing = false

    override fun attachView(view: PlantDetailsContract.View) {
        this.view = view
        this.view?.setupUiForMode(mode)
        this.view?.showPlantDetails(plantResult)
    }

    override fun detachView() {
        this.view = null
    }

    override fun handlePrimaryAction(nickname: String, notes: String) {
        if (isProcessing) return
        isProcessing = true

        val pendingText = if (mode == PlantDetailsContract.Mode.RESULT) "Saving..." else "Updating..."
        view?.updatePrimaryButtonState(false, pendingText)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                plantResult.nickname = nickname.ifBlank { "Unnamed Plant" }
                plantResult.notes = notes

                if (mode == PlantDetailsContract.Mode.RESULT) {
                    val permanentPaths = mutableListOf<String>()
                    for (tempPath in plantResult.imagePaths) {
                        if (tempPath.isNotBlank() && !StorageService.isSaved(tempPath)) {
                            val targetPath = StorageService.saveImagePermanently(tempPath)
                            StorageService.markAsSaved(targetPath)
                            permanentPaths.add(targetPath)
                        } else {
                            permanentPaths.add(tempPath)
                        }
                    }
                    plantResult.imagePaths = permanentPaths
                    StorageService.savePlant(plantResult)
                } else {
                    StorageService.updatePlant(plantResult)
                }

                withContext(Dispatchers.Main) {
                    val feedback = if (mode == PlantDetailsContract.Mode.RESULT) "Saved successfully" else "Updated successfully"
                    view?.showMessage(feedback)
                    view?.navigateBack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Execution failed: ${e.localizedMessage}")
                    isProcessing = false
                    val defaultText = if (mode == PlantDetailsContract.Mode.RESULT) "Save Plant" else "Update Details"
                    view?.updatePrimaryButtonState(true, defaultText)
                }
            }
        }
    }

    override fun handleSecondaryAction() {
        view?.navigateBack()
    }

    override fun handleDeleteRequested() {
        view?.showDeleteConfirmationDialog()
    }

    override fun confirmDeletion() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                StorageService.deletePlant(plantResult.id)
                withContext(Dispatchers.Main) {
                    view?.showMessage("Specimen removed from database.")
                    view?.navigateBack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showError("Unable to delete entry: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun onWikipediaClicked() {
        if (plantResult.scientificName.isNotBlank()) {
            val query = plantResult.scientificName.replace(" ", "_")
            view?.openWikipediaLink("https://en.wikipedia.org/wiki/$query")
        } else {
            view?.showMessage("Wikipedia link reference broken or missing.")
        }
    }
}