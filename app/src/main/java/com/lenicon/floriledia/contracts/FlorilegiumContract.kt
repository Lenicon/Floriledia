package com.lenicon.floriledia.contracts

import com.lenicon.floriledia.models.PlantResult

interface FlorilegiumContract {
    interface View {
        fun showPlants(plants: List<PlantResult>)
        fun updateHeaderTitle(count: Int)
        fun toggleEmptyState(isVisible: Boolean)
        fun toggleClearSearchButton(isVisible: Boolean)
        fun navigateToDetails(plant: PlantResult)
        fun clearSearchInput()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun startObservingCollection()
        fun onSearchQueryChanged(query: String)
        fun onClearSearchClicked()
        fun onPlantClicked(plant: PlantResult)
    }
}