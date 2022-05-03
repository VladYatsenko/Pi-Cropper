package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.adapter.ItemType
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireVHCustomizer
import com.yatsenko.imagepicker.ui.viewer.core.Photo
import com.yatsenko.imagepicker.ui.viewer.utils.Config
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionEndHelper
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionStartHelper
import com.yatsenko.imagepicker.widgets.imageview.SubsamplingScaleImageView2

class SubsamplingViewHolder(view: View, callback: ImageViewerAdapterListener) : FullscreenViewHolder(view) {

    companion object {
        val ITEM_TYPE: Int = R.layout.item_imageviewer_subsampling

        fun create(parent: ViewGroup, callback: ImageViewerAdapterListener) = SubsamplingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_imageviewer_subsampling, parent, false),
            callback
        )
    }

    private val subsamplingView = view.findViewById<SubsamplingScaleImageView2>(R.id.subsampling).apply {
        setMinimumScaleType(Config.subsamplingScaleType)
        setListener(object : SubsamplingScaleImageView2.Listener {
            override fun onDrag(view: SubsamplingScaleImageView2, fraction: Float) = callback.onDrag(this@SubsamplingViewHolder, view, fraction)
            override fun onRestore(view: SubsamplingScaleImageView2, fraction: Float) = callback.onRestore(this@SubsamplingViewHolder, view, fraction)
            override fun onRelease(view: SubsamplingScaleImageView2) = callback.onRelease(this@SubsamplingViewHolder, view)
        })
        requireVHCustomizer().initialize(ItemType.SUBSAMPLING, this@SubsamplingViewHolder)
    }

    override var data: Media? = null
    override var endView: View = subsamplingView

    fun bind(data: Media.SubsamplingImage) {
        this.data = data
//        subsamplingView.setTag(R.id.viewer_adapter_item_key, item.id())
//        subsamplingView.setTag(R.id.viewer_adapter_item_data, item)
//        subsamplingView.setTag(R.id.viewer_adapter_item_holder, this)

//        requireVHCustomizer().bind(ItemType.SUBSAMPLING, item, this)
//        requireImageLoader().load(subsamplingView, item, this)
    }

    override fun beforeTransitionStart(startView: View?) {
        subsamplingView.layoutParams = subsamplingView.layoutParams.apply {
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

    override fun beforeTransitionEnd(startView: View?) {}

    override fun transitionStart() {
        subsamplingView.layoutParams = subsamplingView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            if (this is ViewGroup.MarginLayoutParams) {
                marginStart = 0
                topMargin = 0
            }
        }
    }

    override fun transitionEnd(startView: View?) {
        subsamplingView.translationX = 0f
        subsamplingView.translationY = 0f
        subsamplingView.scaleX = 2f
        subsamplingView.scaleY = 2f
        // photoView.alpha = startView?.alpha ?: 0f
        fade() // https://github.com/davemorrissey/subsampling-scale-image-view/issues/313
        subsamplingView.layoutParams = subsamplingView.layoutParams.apply {
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
        subsamplingView.animate()
            .setDuration(Config.durationTransition)
            .alpha(0f)
            .start()
    }
}


