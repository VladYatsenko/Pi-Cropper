package com.yatsenko.imagepicker.ui.viewer.utils

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder

interface OverlayCustomizer {
    fun provideView(parent: ViewGroup): View? = null
}

interface VHCustomizer {
    fun initialize(type: Int, viewHolder: RecyclerView.ViewHolder) {}
    fun bind(type: Int, data: Media, viewHolder: RecyclerView.ViewHolder) {}
}

interface ViewerCallback : ImageViewerAdapterListener {
    override fun onInit(viewHolder: FullscreenViewHolder) {}
    override fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {}
    override fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {}
    override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {}
}