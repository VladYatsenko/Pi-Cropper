package com.yatsenko.imagepicker.ui.cropper.shape;

import android.graphics.Bitmap;

import java.io.Serializable;

interface CropIwaShapeMask: Serializable {
    fun applyMaskTo(croppedRegion: Bitmap): Bitmap
}
