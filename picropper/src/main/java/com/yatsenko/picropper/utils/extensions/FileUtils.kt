package com.yatsenko.picropper.utils.extensions

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat
import java.util.*


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

    fun tempFile(context: Context, extension: String): File {
        val destinationFileName = "${System.currentTimeMillis()}$extension"
        return File(context.cacheDir, destinationFileName)
    }

    fun cameraUri(activity: Activity, dirName: String, extension: String): Uri? {
        val resultPath: String = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + dirName + System.currentTimeMillis() + extension

        File(resultPath).parentFile?.mkdir()

        return if (Build.VERSION.SDK_INT < 29) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.TITLE, "Photo")
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Edited")
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            contentValues.put("_data", resultPath)
            val resolver: ContentResolver = activity.contentResolver
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            resolver.insert(contentUri, contentValues)
        } else {
            val file = File(resultPath)
            val relativeLocation = Environment.DIRECTORY_PICTURES
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "$relativeLocation/$dirName")
            contentValues.put(MediaStore.MediaColumns.TITLE, "Photo")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            contentValues.put(MediaStore.MediaColumns.BUCKET_ID, file.toString().lowercase(Locale.US).hashCode())
            contentValues.put(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, file.name.lowercase(Locale.US))
            val resolver: ContentResolver = activity.contentResolver
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            resolver.insert(contentUri, contentValues)
        }
    }

    fun cameraUri(activity: Activity, extension: String): Uri? {
        val name = System.currentTimeMillis().toString()
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File.createTempFile(name, extension, storageDir)
        return FileProvider.getUriForFile(
            activity,
            activity.applicationContext.packageName + ".provider",
            file
        )
    }

    internal fun File.fileUri(context: Context) = Uri.fromFile(this)

    fun File.getUriForFile(activity: Activity): Uri? {
        return FileProvider.getUriForFile(activity, activity.applicationContext.packageName + ".provider", this)
    }
}