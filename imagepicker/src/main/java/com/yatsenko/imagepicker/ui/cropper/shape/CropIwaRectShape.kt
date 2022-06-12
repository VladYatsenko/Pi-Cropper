package com.yatsenko.imagepicker.ui.cropper.shape

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaOverlayConfig

class CropIwaRectShape(config: CropIwaOverlayConfig) : CropIwaShape(config) {

    override fun clearArea(canvas: Canvas?, cropBounds: RectF?, clearPaint: Paint?) {
        if (cropBounds != null && clearPaint != null)
            canvas?.drawRect(cropBounds, clearPaint)
    }

    override fun drawBorders(canvas: Canvas?, cropBounds: RectF?, paint: Paint?) {
        if (cropBounds != null && paint != null)
            canvas?.drawRect(cropBounds, paint)
    }

    override fun getMask(): CropIwaShapeMask = RectShapeMask()

    class RectShapeMask : CropIwaShapeMask {
        override fun applyMaskTo(croppedRegion: Bitmap): Bitmap {
            return croppedRegion
        }
    }
}