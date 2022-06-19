package com.yatsenko.imagepicker.ui.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.core.ViewerTransitionHelper
import com.yatsenko.imagepicker.utils.extensions.checkboxPosition
import com.yatsenko.imagepicker.utils.extensions.loadImage
import java.util.concurrent.atomic.AtomicBoolean

class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        fun create(parent: ViewGroup) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    private val imageView = view.findViewById<ImageView>(R.id.image)
    private val position = view.findViewById<TextView>(R.id.position)

    private var media: Media? = null

    fun bind(media: Media, single: Boolean, result: (AdapterResult) -> Unit) {
        this.media = media

        position.checkboxPosition(media, single, result)
        imageView.loadImage(media) {}
        imageView.alpha = if (media.inFullscreen) 0f else 1f

        itemView.setOnClickListener {
            result(AdapterResult.OnImageClicked(imageView, media, bindingAdapterPosition))
        }

        ViewerTransitionHelper.put(media.id, imageView)
    }

    fun refreshTransitionView() {
        val id = media?.id ?: return
        ViewerTransitionHelper.put(id, imageView)
    }

}