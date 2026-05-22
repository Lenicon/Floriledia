package com.lenicon.floriledia.views.components

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import java.io.File

class CollageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Define a callback mechanism so your Activity can intercept image clicks
    var onImageClickListener: ((paths: List<String>, selectedIndex: Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
    }

    fun setImages(paths: List<String>) {
        removeAllViews() // Avoid memory leaks/duplicate rendering on state redraws
        val count = paths.size
        if (count == 0) return

        val elementMargin = (2 * resources.displayMetrics.density).toInt()

        when (count) {
            1 -> {
                val img = createImageWrapper(paths[0], 0, paths)
                addView(img, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
            }
            2 -> {
                val img1 = createImageWrapper(paths[0], 0, paths)
                val img2 = createImageWrapper(paths[1], 1, paths)

                addView(img1, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply { marginEnd = elementMargin })
                addView(img2, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
            }
            3 -> {
                val leftImg = createImageWrapper(paths[0], 0, paths)
                val rightContainer = createVerticalContainer()

                val rightImg1 = createImageWrapper(paths[1], 1, paths)
                val rightImg2 = createImageWrapper(paths[2], 2, paths)

                rightContainer.addView(rightImg1, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply { bottomMargin = elementMargin })
                rightContainer.addView(rightImg2, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

                addView(leftImg, LayoutParams(0, LayoutParams.MATCH_PARENT, 4f).apply { marginEnd = elementMargin })
                addView(rightContainer, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
            }
            else -> { // 4+ items
                val leftImg = createImageWrapper(paths[0], 0, paths)
                val rightContainer = createVerticalContainer()

                val rightImg1 = createImageWrapper(paths[1], 1, paths)
                val rightImg2 = createImageWrapper(paths[2], 2, paths)

                // Bottom frame container with overlay check setup
                val bottomStackFrame = FrameLayout(context)
                val rightImg3 = createImageWrapper(paths[3], 3, paths)
                bottomStackFrame.addView(rightImg3, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

                if (count > 4) {
                    val overlay = TextView(context).apply {
                        text = "+${count - 4}"
                        setTextColor(Color.WHITE)
                        textSize = 20f
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.parseColor("#8A000000"))
                        setOnClickListener { onImageClickListener?.invoke(paths, 3) }
                    }
                    bottomStackFrame.addView(overlay, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                }

                rightContainer.addView(rightImg1, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply { bottomMargin = elementMargin })
                rightContainer.addView(rightImg2, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply { bottomMargin = elementMargin })
                rightContainer.addView(bottomStackFrame, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

                addView(leftImg, LayoutParams(0, LayoutParams.MATCH_PARENT, 5f).apply { marginEnd = elementMargin })
                addView(rightContainer, LayoutParams(0, LayoutParams.MATCH_PARENT, 2f))
            }
        }
    }

    private fun createVerticalContainer(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
        }
    }

    private fun createImageWrapper(path: String, index: Int, allPaths: List<String>): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setOnClickListener {
                onImageClickListener?.invoke(allPaths, index)
            }
            Glide.with(context)
                .load(File(path))
                .into(this)
        }
    }
}