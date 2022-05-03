package com.yatsenko.imagepicker.ui.viewer.core

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.widgets.video.ExoVideoView2

object ImageLoader {

    fun load(view: ImageView, data: Media.Image, viewHolder: RecyclerView.ViewHolder) {
        Glide.with(view)
            .load(data.imagePath)
            .placeholder(view.drawable)
            .into(view)
    }

    fun load(exoVideoView: ExoVideoView2, data: Media.Video, viewHolder: RecyclerView.ViewHolder) {

    }

    fun load(subsamplingView: SubsamplingScaleImageView, data: Media.SubsamplingImage, viewHolder: RecyclerView.ViewHolder) {

    }

}
