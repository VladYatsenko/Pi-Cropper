package com.yatsenko.imagepicker.ui.cropper.utils

import android.graphics.RectF
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

class TensionInterpolator {

    private val TENSION_FACTOR = 10f

    private var tensionZone = 0f
    private var tensionZonePull = 0f

    private var yTensionBounds: TensionBorder? = null
    private var xTensionBounds: TensionBorder? = null

    private val interpolator: Interpolator = DecelerateInterpolator(2f)

    private var downX = 0f
    private var downY = 0f

    fun onDown(x: Float, y: Float, draggedObj: RectF, tensionStartBorder: RectF) {
        downX = x
        downY = y
        tensionZone = Math.min(draggedObj.width(), draggedObj.height()) * 0.2f
        tensionZonePull = tensionZone * TENSION_FACTOR
        xTensionBounds = TensionBorder(
            draggedObj.right - tensionStartBorder.right,
            tensionStartBorder.left - draggedObj.left
        )
        yTensionBounds = TensionBorder(
            draggedObj.bottom - tensionStartBorder.bottom,
            tensionStartBorder.top - draggedObj.top
        )
    }

    fun interpolateX(x: Float): Float {
        return downX + interpolateDistance(x - downX, xTensionBounds)
    }

    fun interpolateY(y: Float): Float {
        return downY + interpolateDistance(y - downY, yTensionBounds)
    }

    private fun interpolateDistance(delta: Float, tensionBorder: TensionBorder?): Float {
        val distance = Math.abs(delta)
        val direction: Float = if (delta >= 0) 1f else (-1).toFloat()
        val tensionStart = if (direction == 1f) tensionBorder!!.positiveTensionStart else tensionBorder!!.negativeTensionStart
        if (distance < tensionStart) {
            return delta
        }
        val tensionDiff = distance - tensionStart
        val tensionEnd = tensionStart + tensionZone
        if (distance >= tensionZonePull + tensionStart) {
            return tensionEnd * direction
        }
        val realProgress = tensionDiff / tensionZonePull
        val progress = interpolator.getInterpolation(realProgress)
        return (tensionStart + progress * tensionZone) * direction
    }

    private class TensionBorder internal constructor(negativeTensionStart: Float, positiveTensionStart: Float) {
        val negativeTensionStart: Float = Math.max(negativeTensionStart, 0f)
        val positiveTensionStart: Float = Math.max(positiveTensionStart, 0f)

        override fun toString(): String {
            return "TensionBorder{" +
                    "negativeTensionStart=" + negativeTensionStart +
                    ", positiveTensionStart=" + positiveTensionStart +
                    '}'
        }

    }

}