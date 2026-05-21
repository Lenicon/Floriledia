package com.lenicon.floriledia.contracts

import com.lenicon.floriledia.models.PlantResult

interface ResultContract {
    interface View {
        fun showPlantDetails(result: PlantResult)
        fun updateSaveButtonState(isSaving: Boolean)
        fun showMessage(message: String)
        fun showError(error: String)
        fun navigateBack()
        fun openWikipediaLink(url: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun savePlant(nickname: String, notes: String)
        fun discard()
        fun onWikipediaClicked()
    }
}