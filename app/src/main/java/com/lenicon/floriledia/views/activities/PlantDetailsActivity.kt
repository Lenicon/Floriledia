package com.lenicon.floriledia.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.lenicon.floriledia.databinding.ActivityPlantDetailsBinding
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.contracts.PlantDetailsContract
import com.lenicon.floriledia.presenters.PlantDetailsPresenter
import com.lenicon.floriledia.views.dialogs.FullImageViewerDialog

class PlantDetailsActivity : AppCompatActivity(), PlantDetailsContract.View {

    private lateinit var binding: ActivityPlantDetailsBinding
    private lateinit var presenter: PlantDetailsContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val plantData = IntentCompat.getParcelableExtra(intent, "extra_plant_result", PlantResult::class.java)
            ?: return finish()

        val modeValue = intent.getStringExtra("extra_view_mode") ?: PlantDetailsContract.Mode.RESULT.name
        val activeMode = PlantDetailsContract.Mode.valueOf(modeValue)

        presenter = PlantDetailsPresenter(plantData, activeMode, lifecycleScope)
        presenter.attachView(this)

        configureListeners()
    }

    private fun configureListeners() {
        binding.btnPrimary.setOnClickListener {
            presenter.handlePrimaryAction(
                binding.etNickname.text.toString(),
                binding.etNotes.text.toString()
            )
        }

        binding.btnSecondary.setOnClickListener {
            presenter.handleSecondaryAction()
        }

        binding.btnDeleteCollection.setOnClickListener {
            presenter.handleDeleteRequested()
        }

        binding.btnWikiSource.setOnClickListener {
            presenter.onWikipediaClicked()
        }
    }

    override fun setupUiForMode(mode: PlantDetailsContract.Mode) {
        if (mode == PlantDetailsContract.Mode.DETAILS) {
            binding.toolbar.title = "Plant Details"
            binding.btnPrimary.text = "Update Details"
            binding.btnSecondary.text = "Back"
            binding.btnDeleteCollection.visibility = View.VISIBLE
        } else {
            binding.toolbar.title = "Identification Result"
            binding.btnPrimary.text = "Save Plant"
            binding.btnSecondary.text = "Discard"
            binding.btnDeleteCollection.visibility = View.GONE
        }
    }

    override fun showPlantDetails(result: PlantResult) {
        binding.etNickname.setText(result.nickname)
        binding.etNotes.setText(result.notes)
        binding.tvScientificName.text = result.scientificName
        binding.tvAuthorship.text = result.authorship
        binding.tvFamily.text = result.family
        binding.tvCommonNames.text = result.commonNames.joinToString(", ")

        if (result.wikiSummary.isBlank()) {
            binding.wikiCard.visibility = View.GONE
        } else {
            binding.wikiCard.visibility = View.VISIBLE
            binding.tvWikiSummary.text = result.wikiSummary
            
            if (result.wikiImageURL.isNotBlank()) {
                binding.ivWikiImage.visibility = View.VISIBLE
                Glide.with(this).load(result.wikiImageURL).into(binding.ivWikiImage)
            } else {
                binding.ivWikiImage.visibility = View.GONE
            }
        }

        binding.plantCollageView.setImages(result.imagePaths)
        binding.plantCollageView.onImageClickListener = { list, index ->
            FullImageViewerDialog(list, index).show(supportFragmentManager, "image_viewer")
        }
    }

    override fun updatePrimaryButtonState(isEnabled: Boolean, text: String) {
        binding.btnPrimary.isEnabled = isEnabled
        binding.btnPrimary.text = text
    }

    override fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to remove this plant from your collection?")
            .setPositiveButton("Delete") { _, _ -> presenter.confirmDeletion() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(error: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun navigateBack() {
        finish()
    }

    override fun openWikipediaLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}