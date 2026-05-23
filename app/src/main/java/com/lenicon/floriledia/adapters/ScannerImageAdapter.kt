package com.lenicon.floriledia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.lenicon.floriledia.R
import com.lenicon.floriledia.models.PlantPhoto
import java.io.File
import android.net.Uri

class ScannerImageAdapter(
    private val photos: List<PlantPhoto>,
    private val maxPhotos: Int,
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int {
        return if (photos.size < maxPhotos) photos.size + 1 else photos.size
    }

    override fun getItem(position: Int): Any? {
        return if (position < photos.size) photos[position] else null
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        
        return if (position < photos.size) {
            val view = inflater.inflate(R.layout.item_scanner_thumbnail, parent, false)
            val ivThumb = view.findViewById<ImageView>(R.id.ivPlantThumb)
            val tvOrgan = view.findViewById<TextView>(R.id.tvOrganTag)
            val btnDelete = view.findViewById<View>(R.id.viewDeleteCircle)

            val item = photos[position]
            val file = File(item.path)
            if (file.exists()) ivThumb.setImageURI(Uri.fromFile(file))
            
            tvOrgan.text = item.organ.uppercase()
            btnDelete.setOnClickListener { onDeleteClick(position) }
            view
        } else {
            val view = inflater.inflate(R.layout.item_scanner_add_slot, parent, false)
            view.setOnClickListener { onAddClick() }
            view
        }
    }
}