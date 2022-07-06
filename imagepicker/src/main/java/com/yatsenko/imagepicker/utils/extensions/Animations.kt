package com.yatsenko.imagepicker.utils.extensions

import android.view.View
import android.view.animation.TranslateAnimation

internal object Animations {


    // slide the view from below itself to the current position
    fun View.slideUp(from: Float, to: Float, duration: Long = 350) {
        this.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            from,  // fromYDelta
            to
        ) // toYDelta
        animate.duration = duration
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    // slide the view from its current position to below itself
    fun View.slideDown(from: Float, to: Float, duration: Long = 350) {
        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            from,  // fromYDelta
            to
        ) // toYDelta
        animate.duration = duration
        animate.fillAfter = true
        this.startAnimation(animate)
    }

}