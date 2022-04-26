package com.yatsenko.imagepicker.picker.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image

class ImageGripAdapter(
    private val single: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Image>() {

        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem === newItem
        }

    }

    private val differ = AsyncListDiffer(this, callback)

    val list: List<Image>
        get() = differ.currentList

    var result: (AdapterResult) -> Unit = {}

    fun submitList(list: List<Image>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageViewHolder.create(parent)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ImageViewHolder -> list[position].let { holder.bind(it, single, result) }
        }
    }

}