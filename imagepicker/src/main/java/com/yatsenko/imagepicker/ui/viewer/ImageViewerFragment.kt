package com.yatsenko.imagepicker.ui.viewer

import android.app.Activity
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.widgets.Overlay

class ImageViewer(
    private val activity: Activity,
    private val single: Boolean,
    private val transitionImage: (Int) -> ImageView?,
    private val result: (AdapterResult) -> Unit,
) {

    private var viewer: StfalconImageViewer<Image>? = null
    private var overlayView: Overlay? = null
    var position: Int = -1
        private set

    fun open(images: List<Image>, position: Int, ) {
        overlayView = Overlay(activity).apply {
            data = Overlay.Data(images[position], single)
            result = ::handleResult
        }
        this@ImageViewer.position = position
        viewer = StfalconImageViewer.Builder(activity, images, ::loadImage)
            .withHiddenStatusBar(false)
            .withStartPosition(position)
            .withOverlayView(overlayView)
            .withTransitionFrom(transitionImage(position))
            .withImageChangeListener { viewerPosition ->
                this@ImageViewer.position = viewerPosition
                refreshOverlay(images)
                refreshTransitionImage(transitionImage(position))
            }
            .show()
    }

    fun refreshOverlay(images: List<Image>) {
        overlayView?.data = Overlay.Data(images[position], single)
    }

    fun refreshTransitionImage(imageView: ImageView?) {
        viewer?.updateTransitionImage(imageView)
    }

    private fun handleResult(result: AdapterResult) {
        when (result) {
            AdapterResult.GoBack -> viewer?.close()
            is AdapterResult.OnSelectImageClicked -> result(result)
        }
    }

    private fun loadImage(imageView: ImageView, image: Image?) {
        Glide.with(imageView.context)
            .load(image?.imagePath)
            .into(imageView)
    }

}