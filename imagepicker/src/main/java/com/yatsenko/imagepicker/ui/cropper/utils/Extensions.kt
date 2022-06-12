package com.yatsenko.imagepicker.ui.cropper.utils

import android.graphics.RectF
import java.io.Closeable
import java.io.File
import java.lang.Exception
import java.lang.NullPointerException

fun Closeable?.closeSilently() {
    try {
        this?.close()
    } catch (e: Exception) { /* NOP */
    }
}

fun delete(file: File?) {
    file?.delete()
}

fun enlargeRectBy(value: Float, outRect: RectF): RectF {
    outRect.top -= value
    outRect.bottom += value
    outRect.left -= value
    outRect.right += value
    return outRect
}

fun boundValue(value: Float, lowBound: Float, highBound: Float): Float {
    return Math.max(Math.min(value, highBound), lowBound)
}

fun moveRectBounded(
    initial: RectF, deltaX: Float, deltaY: Float,
    horizontalBound: Int, verticalBound: Int,
    outRect: RectF
): RectF {
    val newLeft = boundValue(initial.left + deltaX, 0f, horizontalBound - initial.width())
    val newRight = newLeft + initial.width()
    val newTop = boundValue(initial.top + deltaY, 0f, verticalBound - initial.height())
    val newBottom = newTop + initial.height()
    outRect[newLeft, newTop, newRight] = newBottom
    return outRect
}

fun constrainRectTo(minLeft: Int, minTop: Int, maxRight: Int, maxBottom: Int, rect: RectF) {
    rect[Math.max(rect.left, minLeft.toFloat()),
            Math.max(rect.top, minTop.toFloat()),
            Math.min(rect.right, maxRight.toFloat())] = Math.min(rect.bottom, maxBottom.toFloat())
}