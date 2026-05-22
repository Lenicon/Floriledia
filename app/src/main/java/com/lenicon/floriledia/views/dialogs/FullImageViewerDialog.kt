package com.lenicon.floriledia.views.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.lenicon.floriledia.R
import com.lenicon.floriledia.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FullImageViewerDialog(
    private val paths: List<String>,
    private val initialIndex: Int
) : DialogFragment() {

    private var currentIndex = initialIndex

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_full_image_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val btnClose = view.findViewById<ImageView>(R.id.btn_close)
        val btnDownload = view.findViewById<ImageView>(R.id.btn_download)

        val adapter = ImagePagerAdapter(paths)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(initialIndex, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentIndex = position
            }
        })

        btnClose.setOnClickListener { dismiss() }

        btnDownload.setOnClickListener {
            val currentPath = paths[currentIndex]
            
            if (StorageService.isSaved(currentPath)) {
                Toast.makeText(requireContext(), "Image already saved!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    
                    withContext(Dispatchers.IO) {
                        StorageService.saveImagePermanently(currentPath)
                        StorageService.markAsSaved(currentPath)
                    }
                    
                    Toast.makeText(requireContext(), "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Couldn't save image: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private inner class ImagePagerAdapter(private val imagePaths: List<String>) :
        RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view as ImageView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val imgView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            return ViewHolder(imgView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.imageView.context)
                .load(File(imagePaths[position]))
                .into(holder.imageView)
        }

        override fun getItemCount(): Int = imagePaths.size
    }
}