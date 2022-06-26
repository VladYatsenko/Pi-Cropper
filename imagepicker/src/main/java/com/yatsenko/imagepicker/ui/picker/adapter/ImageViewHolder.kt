package com.yatsenko.imagepicker.ui.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.core.ViewerTransitionHelper
import com.yatsenko.imagepicker.utils.extensions.EdgeToEdge.updateMargin
import com.yatsenko.imagepicker.utils.extensions.checkboxPosition
import com.yatsenko.imagepicker.utils.extensions.dpToPx
import com.yatsenko.imagepicker.utils.extensions.loadImage


class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = R.layout.item_image
        fun create(parent: ViewGroup) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    private val thumbnail = view.findViewById<ImageView>(R.id.image)
    private val position = view.findViewById<TextView>(R.id.position)
    private val positionContainer = view.findViewById<FrameLayout>(R.id.position_container)

    private var media: Media? = null
    private var anim: Animation? = null

    fun bind(media: Media, result: (AdapterResult) -> Unit) {
        if (this.media?.id == media.id && this.media?.isSelected != media.isSelected)
            thumbnail.scaleView(media.isSelected)
        else thumbnail.changeScale(media.isSelected)

        this.media = media
        position.checkboxPosition(media, true)
        positionContainer.setOnClickListener {
            result(AdapterResult.OnSelectImageClicked(media))
        }
        thumbnail.loadImage(media)
        thumbnail.alpha = if (media.inFullscreen) 0f else 1f

        itemView.setOnClickListener {
//            thumbnail.changeScale(media.isSelected)
            result(AdapterResult.OnImageClicked(thumbnail, media, bindingAdapterPosition))
        }

        ViewerTransitionHelper.put(media.id, thumbnail)
    }

    fun refreshTransitionView() {
        val id = media?.id ?: return
        ViewerTransitionHelper.put(id, thumbnail)
    }

    private fun View.scaleView(isSelected: Boolean) {
        val selectedMargin = dpToPx(16).toInt()

        anim?.cancel()
        anim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val newMargin = (if (isSelected) (selectedMargin * interpolatedTime) else selectedMargin + ((0 - selectedMargin) * interpolatedTime)).toInt()
                updateMargin(newMargin, newMargin, newMargin, newMargin)
            }
        }
        anim?.fillAfter = true // Needed to keep the result of the animation
        anim?.duration = 200
        this.startAnimation(anim)
    }

    private fun View.changeScale(isSelected: Boolean) {
        val margin = (if (isSelected) dpToPx(16) else 0).toInt()
        updateMargin(margin, margin, margin, margin)
    }

}