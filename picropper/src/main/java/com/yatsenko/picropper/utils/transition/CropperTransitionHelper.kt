package com.yatsenko.picropper.utils.transition

import android.view.View
import android.widget.ImageView

object CropperTransitionHelper: TransitionHelper {

    private var transition: ImageView? = null

    override fun put(mediaId: String, imageView: ImageView) {
        require(isMainThread)
        if (!imageView.isAttachedToWindow) return
        imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View?) = Unit
            override fun onViewDetachedFromWindow(p0: View?) {
                transition = null
                imageView.removeOnAttachStateChangeListener(this)
            }
        })
        transition = imageView
    }

    override fun provide(mediaId: String): ImageView? {
        return transition
    }

}