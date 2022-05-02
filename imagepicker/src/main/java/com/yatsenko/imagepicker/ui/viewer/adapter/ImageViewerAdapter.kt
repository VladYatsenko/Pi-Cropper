package com.yatsenko.imagepicker.ui.viewer.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.core.Photo
import com.yatsenko.imagepicker.ui.viewer.viewholders.PhotoViewHolder
import com.yatsenko.imagepicker.ui.viewer.viewholders.SubsamplingViewHolder
import com.yatsenko.imagepicker.ui.viewer.viewholders.UnknownViewHolder
import com.yatsenko.imagepicker.ui.viewer.viewholders.VideoViewHolder
import java.util.*

class ImageViewerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.PHOTO -> PhotoViewHolder(parent, callback)
            ItemType.SUBSAMPLING -> SubsamplingViewHolder(parent, callback)
            ItemType.VIDEO -> VideoViewHolder(parent, callback)
            else -> UnknownViewHolder(View(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PhotoViewHolder -> item?.extra<Photo>()?.let { holder.bind(it) }
            is SubsamplingViewHolder -> item?.extra<Photo>()?.let { holder.bind(it) }
            is VideoViewHolder -> item?.extra<Photo>()?.let { holder.bind(it) }
        }

        if (item?.id == key) {
            listener?.onInit(holder)
            key = NO_ID
        }
    }

    override fun getItemId(position: Int): Long = provideItem(position)?.id ?: NO_ID

    override fun getItemViewType(position: Int) = provideItem(position)?.type ?: ItemType.UNKNOWN

    private val callback: ImageViewerAdapterListener = object : ImageViewerAdapterListener {
        override fun onInit(viewHolder: RecyclerView.ViewHolder) {
            listener?.onInit(viewHolder)
        }

        override fun onDrag(viewHolder: RecyclerView.ViewHolder, view: View, fraction: Float) {
            listener?.onDrag(viewHolder, view, fraction)
        }

        override fun onRelease(viewHolder: RecyclerView.ViewHolder, view: View) {
            listener?.onRelease(viewHolder, view)
        }

        override fun onRestore(viewHolder: RecyclerView.ViewHolder, view: View, fraction: Float) {
            listener?.onRestore(viewHolder, view, fraction)
        }
    }

}
