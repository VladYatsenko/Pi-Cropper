package com.yatsenko.picropper.model

import android.widget.ImageView
import com.yatsenko.picropper.widgets.crop.AspectRatioAdapter
import com.yatsenko.picropper.widgets.crop.AspectRatioWrapper

internal sealed class AdapterResult {
    data class FolderChanged(val folder: Folder): AdapterResult()
    object GoBack: AdapterResult()

    object TakeFromCamera: AdapterResult()
    data class OnSelectImageClicked(val media: Media): AdapterResult()
    data class OnImageClicked(val view: ImageView, val media: Media, val position: Int) : AdapterResult()
    data class OnCropImageClicked(val media: Media) : AdapterResult()
    data class OnProvideImageClicked(val media: Media) : AdapterResult()

    //crop
    data class OnAspectRatioClicked(val item: AspectRatioWrapper) : AdapterResult()
    object OnResetRotationClicked : AdapterResult()
    object OnRotate90Clicked : AdapterResult()
    object OnApplyCrop: AdapterResult()
    object OnCancelCrop: AdapterResult()

    object OnRotateStart : AdapterResult()
    data class OnRotateProgress(val deltaAngle: Float) : AdapterResult()
    object OnRotateEnd : AdapterResult()

    data class OnImageRotated(val angle: Float) : AdapterResult()
    data class OnImageCropped(val media: Media.Image) : AdapterResult()
    data class OnCropError(val t: Throwable) : AdapterResult()

    object OnCropImageLoaded: AdapterResult()
}