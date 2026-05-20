package com.lenicon.floriledia.views

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lenicon.floriledia.R
import com.lenicon.floriledia.adapters.ScannerImageAdapter // Ensure this is imported cleanly
import com.lenicon.floriledia.models.PlantPhoto
import com.lenicon.floriledia.services.PlantApiService
import com.lenicon.floriledia.utils.NavigationHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ScannerActivity : AppCompatActivity() {

    private val selectedPhotos = mutableListOf<PlantPhoto>()
    private val maxPhotos = 5
    private var isLoading = false
    private var tempCameraFile: File? = null

    private lateinit var tvPhotoCount: TextView
    private lateinit var gvPhotos: GridView
    private lateinit var btnIdentify: FrameLayout
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvButtonText: TextView
    private lateinit var adapter: ScannerImageAdapter

    // Photo extraction intent contract handles
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> processPickedImage(uri) }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempCameraFile?.let { file -> processPickedImage(Uri.fromFile(file)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        supportActionBar?.title = "Plant Identifier"

        initViews()
        setupGrid()
        updateUIState()

        // Fixed: Pass nav_scanner to highlight the scanner tab instead of the account tab
        NavigationHelper.initBottomNavigation(this, R.id.nav_scanner)
    }

    override fun onResume() {
        super.onResume()
        // Synchronize selected tab look when returning back to this active layout frame
        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNavigation != null) {
            bottomNavigation.selectedItemId = R.id.nav_scanner
        }
    }

    private fun initViews() {
        tvPhotoCount = findViewById(R.id.tvPhotoCount)
        gvPhotos = findViewById(R.id.gvPhotos)
        btnIdentify = findViewById(R.id.btnIdentify)
        pbLoading = findViewById(R.id.pbLoading)
        tvButtonText = findViewById(R.id.tvButtonText)
    }

    private fun setupGrid() {
        adapter = ScannerImageAdapter(selectedPhotos, maxPhotos,
            onAddClick = { showPickerMenu() },
            onDeleteClick = { index -> 
                selectedPhotos.removeAt(index)
                updateUIState()
            }
        )
        gvPhotos.adapter = adapter
    }

    private fun updateUIState() {
        tvPhotoCount.text = "Photos: ${selectedPhotos.size} / $maxPhotos (same plant's organs)"
        adapter.notifyDataSetChanged()

        val isButtonActive = selectedPhotos.isNotEmpty() && !isLoading
        btnIdentify.isEnabled = isButtonActive
        
        // Dynamically style matching your primary/disabled configuration specs
        val bgDrawable = GradientDrawable().apply {
            cornerRadius = dpToPx(12) // Fixed: Passed explicit value into the conversion method
            setColor(if (isButtonActive) Color.parseColor("#FF524444") else Color.parseColor("#FFCCCCCC"))
        }
        btnIdentify.background = bgDrawable

        if (isLoading) {
            pbLoading.visibility = View.VISIBLE
            tvButtonText.text = "Hmm... identifying..."
        } else {
            pbLoading.visibility = View.GONE
            tvButtonText.text = "Identify Plant"
        }

        btnIdentify.setOnClickListener {
            if (isButtonActive) handleIdentification()
        }
    }

    private fun showPickerMenu() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_picker_menu, null)
        
        view.findViewById<View>(R.id.lnGallery).setOnClickListener {
            galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.lnCamera).setOnClickListener {
            val file = File.createTempFile("scan_", ".jpg", cacheDir)
            tempCameraFile = file
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun processPickedImage(uri: Uri) {
        val localFile = File(filesDir, "raw_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(localFile).use { output -> input.copyTo(output) }
        }

        showOrganSelectionDialog { selectedOrgan ->
            selectedPhotos.add(PlantPhoto(localFile.absolutePath, selectedOrgan))
            updateUIState()
        }
    }

    private fun showOrganSelectionDialog(onSelected: (String) -> Unit) {
        val options = arrayOf("Leaf", "Flower", "Fruit", "Bark", "Auto")
        AlertDialog.Builder(this)
            .setTitle("Identify the organ")
            .setItems(options) { _, which ->
                val chosen = options[which]
                onSelected(if (chosen == "Auto") "Auto" else chosen)
            }
            .create().show()
    }

    private fun handleIdentification() {
        isLoading = true
        updateUIState()

        lifecycleScope.launch {
            try {
                val plantResult = PlantApiService.identifyPlant(selectedPhotos)
                
                // If your results display activity has a different class name, change ResultScreenActivity here
                val intent = Intent(this@ScannerActivity, ResultScreenActivity::class.java).apply {
                    putExtra("PLANT_DATA", plantResult)
                }
                startActivity(intent)
                selectedPhotos.clear()
            } catch (e: Exception) {
                showErrorDialog(e.message ?: "Something went wrong.")
            } finally {
                isLoading = false
                updateUIState()
            }
        }
    }

    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(msg)
            .setPositiveButton("Okayyy, if you say so...") { d, _ -> d.dismiss() }
            .show()
    }

    // Fixed: Accepted integer dp input argument value to safely run calculation
    private fun dpToPx(dp: Int): Float = dp * resources.displayMetrics.density
}