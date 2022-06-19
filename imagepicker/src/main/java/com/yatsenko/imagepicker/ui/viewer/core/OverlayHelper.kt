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

    override fun onPageSelected(position: Int, viewHolder: RecyclerView.ViewHolder) {
        val fullscreenViewHolder = (viewHolder as? FullscreenViewHolder) ?: return
        overlayView?.data = Overlay.Data.createFrom(fullscreenViewHolder.data)
    }

    override fun bind(type: Int, data: Media, viewHolder: RecyclerView.ViewHolder) {
        when (type) {
            ImageViewHolder.ITEM_TYPE -> overlayView?.data = Overlay.Data.createFrom(data)
        }
    }

}