package com.yatsenko.imagepicker.ui.viewer.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.viewholders.*

class ImageViewerAdapter(
    initKey: String,
) : RecyclerView.Adapter<FullscreenViewHolder>() {

    private var key: String? = initKey

    var listener: ImageViewerAdapterListener? = null

    private val internalListener = object : ImageViewerAdapterListener {
        override fun onInit(viewHolder: FullscreenViewHolder) {
            listener?.onInit(viewHolder)
        }

        override fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {
            listener?.onDrag(viewHolder, view, fraction)
        }

        override fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {
            listener?.onRestore(viewHolder, view, fraction)
        }

        override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {
            listener?.onRelease(viewHolder, view)
        }

    }

    private val callback = object : DiffUtil.ItemCallback<Media>() {

        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem === newItem
        }

    }

    private val differ = AsyncListDiffer(this, callback)

    val list: List<Media>
        get() = differ.currentList

    fun submitList(list: List<Media>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullscreenViewHolder {
        return when (viewType) {
            ImageViewHolder.ITEM_TYPE -> ImageViewHolder.create(parent, internalListener)
            SubsamplingViewHolder.ITEM_TYPE -> SubsamplingViewHolder.create(parent, internalListener)
            VideoViewHolder.ITEM_TYPE -> VideoViewHolder.create(parent, internalListener)
            else -> UnknownViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: FullscreenViewHolder, position: Int) {
        val item = list[position]
        when (holder) {
            is ImageViewHolder -> (item as? Media.Image)?.let { holder.bind(it) }
            is SubsamplingViewHolder -> (item as? Media.SubsamplingImage)?.let { holder.bind(it) }
            is VideoViewHolder -> (item as? Media.Video)?.let { holder.bind(it) }
        }

        if (item.id == key) {
            internalListener.onInit(holder)
            key = null
        }
    }

    override fun getItemId(position: Int): Long {
        return list[position].lastModified
    }

    override fun getItemViewType(position: Int): Int {
        return when(list[position]){
            is Media.Image -> ImageViewHolder.ITEM_TYPE
            is Media.SubsamplingImage -> SubsamplingViewHolder.ITEM_TYPE
            is Media.Video -> VideoViewHolder.ITEM_TYPE
        }
    }

    override fun getItemCount(): Int = list.size

}
