package com.yatsenko.picropper.ui.picker.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media

internal class MediaGripAdapter(
    private val single: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Media>() {

        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem === newItem
        }

    }

    init {
        setHasStableIds(true)
    }

    private val differ = AsyncListDiffer(this, callback)

    val list: List<Media>
        get() = differ.currentList

    var result: (AdapterResult) -> Unit = {}

    fun submitList(list: List<Media>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MediaViewHolder.VIEW_TYPE -> MediaViewHolder.create(parent)
            else -> CameraViewHolder.create(parent)
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MediaViewHolder -> list[position].let { image ->
                holder.bind(image, single) { result(it) }
            }
            is CameraViewHolder -> holder.bind(result)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        when (holder) {
            is MediaViewHolder -> holder.refreshTransitionView()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(list[position]) {
            is Media.Camera -> CameraViewHolder.VIEW_TYPE
            else -> MediaViewHolder.VIEW_TYPE
        }
    }

    override fun getItemId(position: Int): Long {
        return list.getOrNull(position)?.lastModified ?: -1L
    }

}