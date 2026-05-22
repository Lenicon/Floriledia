package com.lenicon.floriledia.contracts

import com.lenicon.floriledia.models.PlantResult

interface PlantDetailsContract {
    enum class Mode { RESULT, DETAILS }

    interface View {
        fun showPlantDetails(result: PlantResult)
        fun setupUiForMode(mode: Mode)
        fun updatePrimaryButtonState(isEnabled: Boolean, text: String)
        fun showDeleteConfirmationDialog()
        fun showMessage(message: String)
        fun showError(error: String)
        fun navigateBack()
        fun openWikipediaLink(url: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun handlePrimaryAction(nickname: String, notes: String)
        fun handleSecondaryAction()
        fun handleDeleteRequested()
        fun confirmDeletion()
        fun onWikipediaClicked()
    }
}