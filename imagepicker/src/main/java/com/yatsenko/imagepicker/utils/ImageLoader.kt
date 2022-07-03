package com.yatsenko.imagepicker.utils

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.widgets.video.ExoVideoView2

interface MediaLoader {
    fun load(view: ImageView, data: Media.Image) {}
    fun load(exoVideoView: ExoVideoView2, data: Media.Video, viewHolder: RecyclerView.ViewHolder) {}
    fun load(subsamplingView: SubsamplingScaleImageView, data: Media.SubsamplingImage, viewHolder: RecyclerView.ViewHolder) {}
}

object ImageLoader: MediaLoader {

    override fun load(view: ImageView, data: Media.Image) {
        Glide.with(view)
            .load(data.mediaPath)
            .placeholder(view.drawable)
            .into(view)
    }

    override fun load(exoVideoView: ExoVideoView2, data: Media.Video, viewHolder: RecyclerView.ViewHolder) {

    }

    override fun load(subsamplingView: SubsamplingScaleImageView, data: Media.SubsamplingImage, viewHolder: RecyclerView.ViewHolder) {

    }

}
