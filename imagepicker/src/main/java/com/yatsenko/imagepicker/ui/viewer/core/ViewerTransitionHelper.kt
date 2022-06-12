package com.yatsenko.imagepicker.ui.viewer.core

import android.os.Looper
import android.view.View
import android.widget.ImageView

object ViewerTransitionHelper {

    private val _transition = HashMap<ImageView, String>()

    val transition: Map<ImageView, String>
        get() = _transition

    fun put(mediaId: String, imageView: ImageView) {
        require(isMainThread())
        if (!imageView.isAttachedToWindow) return
        imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View?) = Unit
            override fun onViewDetachedFromWindow(p0: View?) {
                _transition.remove(imageView)
                imageView.removeOnAttachStateChangeListener(this)
            }
        })
        _transition[imageView] = mediaId
    }

    fun provide(mediaId: String): ImageView? {
        return _transition.keys.firstOrNull { _transition[it] == mediaId }
    }

    private fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

}


