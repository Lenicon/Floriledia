package com.lenicon.floriledia.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.lenicon.floriledia.R
import com.lenicon.floriledia.contracts.PlantDetailsContract
import com.lenicon.floriledia.databinding.ActivityPlantDetailsBinding
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.presenters.PlantDetailsPresenter
import java.io.File

class PlantDetailsActivity : AppCompatActivity(), PlantDetailsContract.View {

    private lateinit var binding: ActivityPlantDetailsBinding
    private lateinit var presenter: PlantDetailsContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = PlantDetailsPresenter(lifecycleScope)
        presenter.attachView(this)

        val parcelablePlant = IntentCompat.getParcelableExtra(intent, "PLANT_EXTRA", PlantResult::class.java)
        if (parcelablePlant == null) {
            showToast("Failed to load plant data")
            finish()
            return
        }

        setupActionBar()
        setupListeners()
        
        // Let the presenter take over data orchestration
        presenter.initializePlant(parcelablePlant)
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { closeScreen() }

        binding.btnDeleteCollection.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnUpdate.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            val notes = binding.etNotes.text.toString()
            presenter.updatePlantDetails(nickname, notes)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onInputFieldsChanged(
                    binding.etNickname.text.toString(),
                    binding.etNotes.text.toString()
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etNickname.addTextChangedListener(textWatcher)
        binding.etNotes.addTextChangedListener(textWatcher)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Plant?")
            .setMessage("This will permanently remove this plant from your collection.")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                presenter.deletePlant()
            }
            .show()
    }

    // --- MVP View Architecture Realization Hooks ---

    override fun populatePlantDetails(plant: PlantResult) {
        binding.etNickname.setText(plant.nickname)
        binding.etNotes.setText(plant.notes)

        addInfoRow("Scientific Name", plant.scientificName)
        addInfoRow("Authorship", plant.authorship)
        addInfoRow("Family", plant.family)
        
        if (plant.commonNames.isNotEmpty()) {
            addInfoRow("Common Names", plant.commonNames.joinToString(", "))
        }

        if (plant.imagePaths.isNotEmpty()) {
            val file = File(plant.imagePaths[0])
            if (file.exists()) {
                binding.plantCollageImage.setImageURI(Uri.fromFile(file))
            }
        }

        if (plant.wikiSummary.isNotEmpty()) {
            binding.tvWikiSummary.text = plant.wikiSummary
            binding.tvWikiLink.visibility = View.VISIBLE
            
            if (plant.wikiImageURL.isNotEmpty()) {
                binding.wikiImage.visibility = View.VISIBLE
                // Implement your image loader (Glide/Coil) setup here if needed
            }
        }
    }

    override fun updateActionBarTitle(nickname: String) {
        supportActionBar?.title = if (nickname.isBlank()) "Plant Details" else "$nickname's Details"
    }

    override fun updateSaveButtonState(isSaving: Boolean, text: String) {
        binding.btnUpdate.isEnabled = isSaving
        binding.btnUpdate.text = text
    }

    override fun showSuccessMessage(message: String) {
        // Safe check using root layout identifier view bounding bindings
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun closeScreen() {
        finish()
    }

    private fun addInfoRow(title: String, value: String) {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, binding.infoContainer, false)
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)
        
        text1.text = title
        text1.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
        text1.textSize = 12f
        
        text2.text = value
        text2.textSize = 16f
        
        binding.infoContainer.addView(view)
    }

    override fun onSupportNavigateUp(): Boolean {
        closeScreen()
        return true
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}