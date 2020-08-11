package com.yatsenko.imagepicker.viewer.viewer.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import com.yatsenko.imagepicker.viewer.common.extensions.resetScale
import com.yatsenko.imagepicker.viewer.common.pager.RecyclingPagerAdapter
import com.yatsenko.imagepicker.viewer.loader.ImageLoader

internal class ImagesPagerAdapter<T>(
    private val context: Context,
    _images: List<T>,
    private val imageLoader: ImageLoader<T>,
    private val isZoomingAllowed: Boolean
) : RecyclingPagerAdapter<ImagesPagerAdapter<T>.ViewHolder>() {

    private var images = _images
    private val holders = mutableListOf<ViewHolder>()

    fun isScaled(position: Int): Boolean =
        holders.firstOrNull { it.position == position }?.isScaled ?: false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val photoView = PhotoView(context).apply {
            isEnabled = isZoomingAllowed
            setOnViewDragListener { _, _ -> setAllowParentInterceptOnEdge(scale == 1.0f) }
        }

        return ViewHolder(photoView).also { holders.add(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = images.size

    internal fun updateImages(images: List<T>) {
        this.images = images
        notifyDataSetChanged()
    }

    internal fun resetScale(position: Int) =
        holders.firstOrNull { it.position == position }?.resetScale()

    internal inner class ViewHolder(itemView: View)
        : RecyclingPagerAdapter.ViewHolder(itemView) {

        internal var isScaled: Boolean = false
            get() = photoView.scale > 1f

        private val photoView: PhotoView = itemView as PhotoView

        fun bind(position: Int) {
            this.position = position
            imageLoader.loadImage(photoView, images[position])
        }

        fun resetScale() = photoView.resetScale(animate = true)
    }
}