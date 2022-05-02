package com.yatsenko.imagepicker.ui.viewer.core

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener

interface ViewerCallback : ImageViewerAdapterListener {
    override fun onInit(viewHolder: RecyclerView.ViewHolder) {}
    override fun onDrag(viewHolder: RecyclerView.ViewHolder, view: View, fraction: Float) {}
    override fun onRestore(viewHolder: RecyclerView.ViewHolder, view: View, fraction: Float) {}
    override fun onRelease(viewHolder: RecyclerView.ViewHolder, view: View) {}
    fun onPageScrollStateChanged(state: Int) {}
    fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    fun onPageSelected(position: Int, viewHolder: RecyclerView.ViewHolder) {}
}