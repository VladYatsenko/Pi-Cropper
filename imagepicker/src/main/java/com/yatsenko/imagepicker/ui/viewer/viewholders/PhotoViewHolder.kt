package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.picker.adapter.ImageViewHolder
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.core.Photo
import com.yatsenko.imagepicker.ui.viewer.widgets.PhotoView2

class PhotoViewHolder(
    val view: View,
    callback: ImageViewerAdapterListener
) : RecyclerView.ViewHolder(view) {
    
    companion object {

        fun create(parent: ViewGroup) = ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_imageviewer_photo, parent, false)
        )
        
    }
    
    val photoView = view.findViewById<PhotoView2>(R.id.photo_view).apply {
        setListener(object : PhotoView2.Listener {
            override fun onDrag(view: PhotoView2, fraction: Float) = callback.onDrag(this@PhotoViewHolder, view, fraction)
            override fun onRestore(view: PhotoView2, fraction: Float) = callback.onRestore(this@PhotoViewHolder, view, fraction)
            override fun onRelease(view: PhotoView2) = callback.onRelease(this@PhotoViewHolder, view)
        })
    }

    fun bind(item: Photo) {
        photoView.setTag(R.id.viewer_adapter_item_key, item.id())
        photoView.setTag(R.id.viewer_adapter_item_data, item)
        photoView.setTag(R.id.viewer_adapter_item_holder, this)
//        requireVHCustomizer().bind(ItemType.PHOTO, item, this)
//        requireImageLoader().load(photoView, item, this)
    }
}