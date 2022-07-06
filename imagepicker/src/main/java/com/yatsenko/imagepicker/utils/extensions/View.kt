package com.yatsenko.imagepicker.utils.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yatsenko.imagepicker.core.Theme

internal fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics)
}

internal fun dpToPxInt(@Dimension(unit = Dimension.DP) dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics).toInt()
}

internal fun View.visible() {
    visibility = View.VISIBLE
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.gone() {
    visibility = View.GONE
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

internal fun FloatingActionButton.applyTheming() {
    this.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, Theme.themedColor(Theme.accentColor)))
    this.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, Theme.themedColor(Theme.accentDualColor)))
}

internal val Context.actionBarSize: Int
    get() = this.sizeByIdentifier("status_bar_height")

internal val Context.navigationBarSize: Int
    get() = this.sizeByIdentifier("navigation_bar_height")

private fun Context.sizeByIdentifier(name: String): Int {
    val resources = this.resources
    val resourceId: Int = resources.getIdentifier(name, "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

internal fun <T : RecyclerView.ViewHolder> RecyclerView.findViewHolderByAdapterPosition(position: Int): T? {
    return this.findViewHolderForAdapterPosition(position) as? T
}

internal fun <T : RecyclerView.ViewHolder> ViewPager2.findViewHolderByAdapterPosition(position: Int): T? {
    return (getChildAt(0) as? RecyclerView?)?.findViewHolderByAdapterPosition(position)
}

fun View.updateMargin(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) = updateLayoutParams<ViewGroup.MarginLayoutParams> { updateMargins(left, top, right, bottom) }


