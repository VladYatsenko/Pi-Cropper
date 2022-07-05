package com.yatsenko.imagepicker.utils.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.yatsenko.imagepicker.model.Media


fun ImageView.loadImage(image: Media?, onLoadingFinished: () -> Unit = {}) {
    val requestOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontAnimate()
    val listener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            onLoadingFinished()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            this@loadImage.setImageDrawable(resource)
            onLoadingFinished()
            return false
        }
    }
    Glide.with(this)
        .load(image?.mediaPath)
        .apply(requestOptions)
        .listener(listener)
        .thumbnail(0.2f)
        .dontTransform()
        .into(this)
}

internal fun PhotoView.resetScale(animate: Boolean): Boolean {
    val canReset = scale != minimumScale
    if (canReset)
        setScale(minimumScale, animate)
    return canReset
}