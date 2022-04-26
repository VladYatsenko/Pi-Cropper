package com.yatsenko.imagepicker.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.utils.checkboxPosition
import com.yatsenko.imagepicker.utils.loadImage
import kotlinx.android.synthetic.main.item_image.view.*

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

    fun bind(image: Image, single: Boolean, result: (AdapterResult) -> Unit) {
        position.checkboxPosition(image, single, result)
        imageView.loadImage(image)

        imageView.setOnClickListener {
            result(AdapterResult.OnImageClicked(image, bindingAdapterPosition))
        }
    }

}