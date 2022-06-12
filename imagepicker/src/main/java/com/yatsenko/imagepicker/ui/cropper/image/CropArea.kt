package com.yatsenko.imagepicker.ui.cropper.image

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF

class CropArea(private val imageRect: Rect, private val cropRect: Rect) {

    companion object {

        fun create(coordinateSystem: RectF, imageRect: RectF, cropRect: RectF): CropArea {
            return CropArea(
                moveRectToCoordinateSystem(coordinateSystem, imageRect),
                moveRectToCoordinateSystem(coordinateSystem, cropRect)
            )
        }

        private fun moveRectToCoordinateSystem(system: RectF, rect: RectF): Rect {
            val originX = system.left
            val originY = system.top
            return Rect(
                Math.round(rect.left - originX), Math.round(rect.top - originY),
                Math.round(rect.right - originX), Math.round(rect.bottom - originY)
            )
        }
    }

    fun applyCropTo(bitmap: Bitmap): Bitmap? {
        val immutableCropped = Bitmap.createBitmap(
            bitmap,
            findRealCoordinate(bitmap.width, cropRect.left, imageRect.width().toFloat()),
            findRealCoordinate(bitmap.height, cropRect.top, imageRect.height().toFloat()),
            findRealCoordinate(bitmap.width, cropRect.width(), imageRect.width().toFloat()),
            findRealCoordinate(bitmap.height, cropRect.height(), imageRect.height().toFloat())
        )
        return immutableCropped.copy(immutableCropped.config, true)
    }


    private fun findRealCoordinate(imageRealSize: Int, cropCoordinate: Int, cropImageSize: Float): Int {
        return Math.round(imageRealSize * cropCoordinate / cropImageSize)
    }

}