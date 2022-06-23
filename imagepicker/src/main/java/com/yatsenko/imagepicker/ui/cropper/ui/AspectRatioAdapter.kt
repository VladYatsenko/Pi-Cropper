package com.yatsenko.imagepicker.ui.cropper.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.widgets.aspectRatio.AspectRatioPreviewView

class AspectRatioAdapter: RecyclerView.Adapter<AspectRatioAdapter.ViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<AspectRatioPreviewView.Data>() {

        override fun areItemsTheSame(oldItem: AspectRatioPreviewView.Data, newItem: AspectRatioPreviewView.Data): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: AspectRatioPreviewView.Data, newItem: AspectRatioPreviewView.Data): Boolean {
            return oldItem === newItem
        }

    }

    private val differ = AsyncListDiffer(this, callback)

    var result: (AdapterResult) -> Unit = {}

    fun submitList(ratios: List<AspectRatioPreviewView.Data>) {
        differ.submitList(ratios)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(AspectRatioPreviewView.create(parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class ViewHolder(private val view: AspectRatioPreviewView) : RecyclerView.ViewHolder(view) {

        fun bind(item: AspectRatioPreviewView.Data) {
            view.data = item
            itemView.setOnClickListener {
                if (item.isSelected) return@setOnClickListener
                result(AdapterResult.OnAspectRatioClicked(item))
            }
        }

    }

}