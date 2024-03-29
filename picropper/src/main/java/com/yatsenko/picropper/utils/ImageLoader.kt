package com.yatsenko.picropper.utils

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.widgets.video.ExoVideoView2

internal interface MediaLoader {
    fun load(view: ImageView, data: Media.Image) {}
    fun load(exoVideoView: ExoVideoView2, data: Media.Video, viewHolder: RecyclerView.ViewHolder) {}
    fun load(subsamplingView: SubsamplingScaleImageView, data: Media.SubsamplingImage, viewHolder: RecyclerView.ViewHolder) {}
}

internal object ImageLoader: MediaLoader {

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
