package com.yatsenko.imagepicker.ui.cropper.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.yatsenko.imagepicker.ui.cropper.AspectRatio
import com.yatsenko.imagepicker.ui.cropper.config.ConfigChangeListener
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaOverlayConfig
import com.yatsenko.imagepicker.ui.cropper.listeners.OnImagePositionedListener
import com.yatsenko.imagepicker.ui.cropper.listeners.OnNewBoundsListener
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaShape
import kotlin.math.roundToInt

open class CropIwaOverlayView constructor(
    context: Context,
    private val config: CropIwaOverlayConfig
) : View(context, null), ConfigChangeListener, OnImagePositionedListener {

    var newBoundsListener: OnNewBoundsListener? = null

    private val cropShape: CropIwaShape
        get() = config.getCropShape()
    private val cropScale: Float
        get() = config.getCropScale()

    private val overlayPaint = Paint().apply {
        style = Paint.Style.FILL
        color = config.getOverlayColor()
    }

    private val imageBounds = RectF()
    protected var cropRect = RectF()

    var shouldDrawOverlay: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val aspectRatio: AspectRatio?
        get() {
            var aspectRatio: AspectRatio = config.getAspectRatio()
            if (aspectRatio === AspectRatio.IMG_SRC) {
                if (imageBounds.width() == 0f || imageBounds.height() == 0f) {
                    return null
                }
                aspectRatio = AspectRatio.AspectOriginal(
                    imageBounds.width().roundToInt(),
                    imageBounds.height().roundToInt()
                )
            }
            return aspectRatio
        }

    private val isValidCrop: Boolean
        get() = cropRect.width() >= config.getMinWidth() && cropRect.height() >= config.getMinHeight()

    init {
        config.addConfigChangeListener(this)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onConfigChanged() {
        overlayPaint.color = config.getOverlayColor()
        cropShape.onConfigChanged()
        setCropRectAccordingToAspectRatio()
        notifyNewBounds()
        invalidate()
    }

    protected fun notifyNewBounds() {
        //Do not allow client code to modify our cropRect!
        newBoundsListener?.onNewBounds(RectF(cropRect))
    }

    override fun onImagePositioned(imageRect: RectF) {
        imageBounds.set(imageRect)
        setCropRectAccordingToAspectRatio()
        notifyNewBounds()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //We will get here measured dimensions of an ImageView
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (shouldDrawOverlay) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
            if (isValidCrop)
                cropShape.draw(canvas, cropRect)
        }
    }

    open fun isResizing(): Boolean {
        return false
    }

    open fun isDraggingCropArea(): Boolean {
        return false
    }

    fun getCropRec(): RectF {
        return RectF(cropRect)
    }

    fun isDrawn(): Boolean {
        return shouldDrawOverlay
    }


    private fun setCropRectAccordingToAspectRatio() {
        val viewWidth = measuredWidth.toFloat()
        val viewHeight = measuredHeight.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) {
            return
        }
        val aspectRatio: AspectRatio = this.aspectRatio ?: return
        if (cropRect.width() != 0f && cropRect.height() != 0f) {
            val currentRatio = cropRect.width() / cropRect.height()
            if (Math.abs(currentRatio - aspectRatio.ratio) < 0.001) {
                return
            }
        }
        val centerX = viewWidth * 0.5f
        val centerY = viewHeight * 0.5f
        val halfWidth: Float
        val halfHeight: Float
        val calculateFromWidth = (aspectRatio.height < aspectRatio.width
                || aspectRatio.isSquare && viewWidth < viewHeight)
        if (calculateFromWidth) {
            halfWidth = viewWidth * cropScale * 0.5f
            halfHeight = halfWidth / aspectRatio.ratio
        } else {
            halfHeight = viewHeight * cropScale * 0.5f
            halfWidth = halfHeight * aspectRatio.ratio
        }
        cropRect[centerX - halfWidth, centerY - halfHeight, centerX + halfWidth] = centerY + halfHeight
    }
}