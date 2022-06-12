package com.yatsenko.imagepicker.ui.cropper.utils

import android.animation.FloatEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.view.animation.AccelerateDecelerateInterpolator
import java.lang.ref.WeakReference

class MatrixAnimator {

    fun animate(initial: Matrix?, target: Matrix?, listener: ValueAnimator.AnimatorUpdateListener) {
        val animator = ValueAnimator.ofObject(MatrixEvaluator(), initial, target)
        animator.addUpdateListener(SafeListener(listener))
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 200
        animator.start()
    }


    private class MatrixEvaluator : TypeEvaluator<Matrix> {
        private val current: Matrix = Matrix()
        private var lastStart: Matrix? = null
        private var lastEnd: Matrix? = null
        private val floatEvaluator: FloatEvaluator = FloatEvaluator()
        private var initialTranslationX = 0f
        private var initialTranslationY = 0f
        private var initialScale = 0f
        private var endTranslationX = 0f
        private var endTranslationY = 0f
        private var endScale = 0f
        override fun evaluate(fraction: Float, startValue: Matrix, endValue: Matrix): Matrix {
            if (shouldReinitialize(startValue, endValue)) {
                collectValues(startValue, endValue)
            }
            val translationX = floatEvaluator.evaluate(fraction, initialTranslationX, endTranslationX)
            val translationY = floatEvaluator.evaluate(fraction, initialTranslationY, endTranslationY)
            val scale = floatEvaluator.evaluate(fraction, initialScale, endScale)
            current.reset()
            current.postScale(scale, scale)
            current.postTranslate(translationX, translationY)
            return current
        }

        private fun shouldReinitialize(start: Matrix, end: Matrix): Boolean {
            return lastStart !== start || lastEnd !== end
        }

        private fun collectValues(start: Matrix, end: Matrix) {
            val matrixUtils = MatrixUtils()
            initialTranslationX = matrixUtils.getXTranslation(start)
            initialTranslationY = matrixUtils.getYTranslation(start)
            initialScale = matrixUtils.getScaleX(start)
            endTranslationX = matrixUtils.getXTranslation(end)
            endTranslationY = matrixUtils.getYTranslation(end)
            endScale = matrixUtils.getScaleX(end)
            lastStart = start
            lastEnd = end
        }

    }

    private class SafeListener (wrapped: ValueAnimator.AnimatorUpdateListener) : ValueAnimator.AnimatorUpdateListener {
        private val wrapped: WeakReference<ValueAnimator.AnimatorUpdateListener> = WeakReference(wrapped)
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val listener = wrapped.get()
            listener?.onAnimationUpdate(animation)
        }

    }

}