package com.yatsenko.imagepicker.ui.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.utils.extensions.checkboxPosition
import com.yatsenko.imagepicker.utils.extensions.loadImage
import java.util.concurrent.atomic.AtomicBoolean

class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        fun create(parent: ViewGroup) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    private val enterTransitionStarted = AtomicBoolean()


    private val imageView = view.findViewById<ImageView>(R.id.image)
    private val position = view.findViewById<TextView>(R.id.position)

    val transitionImageView: ImageView
        get() = imageView

    fun bind(image: Image, single: Boolean, viewerPosition: () -> Int, result: (AdapterResult) -> Unit) {
        position.checkboxPosition(image, single, result)
        imageView.loadImage(image) {
            if (viewerPosition() != bindingAdapterPosition) return@loadImage
            if (enterTransitionStarted.getAndSet(true)) return@loadImage

            result(AdapterResult.ImageLoaded)
        }

        imageView.transitionName = image.id

        itemView.setOnClickListener {
            result(AdapterResult.OnImageClicked(imageView, image, bindingAdapterPosition))
        }
    }

}