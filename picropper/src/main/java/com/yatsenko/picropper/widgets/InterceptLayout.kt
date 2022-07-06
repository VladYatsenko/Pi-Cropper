package com.yatsenko.picropper.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.yatsenko.picropper.utils.transition.TransitionEndHelper
import com.yatsenko.picropper.utils.transition.TransitionStartHelper

internal class InterceptLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    override fun onInterceptTouchEvent(ev: MotionEvent?) = TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating
}