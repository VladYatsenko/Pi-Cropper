package com.yatsenko.imagepicker.ui.cropper.config

import android.graphics.Bitmap
import android.net.Uri

class CropIwaSaveConfig(
    val compressFormat: Bitmap.CompressFormat,
    val quality: Int,
    val width: Int,
    val height: Int,
    val dstUri: Uri
)