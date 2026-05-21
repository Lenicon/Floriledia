package com.lenicon.floriledia.presenters

import androidx.lifecycle.LifecycleCoroutineScope
import com.lenicon.floriledia.contracts.FlorilegiumContract
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FlorilegiumPresenter(
    private val lifecycleScope: LifecycleCoroutineScope
) : FlorilegiumContract.Presenter {

    private var view: FlorilegiumContract.View? = null
    private var allCachedPlants: List<PlantResult> = emptyList()

    override fun attachView(view: FlorilegiumContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun startObservingCollection() {
        lifecycleScope.launch {
            // Trigger an initial storage load sequence across database shards
            StorageService.load()

            // Reactively collect from the shared background state flow stream
            StorageService.plantsStateFlow.collectLatest { jsonObjects ->
                val structuralPlants = mapJsonToPlantResults(jsonObjects)
                allCachedPlants = structuralPlants

                // Jump back onto main UI execution line to apply layout changes
                withContext(Dispatchers.Main) {
                    view?.showPlants(structuralPlants)
                    view?.updateHeaderTitle(structuralPlants.size)
                    view?.toggleEmptyState(structuralPlants.isEmpty())
                }
            }
        }
    }

    override fun onSearchQueryChanged(query: String) {
        view?.toggleClearSearchButton(query.isNotEmpty())
        
        if (query.isBlank()) {
            view?.showPlants(allCachedPlants)
            view?.toggleEmptyState(allCachedPlants.isEmpty())
        } else {
            val filtered = allCachedPlants.filter { plant ->
                plant.nickname.contains(query, ignoreCase = true) ||
                plant.scientificName.contains(query, ignoreCase = true) ||
                plant.commonNames.any { it.contains(query, ignoreCase = true) }
            }
            view?.showPlants(filtered)
            view?.toggleEmptyState(filtered.isEmpty())
        }
    }

    override fun onClearSearchClicked() {
        view?.clearSearchInput()
    }

    override fun onPlantClicked(plant: PlantResult) {
        view?.navigateToDetails(plant)
    }

    /**
     * Translates low-level JSONObject array streams safely back into explicit domain models.
     */
    private suspend fun mapJsonToPlantResults(jsonList: List<JSONObject>): List<PlantResult> = 
        withContext(Dispatchers.Default) {
            jsonList.map { obj ->
                val commonNamesList = mutableListOf<String>()
                val commonNamesArray = obj.optJSONArray("commonNames")
                if (commonNamesArray != null) {
                    for (i in 0 until commonNamesArray.length()) {
                        commonNamesList.add(commonNamesArray.optString(i))
                    }
                }

                val imagePathsList = mutableListOf<String>()
                val imagePathsArray = obj.optJSONArray("imagePaths")
                if (imagePathsArray != null) {
                    for (i in 0 until imagePathsArray.length()) {
                        imagePathsList.add(imagePathsArray.optString(i))
                    }
                }

                PlantResult(
                    id = obj.optString("id", ""),
                    scientificName = obj.optString("scientificName", ""),
                    authorship = obj.optString("authorship", ""),
                    family = obj.optString("family", ""),
                    commonNames = commonNamesList,
                    wikiSummary = obj.optString("wikiSummary", ""),
                    wikiImageURL = obj.optString("wikiImageURL", ""),
                    imagePaths = imagePathsList,
                    nickname = obj.optString("nickname", ""),
                    notes = obj.optString("notes", "")
                )
            }
        }
}