package com.lenicon.floriledia.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.lenicon.floriledia.databinding.ActivityResultScreenBinding
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.contracts.ResultContract
import com.lenicon.floriledia.presenters.ResultPresenter
import androidx.core.content.IntentCompat


class ResultScreenActivity : AppCompatActivity(), ResultContract.View {

    private lateinit var binding: ActivityResultScreenBinding
    private lateinit var presenter: ResultContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Extract your plant result data structure from the intent parcel
        val result = IntentCompat.getParcelableExtra(intent, "extra_plant_result", PlantResult::class.java)
            ?: return finish()

        presenter = ResultPresenter(result, lifecycleScope)
        presenter.attachView(this)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        binding.btnSave.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            val notes = binding.etNotes.text.toString()
            presenter.savePlant(nickname, notes)
        }

        binding.btnDiscard.setOnClickListener {
            presenter.discard()
        }

        binding.btnWikiSource.setOnClickListener {
            presenter.onWikipediaClicked()
        }
    }

    override fun showPlantDetails(result: PlantResult) {
        binding.etNickname.setText(result.nickname)
        binding.etNotes.setText(result.notes)
        
        // Explicitly use standard setter methods to bypass property-syntax compilation limits
        binding.tvScientificName.setText(result.scientificName)
        binding.tvAuthorship.setText(result.authorship)
        binding.tvFamily.setText(result.family)
        binding.tvCommonNames.setText(result.commonNames.joinToString(", "))

        if (result.wikiSummary.isBlank()) {
            binding.wikiCard.setVisibility(View.GONE)
        } else {
            binding.wikiCard.setVisibility(View.VISIBLE)
            binding.tvWikiSummary.setText(result.wikiSummary)
            
            if (result.wikiImageURL.isNotBlank()) {
                Glide.with(this)
                    .load(result.wikiImageURL)
                    .into(binding.ivWikiImage)
            } else {
                binding.ivWikiImage.setVisibility(View.GONE)
            }
        }

        buildDynamicCollage(result.imagePaths)
    }

    override fun updateSaveButtonState(isSaving: Boolean) {
        binding.btnSave.setEnabled(!isSaving)
        binding.btnSave.setText(if (isSaving) "Saving..." else "Save Plant")
    }
    
    private fun buildDynamicCollage(paths: List<String>) {
        val collageView = binding.plantCollageView
        collageView.setImages(paths)
        collageView.onImageClickListener = { allPaths, selectedIndex ->
            openFullImage(allPaths, selectedIndex)
        }
    }

    private fun openFullImage(paths: List<String>, initialIndex: Int) {
        FullImageViewerDialog(paths, initialIndex).show(supportFragmentManager, "image_viewer")
    }
    
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(error: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("Okay") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun navigateBack() {
        finish()
    }

    override fun openWikipediaLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}