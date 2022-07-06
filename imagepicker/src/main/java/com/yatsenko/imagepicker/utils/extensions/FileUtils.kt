package com.yatsenko.imagepicker.utils.extensions

import android.content.Context
import android.net.Uri
import java.io.File
import java.text.DecimalFormat

internal object FileUtils {

    fun stringFileSize(size: Int): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        return when {
            size < sizeMb -> df.format(size / sizeKb).toString() + " Kb"
            size < sizeGb -> df.format(size / sizeMb).toString() + " Mb"
            size < sizeTerra -> df.format(size / sizeGb).toString() + " Gb"
            else -> ""
        }
    }

    fun tempFileUri(context: Context, extension: String): Uri {
        return Uri.fromFile(tempFile(context, extension))
    }

    fun tempFile(context: Context, extension: String): File {
        val destinationFileName = "${System.currentTimeMillis()}$extension"
        return File(context.cacheDir, destinationFileName)
    }

    fun File.fileUri(context: Context) = Uri.fromFile(this)
}