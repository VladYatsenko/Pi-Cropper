package com.yatsenko.imagepicker.ui.viewer.core

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder

interface ViewerCallback : ImageViewerAdapterListener {
    override fun onInit(viewHolder: FullscreenViewHolder) {}
    override fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {}
    override fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {}
    override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {}
    fun onPageScrollStateChanged(state: Int) {}
    fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    fun onPageSelected(position: Int, viewHolder: FullscreenViewHolder) {}
}