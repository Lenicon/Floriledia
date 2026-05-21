package com.lenicon.floriledia.contracts

import android.net.Uri
import com.lenicon.floriledia.models.PlantPhoto
import com.lenicon.floriledia.models.PlantResult
import java.io.File

interface ScannerContract {

    interface View {
        fun updatePhotoList(photos: List<PlantPhoto>)
        fun showLoading(isLoading: Boolean, progressMessage: String = "")
        fun openResultScreen(plantResult: PlantResult)
        fun showError(message: String)
        fun showImageSourceDialog()
        fun triggerCameraIntent(file: File)
        fun triggerGalleryIntent()
        fun copyUriToFile(uri: Uri, targetFile: File)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun handleAddPhotoClick()
        fun handleCameraOptionSelected()
        fun handleGalleryOptionSelected()
        fun handlePhotoCaptured(file: File, organ: String)
        fun handleGalleryPhotoSelected(uri: Uri, organ: String)
        fun handleRemovePhoto(index: Int)
        fun handleIdentification()
    }
}