package com.lenicon.floriledia.presenters

import androidx.lifecycle.LifecycleCoroutineScope
import com.lenicon.floriledia.contracts.PlantDetailsContract
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantDetailsPresenter(
    private val lifecycleScope: LifecycleCoroutineScope
) : PlantDetailsContract.Presenter {

    private var view: PlantDetailsContract.View? = null
    private lateinit var plant: PlantResult
    private var isProcessing = false

    override fun attachView(view: PlantDetailsContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun initializePlant(plant: PlantResult) {
        this.plant = plant
        view?.populatePlantDetails(plant)
        view?.updateActionBarTitle(plant.nickname)
    }

    override fun onInputFieldsChanged(currentNickname: String, currentNotes: String) {
        val hasChanges = currentNickname != plant.nickname || currentNotes != plant.notes
        val buttonText = if (isProcessing) "Updating..." else "Update Details"
        view?.updateSaveButtonState(hasChanges && !isProcessing, buttonText)
    }

    override fun updatePlantDetails(newNickname: String, newNotes: String) {
        if (isProcessing) return
        isProcessing = true
        view?.updateSaveButtonState(false, "Updating...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Mutate local memory reference variables
                plant.nickname = newNickname
                plant.notes = newNotes

                // Commit mutated values across active storage files
                StorageService.updatePlant(plant)

                withContext(Dispatchers.Main) {
                    view?.showSuccessMessage("Changes saved successfully")
                    view?.updateActionBarTitle(plant.nickname)
                    isProcessing = false
                    onInputFieldsChanged(newNickname, newNotes)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showErrorDialog("Update failed: ${e.localizedMessage}")
                    isProcessing = false
                    onInputFieldsChanged(newNickname, newNotes)
                }
            }
        }
    }

    override fun deletePlant() {
        if (isProcessing) return
        isProcessing = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                StorageService.deletePlant(plant.id)
                withContext(Dispatchers.Main) {
                    view?.showToast("Plant removed from collection")
                    view?.closeScreen()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showErrorDialog("Deletion failed: ${e.localizedMessage}")
                    isProcessing = false
                }
            }
        }
    }
}