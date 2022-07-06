package com.yatsenko.imagepicker.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.yatsenko.imagepicker.utils.transition.TransitionEndHelper
import com.yatsenko.imagepicker.utils.transition.TransitionStartHelper

internal class InterceptLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    override fun onInterceptTouchEvent(ev: MotionEvent?) = TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating
}