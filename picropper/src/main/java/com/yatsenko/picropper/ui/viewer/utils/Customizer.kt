package com.yatsenko.picropper.ui.viewer.utils

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.picropper.ui.viewer.viewholders.FullscreenViewHolder

interface OverlayCustomizer {
    fun provideView(parent: ViewGroup): View? = null
}

internal interface VHCustomizer {
    fun initialize(type: Int, viewHolder: RecyclerView.ViewHolder) {}
    fun bind(type: Int, data: Media, viewHolder: RecyclerView.ViewHolder) {}
}

internal interface ViewerCallback : ImageViewerAdapterListener {
    override fun onInit(viewHolder: FullscreenViewHolder) {}
    override fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {}
    override fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {}
    override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {}
}