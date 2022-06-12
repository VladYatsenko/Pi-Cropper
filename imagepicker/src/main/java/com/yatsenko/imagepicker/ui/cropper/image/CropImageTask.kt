package com.yatsenko.imagepicker.ui.cropper.image

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaSaveConfig
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaShapeMask
import com.yatsenko.imagepicker.ui.cropper.utils.closeSilently
import java.io.IOException

class CropImageTask constructor(
    private var context: Context,
    private val cropArea: CropArea,
    private val mask: CropIwaShapeMask,
    private val srcUri: Uri,
    private val saveConfig: CropIwaSaveConfig
) : AsyncTask<Void, Void, Throwable>() {

    override fun doInBackground(vararg params: Void?): Throwable? {
        try {
            val bitmap = CropIwaBitmapManager.instance
                .loadToMemory(context, srcUri, saveConfig.width, saveConfig.height)
                ?: return NullPointerException("Failed to load bitmap")
            var cropped = cropArea.applyCropTo(bitmap)!!
            cropped = mask.applyMaskTo(cropped)
            val dst: Uri = saveConfig.dstUri
            val os = context.contentResolver.openOutputStream(dst)
            cropped.compress(saveConfig.compressFormat, saveConfig.quality, os)
            os.closeSilently()
            bitmap.recycle()
            cropped.recycle()
        } catch (e: IOException) {
            return e
        }
        return null
    }

    override fun onPostExecute(throwable: Throwable?) {
        if (throwable == null) {
            CropIwaResultReceiver.onCropCompleted(context, saveConfig.dstUri)
        } else {
            CropIwaResultReceiver.onCropFailed(context, throwable)
        }
    }

}