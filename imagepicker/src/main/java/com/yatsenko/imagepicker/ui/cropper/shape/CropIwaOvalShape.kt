package com.yatsenko.imagepicker.ui.cropper.shape

import android.graphics.*
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaOverlayConfig

class CropIwaOvalShape (config: CropIwaOverlayConfig): CropIwaShape(config) {

    private val clipPath = Path()

    override fun clearArea(canvas: Canvas?, cropBounds: RectF?, clearPaint: Paint?) {
        if (cropBounds != null && clearPaint != null)
            canvas?.drawOval(cropBounds, clearPaint)
    }

    override fun drawBorders(canvas: Canvas?, cropBounds: RectF?, paint: Paint?) {
        if (cropBounds != null && paint != null) {
            canvas?.drawOval(cropBounds, paint)
            if (overlayConfig.isDynamicCrop()) {
                canvas?.drawRect(cropBounds, paint)
            }
        }
    }

    override fun drawGrid(canvas: Canvas, cropBounds: RectF, paint: Paint) {
        clipPath.rewind()
        clipPath.addOval(cropBounds, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)
        super.drawGrid(canvas, cropBounds, paint)
        canvas.restore()
    }

    override fun getMask(): CropIwaShapeMask {
        return OvalShapeMask()
    }

    private class OvalShapeMask : CropIwaShapeMask {
        override fun applyMaskTo(croppedRegion: Bitmap): Bitmap {
            croppedRegion.setHasAlpha(true)
            val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            val ovalRect = RectF(0f, 0f, croppedRegion.width.toFloat(), croppedRegion.height.toFloat())
            val maskShape = Path()
            //This is similar to ImageRect\Oval
            maskShape.addRect(ovalRect, Path.Direction.CW)
            maskShape.addOval(ovalRect, Path.Direction.CCW)
            val canvas = Canvas(croppedRegion)
            canvas.drawPath(maskShape, maskPaint)
            return croppedRegion
        }
    }
}