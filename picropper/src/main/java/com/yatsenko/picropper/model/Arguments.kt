package com.yatsenko.picropper.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

internal data class Arguments(
    val aspectRatioList: List<AspectRatio>,
    val allImagesFolder: String,
    val collectCount: Int,
    val circleCrop: Boolean,
    val quality: Int,
    val compressFormat: CompressFormat,
    private val shouldForceOpenEditor: Boolean
) {

    val single: Boolean
        get() = collectCount == 1

    val forceOpenEditor: Boolean
        get() = shouldForceOpenEditor && single

    val bitmapCompressFormat: Bitmap.CompressFormat
        get() = when(compressFormat) {
            CompressFormat.JPEG -> Bitmap.CompressFormat.JPEG
            CompressFormat.PNG -> Bitmap.CompressFormat.PNG
        }

    val compressFormatExtension: String
        get() = when(compressFormat) {
            CompressFormat.JPEG -> ".jpeg"
            CompressFormat.PNG -> ".png"
        }
}

sealed class CompressFormat: Parcelable {

    @Parcelize
    object JPEG: CompressFormat()

    @Parcelize
    object PNG: CompressFormat()

}