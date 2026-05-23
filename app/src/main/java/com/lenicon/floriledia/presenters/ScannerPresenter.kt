package com.lenicon.floriledia.presenters

import android.net.Uri
import com.lenicon.floriledia.contracts.ScannerContract
import com.lenicon.floriledia.models.PlantPhoto
import com.lenicon.floriledia.services.PlantApiService
import com.lenicon.floriledia.models.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class ScannerPresenter(
    private val scope: CoroutineScope,
    private val filesDir: File,
    private val cacheDir: File,
    private val userPrefs: UserPreferences
) : ScannerContract.Presenter {

    private var view: ScannerContract.View? = null
    private val selectedPhotos = mutableListOf<PlantPhoto>()
    private val maxPhotos = 5
    private var isLoading = false

    override fun attachView(view: ScannerContract.View) {
        this.view = view
        view.updatePhotoList(selectedPhotos)
    }

    override fun detachView() {
        this.view = null
    }

    override fun handleAddPhotoClick() {
        if (selectedPhotos.size < maxPhotos) {
            view?.showImageSourceDialog()
        } else {
            view?.showError("Max limit of $maxPhotos photos reached.")
        }
    }

    override fun handleCameraOptionSelected() {
        try {
            val file = File.createTempFile("scan_", ".jpg", filesDir)
            view?.triggerCameraIntent(file)
        } catch (e: Exception) {
            view?.showError("Failed to prepare camera file system.")
        }
    }

    override fun handleGalleryOptionSelected() {
        view?.triggerGalleryIntent()
    }

    override fun handlePhotoCaptured(file: File, organ: String) {
        val localFile = File(filesDir, "raw_${System.currentTimeMillis()}.jpg")
        view?.copyUriToFile(Uri.fromFile(file), localFile)
        
        selectedPhotos.add(PlantPhoto(localFile.absolutePath, organ))
        view?.updatePhotoList(selectedPhotos)
    }

    override fun handleGalleryPhotoSelected(uri: Uri, organ: String) {
        val localFile = File(filesDir, "gallery_${System.currentTimeMillis()}.jpg")
        view?.copyUriToFile(uri, localFile)
        
        selectedPhotos.add(PlantPhoto(localFile.absolutePath, organ))
        view?.updatePhotoList(selectedPhotos)
    }

    override fun handleRemovePhoto(index: Int) {
        if (index in selectedPhotos.indices) {
            selectedPhotos.removeAt(index)
            view?.updatePhotoList(selectedPhotos)
        }
    }

    override fun handleIdentification() {
        if (selectedPhotos.isEmpty() || isLoading) return

        isLoading = true
        view?.showLoading(true, "Hmm... identifying...")

        scope.launch {
            try {
                val result = PlantApiService.identifyPlant(selectedPhotos)
                
                userPrefs.incrementScans()

                if (result.scientificName.isNotBlank()) {
                    val wikiData = com.lenicon.floriledia.services.WikipediaService.fetchWiki(result.scientificName)
                    
                    result.wikiSummary = wikiData.wikiSummary
                    result.wikiImageURL = wikiData.wikiImageURL
                }

                view?.openResultScreen(result)
                selectedPhotos.clear()
                view?.updatePhotoList(selectedPhotos)
            } catch (e: Exception) {
                view?.showError(e.message ?: "Something went wrong.")
            } finally {
                isLoading = false
                view?.showLoading(false)
            }
        }
    }
}