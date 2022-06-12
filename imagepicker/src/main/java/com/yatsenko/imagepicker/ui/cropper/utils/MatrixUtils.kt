package com.yatsenko.imagepicker.ui.cropper.utils

import android.graphics.Matrix
import android.graphics.RectF

class MatrixUtils {

    companion object {
        fun findTransformToAllowedBounds(
            initial: RectF, initialTransform: Matrix,
            allowedBounds: RectF
        ): Matrix {
            val initialBounds = RectF()
            initialBounds.set(initial)
            val transform = Matrix()
            transform.set(initialTransform)
            val current = RectF(initial)
            transform.mapRect(current)
            if (current.width() < allowedBounds.width()) {
                val scale = allowedBounds.width() / current.width()
                scale(initialBounds, scale, transform, current)
            }
            if (current.height() < allowedBounds.height()) {
                val scale = allowedBounds.height() / current.height()
                scale(initialBounds, scale, transform, current)
            }
            if (current.left > allowedBounds.left) {
                translate(initialBounds, allowedBounds.left - current.left, 0f, transform, current)
            }
            if (current.right < allowedBounds.right) {
                translate(initialBounds, allowedBounds.right - current.right, 0f, transform, current)
            }
            if (current.top > allowedBounds.top) {
                translate(initialBounds, 0f, allowedBounds.top - current.top, transform, current)
            }
            if (current.bottom < allowedBounds.bottom) {
                translate(initialBounds, 0f, allowedBounds.bottom - current.bottom, transform, current)
            }
            return transform
        }

        fun scale(initial: RectF, scale: Float, transform: Matrix, outRect: RectF) {
            transform.postScale(scale, scale, outRect.centerX(), outRect.centerY())
            transformInitial(initial, transform, outRect)
        }

        fun translate(initial: RectF, dx: Float, dy: Float, transform: Matrix, outRect: RectF) {
            transform.postTranslate(dx, dy)
            transformInitial(initial, transform, outRect)
        }

        fun transformInitial(initial: RectF, transform: Matrix, outRect: RectF) {
            outRect.set(initial)
            transform.mapRect(outRect)
        }
    }

    private val outValues = FloatArray(9)

    fun getScaleX(mat: Matrix): Float {
        mat.getValues(outValues)
        return outValues[Matrix.MSCALE_X]
    }

    fun getXTranslation(mat: Matrix): Float {
        mat.getValues(outValues)
        return outValues[Matrix.MTRANS_X]
    }

    fun getYTranslation(mat: Matrix): Float {
        mat.getValues(outValues)
        return outValues[Matrix.MTRANS_Y]
    }
}