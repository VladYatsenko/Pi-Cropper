package com.yatsenko.imagepicker.model

import android.widget.ImageView
import com.yatsenko.imagepicker.widgets.crop.AspectRatioAdapter

sealed class AdapterResult {
    data class FolderChanged(val folder: Folder): AdapterResult()
    object GoBack: AdapterResult()

    data class OnSelectImageClicked(val media: Media): AdapterResult()
    data class OnImageClicked(val view: ImageView, val media: Media, val position: Int) : AdapterResult()
    class OnCropImageClicked(val media: Media) : AdapterResult()

    //crop
    data class OnAspectRatioClicked(val item: AspectRatioAdapter.Data) : AdapterResult()
    object OnResetRotationClicked : AdapterResult()
    object OnRotate90Clicked : AdapterResult()

    object OnRotateStart : AdapterResult()
    data class OnRotateProgress(val deltaAngle: Float) : AdapterResult()
    object OnRotateEnd : AdapterResult()


}