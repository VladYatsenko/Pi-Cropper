package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener
import com.github.iielse.imageviewer.R
import com.yatsenko.imagepicker.ui.viewer.adapter.ItemType
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireImageLoader
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireVHCustomizer
import com.yatsenko.imagepicker.ui.viewer.core.Photo
import com.github.iielse.imageviewer.databinding.ItemImageviewerVideoBinding
import com.yatsenko.imagepicker.ui.viewer.widgets.video.ExoVideoView2

class VideoViewHolder(
    parent: ViewGroup,
    callback: ImageViewerAdapterListener,
    val binding: ItemImageviewerVideoBinding =
        ItemImageviewerVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.videoView.addListener(object : ExoVideoView2.Listener {
            override fun onDrag(view: ExoVideoView2, fraction: Float) = callback.onDrag(this@VideoViewHolder, view, fraction)
            override fun onRestore(view: ExoVideoView2, fraction: Float) = callback.onRestore(this@VideoViewHolder, view, fraction)
            override fun onRelease(view: ExoVideoView2) = callback.onRelease(this@VideoViewHolder, view)
        })
        requireVHCustomizer().initialize(ItemType.VIDEO, this)
    }

    fun bind(item: Photo) {
        binding.videoView.setTag(R.id.viewer_adapter_item_key, item.id())
        binding.videoView.setTag(R.id.viewer_adapter_item_data, item)
        binding.videoView.setTag(R.id.viewer_adapter_item_holder, this)
        requireVHCustomizer().bind(ItemType.VIDEO, item, this)
        requireImageLoader().load(binding.videoView, item, this)
    }
}