package com.lenicon.floriledia.views

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lenicon.floriledia.R
import com.lenicon.floriledia.models.PlantResult
import com.lenicon.floriledia.utils.NavigationHelper
import com.lenicon.floriledia.adapters.PlantAdapter


class FlorilegiumActivity : AppCompatActivity() {

    private lateinit var adapter: PlantAdapter
    private lateinit var tvHeaderTitle: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageButton
    private lateinit var tvEmptyState: TextView
    
    // Simulating your StorageService data
    private var savedPlants: List<PlantResult> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_florilegium)

        // Initialize Views
        tvHeaderTitle = findViewById(R.id.tv_header_title)
        etSearch = findViewById(R.id.et_search)
        btnClearSearch = findViewById(R.id.btn_clear_search)
        tvEmptyState = findViewById(R.id.tv_empty_state)
        val rvPlants = findViewById<RecyclerView>(R.id.rv_plants)

        // Load your data
        loadData()
        tvHeaderTitle.text = "Florilegium (${savedPlants.size})"

        // Setup RecyclerView
        rvPlants.layoutManager = GridLayoutManager(this, 2)
        
        // Connect the click lambda to launch the detailed view
        adapter = PlantAdapter(savedPlants) { plant ->
            val intent = Intent(this, PlantDetailsActivity::class.java).apply {
                putExtra("PLANT_EXTRA", plant)
            }
            startActivity(intent)
        }
        rvPlants.adapter = adapter
        
        setupSearch()

        NavigationHelper.initBottomNavigation(this, R.id.nav_florilegium)
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                
                // Toggle clear button visibility
                btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                // Filter the list
                adapter.filter(query)
                
                // Toggle empty state
                tvEmptyState.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            }
        })

        btnClearSearch.setOnClickListener {
            etSearch.text.clear()
        }
    }

    private fun loadData() {
        // TODO: Replace with your actual database/storage fetching logic
        // This simulates your StorageService.load()
        savedPlants = listOf(
            PlantResult(nickname = "Monstera", imagePaths = listOf(), scientificName = "", authorship = "", family = "", commonNames = listOf()),
            PlantResult(nickname = "Snake Plant", imagePaths = listOf(), scientificName = "", authorship = "", family = "", commonNames = listOf())
        )
    }
}