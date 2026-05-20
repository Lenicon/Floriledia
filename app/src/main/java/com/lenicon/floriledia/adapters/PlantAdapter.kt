package com.lenicon.floriledia.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lenicon.floriledia.R
import com.lenicon.floriledia.models.PlantResult
import java.io.File

class PlantAdapter(
    private var allPlants: List<PlantResult>,
    private val onPlantClick: (PlantResult) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    private var filteredList: List<PlantResult> = allPlants

    class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPlant: ImageView = view.findViewById(R.id.iv_plant_image)
        val tvNickname: TextView = view.findViewById(R.id.tv_plant_nickname)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant_card, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = filteredList[position]
        
        holder.tvNickname.text = if (plant.nickname.isNotBlank()) plant.nickname else "Unnamed"

        if (plant.imagePaths.isNotEmpty()) {
            val imgFile = File(plant.imagePaths[0])
            if (imgFile.exists()) {
                holder.ivPlant.setImageURI(Uri.fromFile(imgFile))
            }
        }

        holder.itemView.setOnClickListener {
            onPlantClick(plant)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            allPlants
        } else {
            allPlants.filter {
                it.nickname.lowercase().contains(query.lowercase())
            }
        }
        notifyDataSetChanged()
    }
}