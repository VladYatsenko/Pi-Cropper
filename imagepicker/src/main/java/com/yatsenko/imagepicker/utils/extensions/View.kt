package com.yatsenko.imagepicker.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media

fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics)
}

fun TextView.checkboxPosition(image: Media?, stroke: Boolean = false) {
    //todo config
//    isGone = single
    text = image?.indexString
    val selected= if (stroke) R.drawable.circle_selected_stroke else R.drawable.circle_selected
    val circle = if (image?.isSelected == true) selected else R.drawable.circle
    background = ContextCompat.getDrawable(context, circle)
}

internal val View?.localVisibleRect: Rect
    get() = Rect().also { this?.getLocalVisibleRect(it) }

internal val View?.globalVisibleRect: Rect
    get() = Rect().also { this?.getGlobalVisibleRect(it) }

internal val View?.hitRect: Rect
    get() = Rect().also { this?.getHitRect(it) }

internal val View?.isRectVisible: Boolean
    get() = this != null && globalVisibleRect != localVisibleRect

internal val View?.isVisible: Boolean
    get() = this != null && visibility == View.VISIBLE

internal fun View.visible() {
    visibility = View.VISIBLE
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.gone() {
    visibility = View.GONE
}

internal inline fun <T : View> T.postApply(crossinline block: T.() -> Unit) {
    post { apply(block) }
}

internal inline fun <T : View> T.postDelayed(delayMillis: Long, crossinline block: T.() -> Unit) {
    postDelayed({ block() }, delayMillis)
}

internal fun View.applyMargin(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = start ?: marginStart
            topMargin = top ?: topMargin
            marginEnd = end ?: marginEnd
            bottomMargin = bottom ?: bottomMargin
        }
    }
}

internal fun View.requestNewSize(
    width: Int, height: Int) {
    layoutParams.width = width
    layoutParams.height = height
    layoutParams = layoutParams
}

internal fun View.makeViewMatchParent() {
    applyMargin(0, 0, 0, 0)
    requestNewSize(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT)
}

internal fun View.animateAlpha(from: Float?, to: Float?, duration: Long) {
    alpha = from ?: 0f
    clearAnimation()
    animate()
        .alpha(to ?: 0f)
        .setDuration(duration)
        .start()
}

internal fun View.switchVisibilityWithAnimation() {
    val isVisible = visibility == View.VISIBLE
    val from = if (isVisible) 1.0f else 0.0f
    val to = if (isVisible) 0.0f else 1.0f

    ObjectAnimator.ofFloat(this, "alpha", from, to).apply {
        duration = ViewConfiguration.getDoubleTapTimeout().toLong()

        if (isVisible) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    gone()
                }
            })
        } else {
            visible()
        }

        start()
    }
}

val Context.actionBarSize: Int
    get() = this.sizeByIdentifier("status_bar_height")

val Context.navigationBarSize: Int
    get() = this.sizeByIdentifier("navigation_bar_height")

private fun Context.sizeByIdentifier(name: String): Int {
    val resources = this.resources
    val resourceId: Int = resources.getIdentifier(name, "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}


