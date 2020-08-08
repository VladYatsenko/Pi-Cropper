package com.yatsenko.imagepicker.ui.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.abstractions.IBinder
import com.yatsenko.imagepicker.model.ImageEntity
import kotlinx.android.synthetic.main.item_image.view.*

class ImageGripAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var images: ArrayList<ImageEntity>? = null
//    var onItemClickListener: OnRecyclerItemClickListener? = null

    fun refreshData(images: ArrayList<ImageEntity>? ){
        this.images = ArrayList()
        images?.let {
            this.images?.addAll(it)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false))
    }

    override fun getItemCount(): Int {
        return images?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IBinder)
            holder.bindView()
    }

    inner class ImageItemHolder(view: View) : RecyclerView.ViewHolder(view), IBinder {

        override fun bindView() {
            images?.getOrNull(adapterPosition)?.let { image ->
                Glide.with(itemView.context)
                    .load(image.imagePath)
                    .into(itemView.imagePreviewImg)
            }
        }

        override fun unbindView() {

        }
    }

}