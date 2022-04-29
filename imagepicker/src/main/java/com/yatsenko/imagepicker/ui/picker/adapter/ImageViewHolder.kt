package com.yatsenko.imagepicker.ui.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.utils.checkboxPosition
import com.yatsenko.imagepicker.utils.loadImage

class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        fun create(parent: ViewGroup) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    private val imageView = view.findViewById<ImageView>(R.id.image)
    private val position = view.findViewById<TextView>(R.id.position)

    val transitionImageView: ImageView?
        get() = imageView

    fun bind(image: Image, single: Boolean, viewerPosition: () -> Int, result: (AdapterResult) -> Unit) {
        position.checkboxPosition(image, single, result)
        imageView.loadImage(image)

        itemView.setOnClickListener {
            result(AdapterResult.OnImageClicked(image, bindingAdapterPosition))
        }

        val isViewing = viewerPosition() == bindingAdapterPosition
        imageView.isInvisible = isViewing
        if (isViewing)
            result(AdapterResult.OnBindImage(transitionImageView))
    }

}