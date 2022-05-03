package com.yatsenko.imagepicker.model

import android.widget.ImageView

sealed class AdapterResult {
    data class FolderChanged(val folder: Folder): AdapterResult()
    object GoBack: AdapterResult()

    data class OnSelectImageClicked(val image: Media): AdapterResult()
    data class OnImageClicked(val view: ImageView, val image: Media, val position: Int) : AdapterResult()
    object ImageLoaded : AdapterResult()

}