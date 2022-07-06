package com.yatsenko.imagepicker.ui.viewer.utils

import android.view.View
import android.view.ViewGroup
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.imagepicker.widgets.imageview.Overlay

internal class OverlayHelper (private val single: Boolean): VHCustomizer, OverlayCustomizer, ViewerCallback {

    private var overlayView: Overlay? = null
    var adapterResult: (AdapterResult) -> Unit = {}
    private val internalAdapterResult: (AdapterResult) -> Unit = { adapterResult(it) }

    override fun provideView(parent: ViewGroup): View {
        return Overlay.create(parent.context).apply {
            result = internalAdapterResult
            this@OverlayHelper.overlayView = this
            show()
        }
    }

    override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {
        overlayView?.hide()
    }

    fun submitData(media: Media?) {
        overlayView?.data = Overlay.Data.createFrom(media, single)
    }

}