package com.yatsenko.imagepicker.utils.transition

import android.view.View
import android.widget.ImageView

object CropperTransitionHelper: TransitionHelper {

    private val _transition = HashMap<ImageView, String>()

    override fun put(mediaId: String, imageView: ImageView) {
        require(isMainThread)
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

    override fun provide(mediaId: String): ImageView? {
        return _transition.keys.firstOrNull { _transition[it] == mediaId }
    }

}