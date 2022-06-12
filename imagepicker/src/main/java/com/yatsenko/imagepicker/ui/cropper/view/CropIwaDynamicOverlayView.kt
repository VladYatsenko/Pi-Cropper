package com.yatsenko.imagepicker.ui.cropper.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.SparseArray
import android.view.MotionEvent
import androidx.core.util.isNotEmpty
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaOverlayConfig
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaShape
import com.yatsenko.imagepicker.ui.cropper.utils.boundValue
import com.yatsenko.imagepicker.ui.cropper.utils.enlargeRectBy
import com.yatsenko.imagepicker.ui.cropper.utils.moveRectBounded
import com.yatsenko.imagepicker.utils.extensions.dpToPx
import java.util.*

@SuppressLint("ViewConstructor")
open class CropIwaDynamicOverlayView(
    context: Context,
    private val config: CropIwaOverlayConfig
) : CropIwaOverlayView(context, config) {

    companion object {
        private val CLICK_AREA_CORNER_POINT: Float = dpToPx(24)

        private const val LEFT_TOP = 0
        private const val RIGHT_TOP = 1
        private const val LEFT_BOTTOM = 2
        private const val RIGHT_BOTTOM = 3
    }

    private val cornerSides: Array<FloatArray> = generateCornerSides(Math.min(config.getMinWidth(), config.getMinHeight()) * 0.3f)
    private val cornerPoints: Array<CornerPoint?> = arrayOfNulls<CornerPoint>(4)
    private var fingerToCornerMapping = SparseArray<CornerPoint>()

    private var cropDragStartPoint: PointF? = null
    private var cropRectBeforeDrag: RectF? = null

    override fun onImagePositioned(imageRect: RectF) {
        super.onImagePositioned(imageRect)
        initCornerPoints()
        invalidate()
    }

    private fun initCornerPoints() {
        if (cropRect.width() > 0 && cropRect.height() > 0) {
            if (listOf(*cornerPoints).any { it == null }) {
                val leftTop = PointF(cropRect.left, cropRect.top)
                val leftBot = PointF(cropRect.left, cropRect.bottom)
                val rightTop = PointF(cropRect.right, cropRect.top)
                val rightBot = PointF(cropRect.right, cropRect.bottom)
                cornerPoints[LEFT_TOP] = CornerPoint(leftTop, rightTop, leftBot)
                cornerPoints[LEFT_BOTTOM] = CornerPoint(leftBot, rightBot, leftTop)
                cornerPoints[RIGHT_TOP] = CornerPoint(rightTop, leftTop, rightBot)
                cornerPoints[RIGHT_BOTTOM] = CornerPoint(rightBot, leftBot, rightTop)
            } else {
                updateCornerPointsCoordinates()
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (!shouldDrawOverlay) {
            return false
        }
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> onStartGesture(ev)
            MotionEvent.ACTION_POINTER_DOWN -> onPointerDown(ev)
            MotionEvent.ACTION_MOVE -> onPointerMove(ev)
            MotionEvent.ACTION_POINTER_UP -> onPointerUp(ev)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> onEndGesture()
            else -> return false
        }
        invalidate()
        //        invalidate(
//                (int) cropRect.left, (int) cropRect.top,
//                (int) cropRect.right, (int) cropRect.bottom);
        return true
    }

    private fun onStartGesture(ev: MotionEvent) {
        //Does user want to resize the crop area?
        if (tryAssociateWithCorner(ev)) {
            return
        }
        //Does user want to drag the crop area?
        val index = ev.actionIndex
        if (cropRect.contains(ev.getX(index), ev.getY(index))) {
            cropDragStartPoint = PointF(ev.getX(index), ev.getY(index))
            cropRectBeforeDrag = RectF(cropRect)
        }
    }

    private fun onPointerDown(ev: MotionEvent) {
        if (isResizing()) {
            tryAssociateWithCorner(ev)
        }
    }

    private fun onPointerUp(ev: MotionEvent) {
        val id = ev.getPointerId(ev.actionIndex)
        fingerToCornerMapping.remove(id)
    }

    private fun onPointerMove(ev: MotionEvent) {
        if (isResizing()) {
            for (i in 0 until ev.pointerCount) {
                val id = ev.getPointerId(i)
                val point: CornerPoint? = fingerToCornerMapping[id]
                point?.processDrag(
                    boundValue(ev.getX(i), 0f, width.toFloat()),
                    boundValue(ev.getY(i), 0f, height.toFloat())
                )
            }
            updateCropAreaCoordinates()
        } else if (isDraggingCropArea()) {
            val deltaX = ev.x - cropDragStartPoint!!.x
            val deltaY = ev.y - cropDragStartPoint!!.y
            cropRectBeforeDrag?.let { rect ->
                cropRect = moveRectBounded(
                    rect,
                    deltaX,
                    deltaY,
                    width,
                    height,
                    cropRect
                )
            }
            updateCornerPointsCoordinates()
        }
    }

    private fun onEndGesture() {
        if (cropRectBeforeDrag != cropRect) {
            notifyNewBounds()
        }
        if (fingerToCornerMapping.size() > 0) {
            notifyNewBounds()
        }
        fingerToCornerMapping.clear()
        cropDragStartPoint = null
        cropRectBeforeDrag = null
    }

    private fun updateCornerPointsCoordinates() {
        cornerPoints[LEFT_TOP]?.processDrag(cropRect.left, cropRect.top)
        cornerPoints[RIGHT_BOTTOM]?.processDrag(cropRect.right, cropRect.bottom)
    }

    private fun updateCropAreaCoordinates() {
        val lt = cornerPoints[LEFT_TOP] ?: return
        val rb = cornerPoints[RIGHT_BOTTOM] ?: return
        cropRect[lt.x(), lt.y(), rb.x()] = rb.y()
    }

    override fun onDraw(canvas: Canvas) {
        if (shouldDrawOverlay) {
            super.onDraw(canvas)
            if (areCornersInitialized()) {
                val shape: CropIwaShape = config.getCropShape()
                for (i in cornerPoints.indices) {
                    val point = cornerPoints[i] ?: continue
                    shape.drawCorner(
                        canvas, point.x(), point.y(),
                        cornerSides[i][0], cornerSides[i][1]
                    )
                }
            }
        }
    }

    override fun isResizing(): Boolean {
        return fingerToCornerMapping.isNotEmpty()
    }

    override fun isDraggingCropArea(): Boolean {
        return cropDragStartPoint != null
    }

    /**
     * @return true if ev.x && ev.y are in area of some corner point
     */
    private fun tryAssociateWithCorner(ev: MotionEvent): Boolean {
        val index = ev.actionIndex
        return tryAssociateWithCorner(
            ev.getPointerId(index),
            ev.getX(index), ev.getY(index)
        )
    }

    private fun tryAssociateWithCorner(id: Int, x: Float, y: Float): Boolean {
        for (cornerPoint in cornerPoints) {
            if (cornerPoint?.isClicked(x, y) == true) {
                fingerToCornerMapping.put(id, cornerPoint)
                return true
            }
        }
        return false
    }

    private fun areCornersInitialized(): Boolean {
        return cornerPoints[0]?.isValid == true
    }

    override fun onConfigChanged() {
        super.onConfigChanged()
        initCornerPoints()
    }

    private inner class CornerPoint(
        private val thisPoint: PointF,
        private val horizontalNeighbourPoint: PointF,
        private val verticalNeighbourPoint: PointF
    ) {

        private val clickableArea: RectF = RectF()

        fun processDrag(x: Float, y: Float) {
            val newX = computeCoordinate(
                thisPoint.x, x, horizontalNeighbourPoint.x,
                config.getMinWidth()
            )
            thisPoint.x = newX
            verticalNeighbourPoint.x = newX
            val newY = computeCoordinate(
                thisPoint.y, y, verticalNeighbourPoint.y,
                config.getMinHeight()
            )
            thisPoint.y = newY
            horizontalNeighbourPoint.y = newY
        }

        private fun computeCoordinate(old: Float, candidate: Float, opposite: Float, min: Int): Float {
            val minAllowedPosition: Float
            var isCandidateAllowed = Math.abs(candidate - opposite) > min
            val isDraggingFromLeftOrTop = opposite > old
            if (isDraggingFromLeftOrTop) {
                minAllowedPosition = opposite - min
                isCandidateAllowed = isCandidateAllowed and (candidate < opposite)
            } else {
                minAllowedPosition = opposite + min
                isCandidateAllowed = isCandidateAllowed and (candidate > opposite)
            }
            return if (isCandidateAllowed) candidate else minAllowedPosition
        }

        fun isClicked(x: Float, y: Float): Boolean {
            clickableArea[thisPoint.x, thisPoint.y, thisPoint.x] = thisPoint.y
            enlargeRectBy(CLICK_AREA_CORNER_POINT, clickableArea)
            return clickableArea.contains(x, y)
        }

        fun x(): Float {
            return thisPoint.x
        }

        fun y(): Float {
            return thisPoint.y
        }

        override fun toString(): String {
            return thisPoint.toString()
        }

        val isValid: Boolean
            get() = Math.abs(thisPoint.x - horizontalNeighbourPoint.x) >= config.getMinWidth()

    }

    private fun generateCornerSides(length: Float): Array<FloatArray> {
        val result = Array(4) { FloatArray(2) }
        result[LEFT_TOP] = floatArrayOf(length, length)
        result[LEFT_BOTTOM] = floatArrayOf(length, -length)
        result[RIGHT_TOP] = floatArrayOf(-length, length)
        result[RIGHT_BOTTOM] = floatArrayOf(-length, -length)
        return result
    }
}