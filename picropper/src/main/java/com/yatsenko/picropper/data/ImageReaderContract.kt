package com.yatsenko.picropper.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getStringOrNull
import com.yatsenko.picropper.R
import com.yatsenko.picropper.model.Folder
import com.yatsenko.picropper.model.Media
import java.util.*

internal class ImageReaderContract(private val context: Context) {

    private val columns = arrayOf(
        BaseColumns._ID,
        MediaStore.MediaColumns.BUCKET_ID,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.MIME_TYPE
    )

    private val selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
            MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
            MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?"
    private val selectionArgs = arrayOf("image/png", "image/jpg", "image/jpeg", "image/PNG", "image/JPG", "image/JPEG")
    private val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";


    suspend fun extractImages(allImagesFolder: String): Pair<List<Folder>, List<Media>> {
        val images = mutableListOf<Media>()
        val folders = HashMap<String, Folder>()

        try {
            val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs, sortOrder);

            val all = Folder.All(allImagesFolder, count = cursor?.count ?: 0)
            folders[all.id] = all

            if (cursor?.moveToFirst() == true) {

                val imageIDIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val folderIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val imageModifyIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val folderNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val fileNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

                do {
                    val image = cursor.getImageOrNull(
                        imageIDIndex,
                        folderIdIndex,
                        imageModifyIndex,
                        folderNameIndex,
                        widthIndex,
                        heightIndex,
                        fileNameIndex,
                        sizeIndex
                    )
                    image?.let { images.add(image) }
                    cursor.getFolderOrNull(
                        folderIdIndex,
                        folderNameIndex
                    )?.let { folder ->
                        val f = folders[folder.id] as? Folder.Common ?: folder
                        folders[folder.id] =  if (image != null)
                            f.copy(firstImagePath = image.path, count = f.count + 1)
                        else f
                    }

                } while (cursor.moveToNext())
                cursor.close()
            }

            Collections.sort(images, ImageComparator())

        } catch (e: Exception) {
            Log.e("ImagePicker", "ImagePicker scan data error:" + e);
        }

        val sortedFolders = folders.map { it.value }.toMutableList().apply {
            val index = this.indexOfFirst { it is Folder.All }
            if (index == -1) return@apply
            val all = this[index]
            this.removeAt(index)
            this.add(0, all)
        }

        return Pair(sortedFolders, images)
    }

    private fun Cursor.getImageOrNull(
        imageIDIndex: Int,
        folderIdIndex: Int,
        imageModifyIndex: Int,
        folderNameIndex: Int,
        widthIndex: Int,
        heightIndex: Int,
        fileNameIndex: Int,
        sizeIndex: Int
    ): Media.Image? {
        val imageId = this.getStringOrNull(imageIDIndex) ?: return null
        val folderId = this.getStringOrNull(folderIdIndex) ?: return null
        val lastModify = this.getStringOrNull(imageModifyIndex)?.toLongOrNull() ?: 0L
        val width = this.getStringOrNull(widthIndex)?.toIntOrNull() ?: 0
        val height = this.getStringOrNull(heightIndex)?.toIntOrNull() ?: 0
        val fileName = this.getStringOrNull(fileNameIndex).orEmpty()
        val size = this.getStringOrNull(sizeIndex)?.toIntOrNull() ?: 0

        val imagePath = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageId).toString()

        return Media.Image(imageId, imagePath, lastModify, folderId, width, height, size, fileName)
    }

    private fun Cursor.getFolderOrNull(folderIdIndex: Int, folderNameIndex: Int): Folder.Common? {
        val folderId = this.getStringOrNull(folderIdIndex) ?: return null
        val folderName = this.getStringOrNull(folderNameIndex)

        return Folder.Common(folderId, folderName)
    }

}