package com.yatsenko.picropper.ui.viewer.adapter

import android.view.View
import com.yatsenko.picropper.ui.viewer.viewholders.FullscreenViewHolder

internal interface ImageViewerAdapterListener {
    fun onInit(viewHolder: FullscreenViewHolder)
    fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float)
    fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float)
    fun onRelease(viewHolder: FullscreenViewHolder, view: View)
}