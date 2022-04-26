package com.yatsenko.imagepicker.viewer

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.widgets.Overlay

class ImageViewer(
    private val activity: Activity,
    private val single: Boolean,
    private val result: (AdapterResult) -> Unit,
) {

    private lateinit var viewer: StfalconImageViewer<Image>
    private val overlayView: Overlay
        get() = Overlay(activity).apply { result = ::handleResult }

    fun open(images: List<Image>, position: Int, transitionImage: (Int) -> ImageView?) {
        viewer = StfalconImageViewer.Builder(activity, images, ::loadImage)
            .withHiddenStatusBar(false)
            .withStartPosition(position)
            .withOverlayView(overlayView)
            .withTransitionFrom(transitionImage(position))
            .withImageChangeListener { viewerPosition ->
                overlayView.data = Overlay.Data(images[viewerPosition], single)
                viewer.updateTransitionImage(transitionImage(viewerPosition))
            }
            .show()
    }

    private fun handleResult(result: AdapterResult) {
        when (result) {
            AdapterResult.GoBack -> viewer.close()
            is AdapterResult.OnSelectImageClicked -> result(result)
        }
    }

    private fun loadImage(imageView: ImageView, image: Image?) {
        Glide.with(imageView.context)
            .load(image?.imagePath)
            .into(imageView)
    }

}