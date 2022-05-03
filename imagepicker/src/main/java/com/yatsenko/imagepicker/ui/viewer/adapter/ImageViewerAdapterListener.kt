package com.yatsenko.imagepicker.ui.viewer.adapter

import android.view.View
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder

interface ImageViewerAdapterListener {
    fun onInit(viewHolder: FullscreenViewHolder)
    fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float)
    fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float)
    fun onRelease(viewHolder: FullscreenViewHolder, view: View)
}