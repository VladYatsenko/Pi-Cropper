package com.yatsenko.imagepicker.ui.cropper.shape

import android.graphics.*
import com.yatsenko.imagepicker.ui.cropper.config.ConfigChangeListener
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaOverlayConfig

abstract class CropIwaShape (protected val overlayConfig: CropIwaOverlayConfig) : ConfigChangeListener {

    private val clearPaint= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }
    private val borderPaint = Paint(gridPaint)

    init {
        updatePaintObjectsFromConfig()
    }

    fun draw(canvas: Canvas?, cropBounds: RectF?) {
        clearArea(canvas, cropBounds, clearPaint)
        if (overlayConfig.shouldDrawGrid() && canvas != null && cropBounds != null) {
            drawGrid(canvas, cropBounds, gridPaint)
        }
        drawBorders(canvas, cropBounds, borderPaint)
    }

    open fun drawCorner(canvas: Canvas, x: Float, y: Float, deltaX: Float, deltaY: Float) {
        canvas.drawLine(x, y, x + deltaX, y, cornerPaint)
        canvas.drawLine(x, y, x, y + deltaY, cornerPaint)
    }

    open fun getCornerPaint(): Paint? {
        return cornerPaint
    }

    open fun getGridPaint(): Paint? {
        return gridPaint
    }

    open fun getBorderPaint(): Paint? {
        return borderPaint
    }

    abstract fun getMask(): CropIwaShapeMask?

    protected abstract fun clearArea(canvas: Canvas?, cropBounds: RectF?, clearPaint: Paint?)

    protected abstract fun drawBorders(canvas: Canvas?, cropBounds: RectF?, paint: Paint?)

    protected open fun drawGrid(canvas: Canvas, cropBounds: RectF, paint: Paint) {
        val stepX = cropBounds.width() * 0.333f
        val stepY = cropBounds.height() * 0.333f
        var x = cropBounds.left
        var y = cropBounds.top
        for (i in 0..1) {
            x += stepX
            y += stepY
            canvas.drawLine(x, cropBounds.top, x, cropBounds.bottom, paint)
            canvas.drawLine(cropBounds.left, y, cropBounds.right, y, paint)
        }
    }

    override fun onConfigChanged() {
        updatePaintObjectsFromConfig()
    }

    protected open fun updatePaintObjectsFromConfig() {
        cornerPaint.strokeWidth = overlayConfig.getCornerStrokeWidth().toFloat()
        cornerPaint.color = overlayConfig.getCornerColor()
        gridPaint.color = overlayConfig.getGridColor()
        gridPaint.strokeWidth = overlayConfig.getGridStrokeWidth().toFloat()
        borderPaint.color = overlayConfig.getBorderColor()
        borderPaint.strokeWidth = overlayConfig.getBorderStrokeWidth().toFloat()
    }
}