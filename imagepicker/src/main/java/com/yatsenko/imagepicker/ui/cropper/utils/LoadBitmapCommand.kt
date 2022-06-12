package com.yatsenko.imagepicker.ui.cropper.utils

import android.content.Context
import android.net.Uri
import com.yatsenko.imagepicker.ui.cropper.image.CropIwaBitmapManager

class LoadBitmapCommand(
    private val uri: Uri,
    private var width: Int,
    private var height: Int,
    private val loadListener: CropIwaBitmapManager.BitmapLoadListener
) {

    private var executed = false

    fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    /**
     * If we call .setImageUri(Uri) on [com.steelkiwi.cropiwa.CropIwaView] from onCreate
     * view won't know its width and height, so we need to delay image loading until onSizeChanged.
     */
    fun tryExecute(context: Context) {
        if (executed) {
            return
        }
        if (width == 0 || height == 0) {
            return
        }
        executed = true
        CropIwaBitmapManager.instance.load(context, uri, width, height, loadListener)
    }
}