package com.yatsenko.picropper.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.yatsenko.picropper.R
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.picropper.utils.ImageLoader
import com.yatsenko.picropper.ui.viewer.utils.Config
import com.yatsenko.picropper.utils.transition.TransitionEndHelper
import com.yatsenko.picropper.utils.transition.TransitionStartHelper
import com.yatsenko.picropper.widgets.video.ExoVideoView2
import kotlin.math.max

internal class VideoViewHolder(
    view: View,
    callback: ImageViewerAdapterListener
) : FullscreenViewHolder(view) {

    companion object {
        val ITEM_TYPE: Int = R.layout.item_imageviewer_video

        fun create(parent: ViewGroup, callback: ImageViewerAdapterListener) = VideoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_imageviewer_video, parent, false),
            callback
        )
    }

    private val videoView = view.findViewById<ExoVideoView2>(R.id.video_view).apply {
        addListener(object : ExoVideoView2.Listener {
            override fun onDrag(view: ExoVideoView2, fraction: Float) =
                callback.onDrag(this@VideoViewHolder, view, fraction)

            override fun onRestore(view: ExoVideoView2, fraction: Float) =
                callback.onRestore(this@VideoViewHolder, view, fraction)

            override fun onRelease(view: ExoVideoView2) = callback.onRelease(this@VideoViewHolder, view)
        })
    }
    private val imageView = view.findViewById<ImageView>(R.id.image_view)

    override var data: Media? = null
    override var endView: View = imageView

    fun bind(data: Media.Video) {
        this.data = data
        ImageLoader.load(videoView, data, this)
    }

    override fun beforeTransitionStart(startView: View?) {
        imageView.layoutParams = imageView.layoutParams.apply {
            width = startView?.width ?: width
            height = startView?.height ?: height
            val location = IntArray(2)
            TransitionStartHelper.getLocationOnScreen(startView, location)
            if (this is ViewGroup.MarginLayoutParams) {
                marginStart = location[0]
                topMargin = location[1] - Config.transitionOffsetY
            }
        }
    }

    override fun beforeTransitionEnd(startView: View?) {
        imageView.translationX = videoView.translationX
        imageView.translationY = videoView.translationY
        imageView.scaleX = videoView.scaleX
        imageView.scaleY = videoView.scaleY
        imageView.visibility = View.VISIBLE
        videoView.visibility = View.GONE
    }

    override fun transitionStart() {
        imageView.layoutParams = imageView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            if (this is ViewGroup.MarginLayoutParams) {
                marginStart = 0
                topMargin = 0
            }
        }
    }

    override fun transitionEnd(startView: View?) {
        imageView.translationX = 0f
        imageView.translationY = 0f
        imageView.scaleX = if (startView != null) 1f else 2f
        imageView.scaleY = if (startView != null) 1f else 2f
        // photoView.alpha = startView?.alpha ?: 0f
        fade(startView)
        videoView.pause()
        imageView.layoutParams = imageView.layoutParams.apply {
            width = startView?.width ?: width
            height = startView?.height ?: height
            val location = IntArray(2)
            TransitionEndHelper.getLocationOnScreen(startView, location)
            if (this is ViewGroup.MarginLayoutParams) {
                marginStart = location[0]
                topMargin = location[1] - Config.transitionOffsetY
            }
        }
    }

    override fun fade(startView: View?) {
        if (startView != null) {
            imageView.animate()
                .setDuration(0)
                .setStartDelay(max(Config.durationTransition - 20, 0))
                .alpha(0f).start()
        } else {
            imageView.animate().setDuration(Config.durationTransition)
                .alpha(0f).start()
        }
    }
}