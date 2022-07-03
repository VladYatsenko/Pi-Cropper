package com.yatsenko.imagepicker.utils.transition

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

interface TransitionView {

    val viewGroup: ViewGroup

}

interface TransitionStart: TransitionView {

    fun beforeTransitionStart(startView: View?)

    fun transitionStart()

    fun afterTransitionStart() {}
}

interface TransitionEnd: TransitionView {

    fun beforeTransitionEnd(startView: View?)

    fun transitionEnd(startView: View?)

    fun fade(startView: View? = null)

}

interface TransitionHelper {

    val isMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    fun put(mediaId: String, imageView: ImageView)

    fun provide(mediaId: String): ImageView?

}