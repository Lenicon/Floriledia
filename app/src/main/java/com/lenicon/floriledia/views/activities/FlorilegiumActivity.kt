package com.lenicon.floriledia.views.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.lenicon.floriledia.R
import com.lenicon.floriledia.adapters.PlantAdapter
import com.lenicon.floriledia.contracts.FlorilegiumContract
import com.lenicon.floriledia.contracts.PlantDetailsContract
import com.lenicon.floriledia.databinding.ActivityFlorilegiumBinding
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.presenters.FlorilegiumPresenter
import com.lenicon.floriledia.utils.NavigationHelper

class FlorilegiumActivity : AppCompatActivity(), FlorilegiumContract.View {

    private lateinit var binding: ActivityFlorilegiumBinding
    private lateinit var presenter: FlorilegiumContract.Presenter
    private var adapter: PlantAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlorilegiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = FlorilegiumPresenter(lifecycleScope)
        presenter.attachView(this)

        setupRecyclerView()
        setupSearch()
        
        NavigationHelper.initBottomNavigation(this, R.id.nav_florilegium)

        // Begin collecting active profile dataset changes
        presenter.startObservingCollection()
    }

    private fun setupRecyclerView() {
        binding.rvPlants.layoutManager = GridLayoutManager(this, 2)
        
        // Pass item selection events straight up to the presenter interaction hub
        adapter = PlantAdapter(emptyList()) { plant ->
            presenter.onPlantClicked(plant)
        }
        binding.rvPlants.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                presenter.onSearchQueryChanged(s.toString())
            }
        })

        binding.btnClearSearch.setOnClickListener {
            presenter.onClearSearchClicked()
        }
    }

    // --- MVP View Implementations ---

    override fun showPlants(plants: List<PlantResult>) {
        // Assume your PlantAdapter supports dataset replacement cleanly
        adapter?.updateData(plants)
    }

    override fun updateHeaderTitle(count: Int) {
        binding.tvHeaderTitle.setText("Florilegium ($count)")
    }

    override fun toggleEmptyState(isVisible: Boolean) {
        binding.tvEmptyState.setVisibility(if (isVisible) View.VISIBLE else View.GONE)
    }

    override fun toggleClearSearchButton(isVisible: Boolean) {
        binding.btnClearSearch.setVisibility(if (isVisible) View.VISIBLE else View.GONE)
    }

    override fun navigateToDetails(plant: PlantResult) {
        val intent = Intent(context, PlantDetailsActivity::class.java).apply {
            putExtra("extra_plant_result", existingSavedPlant)
            putExtra("extra_view_mode", PlantDetailsContract.Mode.DETAILS.name)
        }
        startActivity(intent)
    }

    override fun clearSearchInput() {
        binding.etSearch.getText()?.clear()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}