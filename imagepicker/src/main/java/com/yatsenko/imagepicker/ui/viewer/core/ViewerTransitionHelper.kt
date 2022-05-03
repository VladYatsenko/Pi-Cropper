package com.yatsenko.imagepicker.ui.viewer.core

import android.os.Looper
import android.view.View
import android.widget.ImageView

object ViewerTransitionHelper {

    private val transition = HashMap<ImageView, String>()

    fun put(mediaId: String, imageView: ImageView) {
        require(isMainThread())
        if (!imageView.isAttachedToWindow) return
        imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View?) = Unit
            override fun onViewDetachedFromWindow(p0: View?) {
                transition.remove(imageView)
                imageView.removeOnAttachStateChangeListener(this)
            }
        })
        transition[imageView] = mediaId
    }

    fun provide(mediaId: String): ImageView? {
        return transition.keys.firstOrNull { transition[it] == mediaId }
    }

    private fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

}

//class SimpleTransformer : Transformer {
//    override fun getView(mediaId: String) = ViewerTransitionHelper.provide(mediaId)
//}


//interface Transformer {
//    fun getView(mediaId: String): ImageView? = null
//}


