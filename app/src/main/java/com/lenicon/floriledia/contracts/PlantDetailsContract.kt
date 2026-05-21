package com.lenicon.floriledia.contracts

import com.lenicon.floriledia.models.PlantResult

interface PlantDetailsContract {
    interface View {
        fun populatePlantDetails(plant: PlantResult)
        fun updateActionBarTitle(nickname: String)
        fun updateSaveButtonState(isSaving: Boolean, text: String)
        fun showSuccessMessage(message: String)
        fun showErrorDialog(message: String)
        fun showToast(message: String)
        fun closeScreen()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun initializePlant(plant: PlantResult)
        fun onInputFieldsChanged(currentNickname: String, currentNotes: String)
        fun updatePlantDetails(newNickname: String, newNotes: String)
        fun deletePlant()
    }
}