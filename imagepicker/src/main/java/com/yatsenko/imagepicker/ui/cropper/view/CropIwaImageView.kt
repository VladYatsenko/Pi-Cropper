package com.yatsenko.imagepicker.ui.cropper.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.annotation.FloatRange
import com.yatsenko.imagepicker.ui.cropper.config.ConfigChangeListener
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaImageViewConfig
import com.yatsenko.imagepicker.ui.cropper.config.InitialPosition
import com.yatsenko.imagepicker.ui.cropper.listeners.OnImagePositionedListener
import com.yatsenko.imagepicker.ui.cropper.listeners.OnNewBoundsListener
import com.yatsenko.imagepicker.ui.cropper.utils.*

@SuppressLint("AppCompatCustomView")
class CropIwaImageView(
    context: Context,
    private val config: CropIwaImageViewConfig
) : ImageView(context), OnNewBoundsListener, ConfigChangeListener {

    private val imgMatrix = Matrix()
    private val matrixUtils = MatrixUtils()
    val gestureDetector = GestureProcessor()

    private var allowedBounds = RectF()
    private var imageBounds = RectF()
    private var realImageBounds = RectF()

    private var imagePositionedListener: OnImagePositionedListener? = null

    init {
        config.addConfigChangeListener(this)
        scaleType = ScaleType.MATRIX
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (hasImageSize()) {
            placeImageToInitialPosition()
        }
    }

    private fun placeImageToInitialPosition() {
        updateImageBounds()
        moveImageToTheCenter()
        if (config.scale == CropIwaImageViewConfig.SCALE_UNSPECIFIED) {
            when (config.initialPosition) {
                InitialPosition.CENTER_CROP -> resizeImageToFillTheView()
                InitialPosition.CENTER_INSIDE -> resizeImageToBeInsideTheView()
            }
            config.setScale(getCurrentScalePercent()).apply()
        } else {
            setScalePercent(config.scale)
        }
        notifyImagePositioned()
    }

    private fun resizeImageToFillTheView() {
        val scale: Float = if (width < height) {
            height.toFloat() / getImageHeight()
        } else {
            width.toFloat() / getImageWidth()
        }
        scaleImage(scale)
    }

    private fun resizeImageToBeInsideTheView() {
        val scale: Float = if (getImageWidth() < getImageHeight()) {
            height.toFloat() / getImageHeight()
        } else {
            width.toFloat() / getImageWidth()
        }
        scaleImage(scale)
    }

    private fun moveImageToTheCenter() {
        updateImageBounds()
        val deltaX = width / 2f - imageBounds.centerX()
        val deltaY = height / 2f - imageBounds.centerY()
        translateImage(deltaX, deltaY)
    }

    private fun calculateMinScale(): Float {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (getRealImageWidth() <= viewWidth && getRealImageHeight() <= viewHeight) {
            return config.minScale
        }
        val scaleFactor = if (viewWidth < viewHeight) viewWidth / getRealImageWidth() else viewHeight / getRealImageHeight()
        return scaleFactor * 0.8f
    }

    private fun getRealImageWidth(): Int {
        val image = drawable
        return image?.intrinsicWidth ?: -1
    }

    private fun getRealImageHeight(): Int {
        val image = drawable
        return image?.intrinsicHeight ?: -1
    }

    fun getImageWidth(): Int {
        return imageBounds.width().toInt()
    }

    fun getImageHeight(): Int {
        return imageBounds.height().toInt()
    }

    fun hasImageSize(): Boolean {
        return getRealImageWidth() != -1 && getRealImageHeight() != -1
    }

    override fun onNewBounds(bounds: RectF) {
        updateImageBounds()
        allowedBounds.set(bounds)
        if (hasImageSize()) {
            post { animateToAllowedBounds() }
            updateImageBounds()
            invalidate()
        }
    }

    private fun animateToAllowedBounds() {
        updateImageBounds()
        val endMatrix = MatrixUtils.findTransformToAllowedBounds(
            realImageBounds, imgMatrix,
            allowedBounds
        )
        val animator = MatrixAnimator()
        animator.animate(imgMatrix, endMatrix) { animation ->
            imgMatrix.set(animation.animatedValue as Matrix)
            setImageMatrix(imgMatrix)
            updateImageBounds()
            invalidate()
        }
    }

    private fun setScalePercent(@FloatRange(from = 0.01, to = 1.0) percent: Float) {
        var percent = percent
        percent = Math.min(Math.max(0.01f, percent), 1f)
        val desiredScale: Float = config.minScale + config.maxScale * percent
        val currentScale = matrixUtils.getScaleX(imgMatrix)
        val factor = desiredScale / currentScale
        scaleImage(factor)
        invalidate()
    }

    private fun scaleImage(factor: Float) {
        updateImageBounds()
        scaleImage(factor, imageBounds.centerX(), imageBounds.centerY())
    }

    private fun scaleImage(factor: Float, pivotX: Float, pivotY: Float) {
        imgMatrix.postScale(factor, factor, pivotX, pivotY)
        setImageMatrix(imgMatrix)
        updateImageBounds()
    }

    private fun translateImage(deltaX: Float, deltaY: Float) {
        imgMatrix.postTranslate(deltaX, deltaY)
        setImageMatrix(imgMatrix)
        if (deltaX > 0.01f || deltaY > 0.01f) {
            updateImageBounds()
        }
    }

    private fun updateImageBounds() {
        realImageBounds[0f, 0f, getRealImageWidth().toFloat()] = getRealImageHeight().toFloat()
        imageBounds.set(realImageBounds)
        imgMatrix.mapRect(imageBounds)
    }

    override fun onConfigChanged() {
        if (Math.abs(getCurrentScalePercent() - config.scale) > 0.001f) {
            setScalePercent(config.scale)
            animateToAllowedBounds()
        }
    }

    fun setImagePositionedListener(imagePositionedListener: OnImagePositionedListener?) {
        this.imagePositionedListener = imagePositionedListener
        if (hasImageSize()) {
            updateImageBounds()
            notifyImagePositioned()
        }
    }

    fun getImageRect(): RectF {
        updateImageBounds()
        return RectF(imageBounds)
    }

    fun notifyImagePositioned() {
        if (imagePositionedListener != null) {
            val imageRect = RectF(imageBounds)
            constrainRectTo(0, 0, width, height, imageRect)
            imagePositionedListener!!.onImagePositioned(imageRect)
        }
    }

    private fun getCurrentScalePercent(): Float {
        return boundValue(
            0.01f + (matrixUtils.getScaleX(imgMatrix) - config.minScale) / config.maxScale,
            0.01f, 1f
        )
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale: Float = matrixUtils.getScaleX(imgMatrix) * scaleFactor
            if (isValidScale(newScale)) {
                scaleImage(scaleFactor, detector.focusX, detector.focusY)
                config.setScale(getCurrentScalePercent()).apply()
            }
            return true
        }

        private fun isValidScale(newScale: Float): Boolean {
            return (newScale >= config.minScale && newScale <= config.minScale + config.maxScale)
        }
    }

    private inner class TranslationGestureListener {
        private var prevX = 0f
        private var prevY = 0f
        private var id = 0
        private val interpolator = TensionInterpolator()
        fun onDown(e: MotionEvent) {
            onDown(e.x, e.y, e.getPointerId(0))
        }

        private fun onDown(x: Float, y: Float, id: Int) {
            updateImageBounds()
            interpolator.onDown(x, y, imageBounds, allowedBounds)
            saveCoordinates(x, y, id)
        }

        fun onTouchEvent(e: MotionEvent, canHandle: Boolean) {
            when (e.actionMasked) {
                MotionEvent.ACTION_POINTER_UP -> {
                    onPointerUp(e)
                    return
                }
                MotionEvent.ACTION_MOVE -> {}
                else -> return
            }
            val index = e.findPointerIndex(id)
            updateImageBounds()
            val currentX = interpolator.interpolateX(e.getX(index))
            val currentY = interpolator.interpolateY(e.getY(index))
            if (canHandle) {
                translateImage(currentX - prevX, currentY - prevY)
            }
            saveCoordinates(currentX, currentY)
        }

        private fun onPointerUp(e: MotionEvent) {
            //If user lifted finger that we used to calculate translation, we need to find a new one
            if (e.getPointerId(e.actionIndex) == id) {
                var index = 0
                while (index < e.pointerCount && index == e.actionIndex) {
                    index++
                }
                onDown(e.getX(index), e.getY(index), e.getPointerId(index))
            }
        }

        private fun saveCoordinates(x: Float, y: Float, id: Int = this.id) {
            prevX = x
            prevY = y
            this.id = id
        }
    }

    inner class GestureProcessor {
        private val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, ScaleGestureListener())
        private val translationGestureListener: TranslationGestureListener = TranslationGestureListener()

        fun onDown(event: MotionEvent) {
            translationGestureListener.onDown(event)
        }

        fun onTouchEvent(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> return
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    animateToAllowedBounds()
                    return
                }
            }
            if (config.isScaleEnabled) {
                scaleDetector.onTouchEvent(event)
            }
            if (config.isTranslationEnabled) {
                //We don't want image translation while scaling gesture is in progress
                //so - canHandle if scaleDetector.isNotInProgress
                translationGestureListener.onTouchEvent(event, !scaleDetector.isInProgress)
            }
        }

    }

}