package com.yatsenko.imagepicker.ui.viewer.core

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.yatsenko.imagepicker.ui.viewer.widgets.video.ExoVideoView2

interface ImageLoader {
    fun load(view: ImageView, data: Photo, viewHolder: RecyclerView.ViewHolder) {}
    fun load(subsamplingView: SubsamplingScaleImageView, data: Photo, viewHolder: RecyclerView.ViewHolder) {}
    fun load(exoVideoView: ExoVideoView2, data: Photo, viewHolder: RecyclerView.ViewHolder) {}
}