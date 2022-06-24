package com.yatsenko.imagepicker.widgets.crop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.yatsenko.imagepicker.R

class HorizontalProgressWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var middleLineColor = ContextCompat.getColor(context, R.color.cerulean)
        set(value) {
            field = value
            progressMiddleLinePaint.color = value
            invalidate()
        }

    private val canvasClipBounds = Rect()

    private var lastTouchedPosition = 0f

    private var progressLineWidth = context.resources.getDimensionPixelSize(R.dimen.width_horizontal_wheel_progress_line)
    private var progressLineHeight = context.resources.getDimensionPixelSize(R.dimen.height_horizontal_wheel_progress_line)
    private var progressLineMargin = context.resources.getDimensionPixelSize(R.dimen.margin_horizontal_wheel_progress_line)

    private var progressLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = progressLineWidth.toFloat()
        color = ContextCompat.getColor(context, R.color.pickerColorAccent)
    }

    private var progressMiddleLinePaint = Paint(progressLinePaint).apply {
        color = middleLineColor
        strokeCap = Paint.Cap.ROUND
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.width_middle_wheel_progress_line).toFloat()
    }

    private var scrollStarted = false
    private var totalScrollDistance = 0f

    var scrollingListener: ScrollingListener? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastTouchedPosition = event.x
            MotionEvent.ACTION_UP -> if (scrollingListener != null) {
                scrollStarted = false
                scrollingListener?.onScrollEnd()
            }
            MotionEvent.ACTION_MOVE -> {
                val distance = event.x - lastTouchedPosition
                if (distance != 0f) {
                    if (!scrollStarted) {
                        scrollStarted = true
                        scrollingListener?.onScrollStart()
                    }
                    onScrollEvent(event, distance)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.getClipBounds(canvasClipBounds)
        val linesCount = canvasClipBounds.width() / (progressLineWidth + progressLineMargin)
        val deltaX = totalScrollDistance % (progressLineMargin + progressLineWidth).toFloat()
        for (i in 0 until linesCount) {
            if (i < linesCount / 4) {
                progressLinePaint.alpha = (255 * (i / (linesCount / 4).toFloat())).toInt()
            } else if (i > linesCount * 3 / 4) {
                progressLinePaint.alpha = (255 * ((linesCount - i) / (linesCount / 4).toFloat())).toInt()
            } else {
                progressLinePaint.alpha = 255
            }
            canvas.drawLine(
                -deltaX + canvasClipBounds.left + i * (progressLineWidth + progressLineMargin),
                canvasClipBounds.centerY() - progressLineHeight / 4.0f,
                -deltaX + canvasClipBounds.left + i * (progressLineWidth + progressLineMargin),
                canvasClipBounds.centerY() + progressLineHeight / 4.0f, progressLinePaint
            )
        }
        canvas.drawLine(
            canvasClipBounds.centerX().toFloat(),
            canvasClipBounds.centerY() - progressLineHeight / 2.0f,
            canvasClipBounds.centerX().toFloat(),
            canvasClipBounds.centerY() + progressLineHeight / 2.0f,
            progressMiddleLinePaint
        )
    }

    private fun onScrollEvent(event: MotionEvent, distance: Float) {
        totalScrollDistance -= distance
        postInvalidate()
        lastTouchedPosition = event.x
        scrollingListener?.onScroll(-distance, totalScrollDistance)
    }

    interface ScrollingListener {
        fun onScrollStart()
        fun onScroll(delta: Float, totalDistance: Float)
        fun onScrollEnd()
    }

}