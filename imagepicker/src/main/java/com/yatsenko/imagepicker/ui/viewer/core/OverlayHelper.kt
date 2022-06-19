package com.yatsenko.imagepicker.ui.viewer.core

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.imagepicker.ui.viewer.viewholders.ImageViewHolder
import com.yatsenko.imagepicker.widgets.imageview.Overlay

class OverlayHelper : VHCustomizer, OverlayCustomizer, ViewerCallback {

    private var overlayView: Overlay? = null
    var adapterResult: (AdapterResult) -> Unit = {}
    private val internalAdapterResult: (AdapterResult) -> Unit = { adapterResult(it) }
    private var currentPosition = -1

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

    fun submitData(position: Int, list: List<Media>) {
        currentPosition = position
        list.getOrNull(position)?.let {
            overlayView?.data = Overlay.Data.createFrom(it)
        }
    }

}