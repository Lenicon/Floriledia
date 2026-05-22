package com.lenicon.floriledia.views.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.lenicon.floriledia.R
import com.lenicon.floriledia.adapters.ScannerImageAdapter
import com.lenicon.floriledia.contracts.ScannerContract
import com.lenicon.floriledia.models.PlantPhoto
import com.lenicon.floriledia.models.UserPreferences
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.presenters.ScannerPresenter
import com.lenicon.floriledia.utils.NavigationHelper
import java.io.File
import java.io.FileOutputStream
import com.lenicon.floriledia.contracts.PlantDetailsContract


class ScannerActivity : AppCompatActivity(), ScannerContract.View {

    private lateinit var presenter: ScannerContract.Presenter
    private lateinit var adapter: ScannerImageAdapter
    private var pendingCameraFile: File? = null

    private lateinit var tvPhotoCount: TextView
    private lateinit var gvPhotos: GridView
    private lateinit var btnIdentify: FrameLayout
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvButtonText: TextView

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingCameraFile?.let { file ->
                showOrganSelectionDialog { organ -> presenter.handlePhotoCaptured(file, organ) }
            }
        }
    }

    // Gallery picker launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            showOrganSelectionDialog { organ -> presenter.handleGalleryPhotoSelected(selectedUri, organ) }
        }
    }

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            presenter.handleAddPhotoClick()
        } else {
            showError("Camera permission is required to take photos.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // 1. Initialize views first so they are ready for the presenter
        initViews()

        // 2. Attach presenter safely
        presenter = ScannerPresenter(lifecycleScope, filesDir, cacheDir, UserPreferences(applicationContext))
        presenter.attachView(this)

        NavigationHelper.initBottomNavigation(this, R.id.nav_scanner)
    }

    private fun initViews() {
        tvPhotoCount = findViewById(R.id.tvPhotoCount)
        gvPhotos = findViewById(R.id.gvPhotos)
        btnIdentify = findViewById(R.id.btnIdentify)
        pbLoading = findViewById(R.id.pbLoading)
        tvButtonText = findViewById(R.id.tvButtonText)

        btnIdentify.setOnClickListener { presenter.handleIdentification() }
    }

    override fun updatePhotoList(photos: List<PlantPhoto>) {
        tvPhotoCount.text = "Photos: ${photos.size} / 5"
        
        adapter = ScannerImageAdapter(photos.toMutableList(), 5,
            onAddClick = { checkPermissionAndAdd() }, // Triggered when clicking the '+' item
            onDeleteClick = { index -> presenter.handleRemovePhoto(index) }
        )
        gvPhotos.adapter = adapter
        
        // btnIdentify remains completely disabled until photos are uploaded
        val hasPhotos = photos.isNotEmpty()
        btnIdentify.isEnabled = hasPhotos
        btnIdentify.setBackgroundColor(if (hasPhotos) Color.parseColor("#FF524444") else Color.parseColor("#FFCCCCCC"))
    }

    private fun checkPermissionAndAdd() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            presenter.handleAddPhotoClick()
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    override fun showImageSourceDialog() {
        val choices = arrayOf("Take Photo (Camera)", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Plant Photo")
            .setItems(choices) { _, which ->
                when (which) {
                    0 -> presenter.handleCameraOptionSelected()
                    1 -> presenter.handleGalleryOptionSelected()
                }
            }
            .show()
    }

    override fun triggerCameraIntent(file: File) {
        pendingCameraFile = file
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
        cameraLauncher.launch(intent)
    }

    override fun triggerGalleryIntent() {
        galleryLauncher.launch("image/*")
    }

    override fun copyUriToFile(uri: Uri, targetFile: File) {
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output -> input.copyTo(output) }
        }
    }

    override fun showLoading(isLoading: Boolean, progressMessage: String) {
        pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        tvButtonText.text = if (isLoading) progressMessage else "Identify Plant"
        btnIdentify.isEnabled = !isLoading
    }

    override fun openResultScreen(plantResult: PlantResult) {
        val intent = Intent(this, PlantDetailsActivity::class.java).apply {
            putExtra("extra_plant_result", plantResult)
            putExtra("extra_view_mode", PlantDetailsContract.Mode.RESULT.name)
        }
        startActivity(intent)
    }

    override fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Notice")
            .setMessage(message)
            .setPositiveButton("Ok") { d, _ -> d.dismiss() }
            .show()
    }

    private fun showOrganSelectionDialog(onSelected: (String) -> Unit) {
        val options = arrayOf("Leaf", "Flower", "Fruit", "Bark", "Auto")
        AlertDialog.Builder(this)
            .setTitle("Identify the organ")
            .setItems(options) { _, which -> onSelected(options[which]) }
            .create().show()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}