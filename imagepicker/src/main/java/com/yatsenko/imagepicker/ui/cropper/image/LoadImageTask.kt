package com.yatsenko.imagepicker.ui.cropper.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask

class LoadImageTask constructor(
    private var context: Context,
    private val uri: Uri,
    private val desiredWidth: Int,
    private val desiredHeight: Int
) : AsyncTask<Void, Void, Throwable>() {

    private var result: Bitmap? = null

    override fun doInBackground(vararg params: Void?): Throwable? {
        try {
            result = CropIwaBitmapManager.instance.loadToMemory(
                context, uri, desiredWidth,
                desiredHeight
            )
            if (result == null) {
                return NullPointerException("Failed to load bitmap")
            }
        } catch (e: Exception) {
            return e
        }
        return null
    }

    override fun onPostExecute(e: Throwable?) {
        CropIwaBitmapManager.instance.notifyListener(uri, result, e)
    }

}