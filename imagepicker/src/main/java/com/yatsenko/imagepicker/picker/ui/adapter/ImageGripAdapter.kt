package com.yatsenko.imagepicker.picker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.picker.abstractions.IBinder
import com.yatsenko.imagepicker.picker.listeners.OnImageClickListener
import com.yatsenko.imagepicker.picker.model.ImageEntity
import com.yatsenko.imagepicker.picker.utils.ImageDataModel
import kotlinx.android.synthetic.main.item_image.view.*

class ImageGripAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var images: ArrayList<ImageEntity> = ArrayList()
    var single: Boolean = true
    var listener: OnImageClickListener? = null

    fun getData(): ArrayList<ImageEntity> {
        return images
    }

    fun refreshData(images: ArrayList<ImageEntity>?) {
        this.images.clear()
        images?.let {
            this.images.addAll(it)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IBinder)
            holder.bindView()
    }

    inner class ImageItemHolder(view: View) : RecyclerView.ViewHolder(view), IBinder {

        override fun bindView() {
            images.getOrNull(adapterPosition)?.let { image ->
                val isSelected: Boolean = ImageDataModel.instance.hasDataInResult(image)

                itemView.imagePositionTxt.visibility = if (single) View.GONE else View.VISIBLE

                itemView.imagePositionTxt.text =
                    if (isSelected) ImageDataModel.instance.indexOfDataInResult(image).plus(1).toString() else ""
                itemView.imagePositionTxt.background =
                    ContextCompat.getDrawable(itemView.context, if (isSelected) R.drawable.circle_selected else R.drawable.circle)

                Glide.with(itemView.context)
                    .load(image.imagePath)
                    .into(itemView.imagePreviewImg)
            }

            itemView.imagePreviewImg.setOnClickListener {
                listener?.onImageClickListener(adapterPosition)
            }
            itemView.imagePositionTxt.setOnClickListener {
                listener?.onCheckboxClickListener(adapterPosition)
            }
        }

        override fun unbindView() {}
    }

}