package com.yatsenko.imagepicker.widgets.aspectRatio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.utils.extensions.applyMargin
import com.yatsenko.imagepicker.utils.extensions.dpToPx
import java.util.*

class AspectRatioPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        fun create(parent: ViewGroup): AspectRatioPreviewView {
            return AspectRatioPreviewView(parent.context).apply {
                val margin = dpToPx(8).toInt()
                this.applyMargin(margin, margin, margin, margin)
                this.layoutParams = ViewGroup.LayoutParams(
                    dpToPx(80).toInt(),
                    dpToPx(80).toInt()
                )
            }
        }
    }

    private val SIZE_TEXT = 12F

    private var colorRectNotSelected = ContextCompat.getColor(context, R.color.pickerColorAccent)
    private var colorRectSelected = ContextCompat.getColor(context, R.color.aspectRatioNotSelected)
    private var colorText = ContextCompat.getColor(context, R.color.aspectRatioText)

    var data: Data? = null
        set(value) {
            field = value
            refreshLayout()
        }

    private var rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorRectNotSelected
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2)
    }
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val dm = context.resources.displayMetrics
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, SIZE_TEXT, dm)
        color = colorText
    }

    private var centerX = 0f

    private var previewRect = RectF()
    private var textOutBounds = Rect()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w * 0.5f
        data?.let {
            configurePreviewRect(it)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(previewRect, rectPaint)
        val text: String = data?.ratio?.ratioString?.invoke(context) ?: ""
        textPaint.getTextBounds(text, 0, text.length, textOutBounds)
        canvas.drawText(
            text, centerX - textOutBounds.width() * 0.5f,
            bottom - textOutBounds.height() * 0.5f,
            textPaint
        )
    }

    private fun refreshLayout() {
        if (width != 0 && height != 0) {
            data?.let {
                rectPaint.color = if (it.isSelected) colorRectSelected else colorRectNotSelected
                configurePreviewRect(it)
            }
        }

        invalidate()
    }

    private fun configurePreviewRect(data: Data) {
        val str = data.ratio.ratioString(context)
        val ratio = data.ratio
        textPaint.getTextBounds(str, 0, str.length, textOutBounds)
        val freeSpace = RectF(0f, 0f, width.toFloat(), height - textOutBounds.height() * 1.2f)
        val calculateFromWidth = (ratio.height < ratio.width || ratio.isSquare && freeSpace.width() < freeSpace.height())
        val halfWidth: Float
        val halfHeight: Float
        if (calculateFromWidth) {
            halfWidth = freeSpace.width() * 0.8f * 0.5f
            halfHeight = halfWidth / ratio.ratio
        } else {
            halfHeight = freeSpace.height() * 0.8f * 0.5f
            halfWidth = halfHeight * ratio.ratio
        }
        previewRect = RectF(
            freeSpace.centerX() - halfWidth,
            freeSpace.centerY() - halfHeight,
            freeSpace.centerX() + halfWidth,
            freeSpace.centerY() + halfHeight
        )
    }

    data class Data(
        val ratio: AspectRatio,
        val isSelected: Boolean
    ) {

        companion object {
            fun createFrom(aspect: AspectRatio) = Data(aspect, false)
        }
    }

}