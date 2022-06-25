package com.yatsenko.imagepicker.ui.viewer.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.core.VHCustomizer
import com.yatsenko.imagepicker.ui.viewer.viewholders.*

private val callback = object : DiffUtil.ItemCallback<Media>() {

    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem === newItem
    }

}

class ImageViewerAdapter(
    initKey: String,
    private val vhCustomizer: VHCustomizer,
) : PagedListAdapter<Media, FullscreenViewHolder>(callback) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullscreenViewHolder {
        return when (viewType) {
            ImageViewHolder.ITEM_TYPE -> ImageViewHolder.create(parent, vhCustomizer, internalListener)
//            SubsamplingViewHolder.ITEM_TYPE -> SubsamplingViewHolder.create(parent, vhCustomizer, internalListener)
//            VideoViewHolder.ITEM_TYPE -> VideoViewHolder.create(parent, vhCustomizer, internalListener)
            else -> UnknownViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: FullscreenViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ImageViewHolder -> (item as? Media.Image)?.let { holder.bind(it) }
//            is SubsamplingViewHolder -> (item as? Media.SubsamplingImage)?.let { holder.bind(it) }
//            is VideoViewHolder -> (item as? Media.Video)?.let { holder.bind(it) }
        }

        if (item?.id == key) {
            internalListener.onInit(holder)
            key = null
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.lastModified ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is Media.Image -> ImageViewHolder.ITEM_TYPE
//            is Media.SubsamplingImage -> SubsamplingViewHolder.ITEM_TYPE
//            is Media.Video -> VideoViewHolder.ITEM_TYPE
            else -> -1
        }
    }

//    override fun getItemCount(): Int = list.size

}
