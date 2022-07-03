package com.yatsenko.imagepicker.utils.transition

import android.view.View
import android.view.ViewGroup

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