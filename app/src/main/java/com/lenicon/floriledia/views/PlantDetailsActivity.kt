package com.lenicon.floriledia.views

import com.lenicon.floriledia.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.lenicon.floriledia.models.PlantResult
import java.io.File
import androidx.core.content.IntentCompat


class PlantDetailsActivity : AppCompatActivity() {

    private lateinit var plant: PlantResult 

    private lateinit var etNickname: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnBack: Button
    private lateinit var btnDelete: Button
    private lateinit var infoContainer: LinearLayout
    private lateinit var tvWikiSummary: TextView
    private lateinit var tvWikiLink: TextView
    private lateinit var ivWikiImage: ImageView
    private lateinit var ivPlantCollage: ImageView

    private var isSaving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_details)

        // Retrieve the serializable plant model safely from intent extras
        val parcelablePlant = IntentCompat.getParcelableExtra(intent, "PLANT_EXTRA", PlantResult::class.java)
        
        if (parcelablePlant == null) {
            Toast.makeText(this, "Failed to load plant data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        plant = parcelablePlant

        setupActionBarTitle()
        initViews()
        populatePlantDetails()
        setupListeners()
    }

    private fun setupActionBarTitle() {
        supportActionBar?.apply {
            title = if (plant.nickname.isEmpty()) {
                "Plant Details"
            } else {
                "${plant.nickname}'s Details"
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initViews() {
        etNickname = findViewById(R.id.etNickname)
        etNotes = findViewById(R.id.etNotes)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDeleteCollection)
        infoContainer = findViewById(R.id.infoContainer)
        tvWikiSummary = findViewById(R.id.tvWikiSummary)
        tvWikiLink = findViewById(R.id.tvWikiLink)
        ivWikiImage = findViewById(R.id.wikiImage)
        ivPlantCollage = findViewById(R.id.plantCollageImage)
    }

    private fun populatePlantDetails() {
        etNickname.setText(plant.nickname)
        etNotes.setText(plant.notes)

        // Populate your scientific metadata fields dynamically
        addInfoRow("Scientific Name", plant.scientificName)
        addInfoRow("Authorship", plant.authorship)
        addInfoRow("Family", plant.family)
        
        if (plant.commonNames.isNotEmpty()) {
            addInfoRow("Common Names", plant.commonNames.joinToString(", "))
        }

        // Handle the image gallery preview from local storage paths
        if (plant.imagePaths.isNotEmpty()) {
            val file = File(plant.imagePaths[0])
            if (file.exists()) {
                ivPlantCollage.setImageURI(Uri.fromFile(file))
            }
        }

        // Populate Wikipedia reference card if data exists
        if (plant.wikiSummary.isNotEmpty()) {
            tvWikiSummary.text = plant.wikiSummary
            tvWikiSummary.setTypeface(null, android.graphics.Typeface.NORMAL)
            tvWikiSummary.setTextColor(resources.getColor(android.R.color.black, theme))
            tvWikiLink.visibility = View.VISIBLE
            
            // Note: Use a network imaging library like Coil or Glide here to pull wikiImageURL
            if (plant.wikiImageURL.isNotEmpty()) {
                ivWikiImage.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnDelete.setOnClickListener {
            if (!isSaving) showDeleteConfirmation()
        }

        btnUpdate.setOnClickListener {
            handleUpdate()
        }

        // Monitors both input fields to toggle the "Update Details" button activity state
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkChanges()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etNickname.addTextChangedListener(textWatcher)
        etNotes.addTextChangedListener(textWatcher)
    }

    private fun checkChanges() {
        val hasChanges = etNickname.text.toString() != plant.nickname || 
                         etNotes.text.toString() != plant.notes
        btnUpdate.isEnabled = hasChanges && !isSaving
    }

    private fun handleUpdate() {
        isSaving = true
        checkChanges()
        btnUpdate.text = "Updating..."

        try {
            // Apply the mutated values directly to your model
            plant.nickname = etNickname.text.toString()
            plant.notes = etNotes.text.toString()

            // TODO: Call your app's background save/sync utility layer here
            // e.g., StorageRepository.updatePlant(plant)

            Snackbar.make(findViewById(R.id.scrollView), "Changes saved successfully", Snackbar.LENGTH_SHORT).show()
            setupActionBarTitle() 
        } catch (e: Exception) {
            showErrorDialog("Update failed: ${e.message}")
        } finally {
            isSaving = false
            btnUpdate.text = "Update Details"
            checkChanges()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Plant?")
            .setMessage("This will permanently remove this plant from your collection.")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                // TODO: Wire up your database/file deletion sequence here
                Toast.makeText(this, "Plant removed from collection", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Dismiss") { dialog: android.content.DialogInterface, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addInfoRow(title: String, value: String) {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, infoContainer, false)
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)
        
        text1.text = title
        text1.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
        text1.textSize = 12f
        
        text2.text = value
        text2.textSize = 16f
        
        infoContainer.addView(view)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}