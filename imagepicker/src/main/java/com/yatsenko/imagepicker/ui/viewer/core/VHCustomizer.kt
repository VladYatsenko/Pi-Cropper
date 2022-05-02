package com.yatsenko.imagepicker.ui.viewer.core

import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.ui.viewer.core.Photo

interface VHCustomizer {
    fun initialize(type: Int, viewHolder: RecyclerView.ViewHolder) {}
    fun bind(type: Int, data: Photo, viewHolder: RecyclerView.ViewHolder) {}
}
