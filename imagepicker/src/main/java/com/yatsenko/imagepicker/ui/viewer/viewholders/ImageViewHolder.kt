package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.core.ImageLoader
import com.yatsenko.imagepicker.ui.viewer.utils.Config
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionEndHelper
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionStartHelper
import com.yatsenko.imagepicker.widgets.imageview.PhotoView2
import kotlin.math.max

class ImageViewHolder(val view: View, callback: ImageViewerAdapterListener) : FullscreenViewHolder(view) {
    
    companion object {
        val ITEM_TYPE: Int = R.layout.item_imageviewer_photo

        fun create(parent: ViewGroup, callback: ImageViewerAdapterListener) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_imageviewer_photo, parent, false),
            callback
        )
    }
    
    private val photoView = view.findViewById<PhotoView2>(R.id.photo_view).apply {
        setListener(object : PhotoView2.Listener {
            override fun onDrag(view: PhotoView2, fraction: Float) = callback.onDrag(this@ImageViewHolder, view, fraction)
            override fun onRestore(view: PhotoView2, fraction: Float) = callback.onRestore(this@ImageViewHolder, view, fraction)
            override fun onRelease(view: PhotoView2) = callback.onRelease(this@ImageViewHolder, view)
        })
    }

    override var data: Media? = null
    override var endView: View = photoView

    fun bind(data: Media.Image) {
        this.data = data
        ImageLoader.load(photoView, data, this)
    }

    override fun afterTransitionStart() {
        (data as? Media.Image)?.let {
            ImageLoader.load(photoView, it, this)
        }
    }

    override fun beforeTransitionStart(startView: View?) {
        photoView.scaleType = (startView as? ImageView?)?.scaleType ?: ImageView.ScaleType.FIT_CENTER
        photoView.layoutParams = photoView.layoutParams.apply {
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

    override fun transitionStart() {
        photoView.scaleType = ImageView.ScaleType.FIT_CENTER
        photoView.layoutParams = photoView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            if (this is ViewGroup.MarginLayoutParams) {
                marginStart = 0
                topMargin = 0
            }
        }
    }

    override fun beforeTransitionEnd(startView: View?) {}

    override fun transitionEnd(startView: View?) {
        photoView.scaleType = (startView as? ImageView?)?.scaleType
            ?: ImageView.ScaleType.FIT_CENTER
        photoView.translationX = 0f
        photoView.translationY = 0f
        photoView.scaleX = if (startView != null) 1f else 2f
        photoView.scaleY = if (startView != null) 1f else 2f
        // photoView.alpha = startView?.alpha ?: 0f
        fade(startView)
        photoView.layoutParams = photoView.layoutParams.apply {
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
            photoView.animate()
                .setDuration(0)
                .setStartDelay(max(Config.durationTransition - 20, 0))
                .alpha(0f).start()
        } else {
            photoView.animate()
                .setDuration(Config.durationTransition)
                .alpha(0f).start()
        }
    }


}