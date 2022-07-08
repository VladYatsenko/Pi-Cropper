package com.yatsenko.picropper.ui.viewer.utils

import android.view.View
import android.view.ViewGroup
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.picropper.widgets.imageview.Overlay

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