package com.yatsenko.picropper.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isInvisible
import com.yatsenko.picropper.R
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.picropper.utils.ImageLoader
import com.yatsenko.picropper.ui.viewer.utils.VHCustomizer
import com.yatsenko.picropper.ui.viewer.utils.Config
import com.yatsenko.picropper.utils.extensions.resetScale
import com.yatsenko.picropper.utils.transition.TransitionEndHelper
import com.yatsenko.picropper.utils.transition.TransitionStartHelper
import com.yatsenko.picropper.widgets.imageview.PhotoView2
import kotlin.math.max

internal class ImageViewHolder(
    val view: View,
    val vhCustomizer: VHCustomizer,
    callback: ImageViewerAdapterListener) : FullscreenViewHolder(view) {
    
    companion object {
        val ITEM_TYPE: Int = R.layout.item_imageviewer_photo

        fun create(
            parent: ViewGroup,
            vhCustomizer: VHCustomizer,
            callback: ImageViewerAdapterListener) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_imageviewer_photo, parent, false),
            vhCustomizer,
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

    init {
        vhCustomizer.initialize(ITEM_TYPE, this)
    }

    override var data: Media? = null
    override var endView: View = photoView

    fun bind(data: Media.Image) {
        this.data = data
        ImageLoader.load(photoView, data)
        photoView.isInvisible = data.hideInViewer
        vhCustomizer.bind(ITEM_TYPE, data, this)
    }

    override fun resetScale(): Boolean {
        return photoView.resetScale(true)
    }

    override fun afterTransitionStart() {
        (data as? Media.Image)?.let {
            ImageLoader.load(photoView, it)
        }
    }

    override fun beforeTransitionStart(startView: View?) {
        startView?.alpha = 0f

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
            startView.animate()
                .setDuration(0)
                .setStartDelay(max(Config.durationTransition - 20, 0))
                .alpha(1f).start()
        } else {
            photoView.animate()
                .setDuration(Config.durationTransition)
                .alpha(0f).start()
        }
    }

}