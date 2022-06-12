package com.yatsenko.imagepicker.ui.cropper.listeners

import android.graphics.RectF

interface OnImagePositionedListener {
    fun onImagePositioned(imageRect: RectF)
}