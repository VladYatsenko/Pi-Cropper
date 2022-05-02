package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.ui.viewer.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.adapter.ItemType
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireImageLoader
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireVHCustomizer
import com.yatsenko.imagepicker.ui.viewer.core.Photo
import com.github.iielse.imageviewer.databinding.ItemImageviewerSubsamplingBinding
import com.yatsenko.imagepicker.ui.viewer.utils.Config
import com.yatsenko.imagepicker.ui.viewer.widgets.SubsamplingScaleImageView2
import com.yatsenko.imagepicker.R

class SubsamplingViewHolder(
    parent: ViewGroup,
    callback: ImageViewerAdapterListener,
    val binding: ItemImageviewerSubsamplingBinding =
        ItemImageviewerSubsamplingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.subsamplingView.setMinimumScaleType(Config.subsamplingScaleType)
        binding.subsamplingView.setListener(object : SubsamplingScaleImageView2.Listener {
            override fun onDrag(view: SubsamplingScaleImageView2, fraction: Float) = callback.onDrag(this@SubsamplingViewHolder, view, fraction)
            override fun onRestore(view: SubsamplingScaleImageView2, fraction: Float) = callback.onRestore(this@SubsamplingViewHolder, view, fraction)
            override fun onRelease(view: SubsamplingScaleImageView2) = callback.onRelease(this@SubsamplingViewHolder, view)
        })
        requireVHCustomizer().initialize(ItemType.SUBSAMPLING, this)
    }

    fun bind(item: Photo) {
        binding.subsamplingView.setTag(R.id.viewer_adapter_item_key, item.id())
        binding.subsamplingView.setTag(R.id.viewer_adapter_item_data, item)
        binding.subsamplingView.setTag(R.id.viewer_adapter_item_holder, this)
        requireVHCustomizer().bind(ItemType.SUBSAMPLING, item, this)
        requireImageLoader().load(binding.subsamplingView, item, this)
    }
}


