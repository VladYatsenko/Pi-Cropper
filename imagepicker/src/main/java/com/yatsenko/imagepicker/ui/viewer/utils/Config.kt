package com.yatsenko.imagepicker.ui.viewer.utils

import android.graphics.Color
import androidx.viewpager2.widget.ViewPager2
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

object Config {
    var isDebug: Boolean = true
    var offscreenPageLimit: Int = 3
    var VIEWER_ORIENTATION: Int = ViewPager2.ORIENTATION_HORIZONTAL
    var viewBackgroundColor: Int = Color.BLACK
    var durationTransition: Long = 250L
    var durationBg: Long = 150L
    var subsamplingScaleType = SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
    var isSwipeToDismiss: Boolean = true
    var swipeTouchSlop = 4f
    var dismissFraction: Float = 0.12f
    var transitionOffsetY = 0
}