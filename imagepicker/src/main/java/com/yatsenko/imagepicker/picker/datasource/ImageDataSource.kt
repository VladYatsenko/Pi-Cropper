package com.yatsenko.imagepicker.picker.datasource

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns._ID
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.*
import android.util.Log
import androidx.collection.ArrayMap
import androidx.core.database.getStringOrNull
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.picker.model.ImageContants
import com.yatsenko.imagepicker.picker.model.ImageEntity
import com.yatsenko.imagepicker.picker.model.ImageFolderEntity
import com.yatsenko.imagepicker.picker.utils.ImageComparator
import java.util.*
import kotlin.collections.ArrayList

class ImageDataSource {

    private object HOLDER {
        val INSTANCE = ImageDataSource()
    }

    companion object {
        val INSTANCE: ImageDataSource by lazy { HOLDER.INSTANCE }
    }

    private val imageList: ArrayList<ImageEntity> = ArrayList()
    private val folderList: ArrayList<ImageFolderEntity> = ArrayList()
    private val resultList: ArrayList<ImageEntity> = ArrayList()

    fun getAllImgList(): List<ImageEntity>? {
        return imageList
    }

    fun getAllFolderList(): ArrayList<ImageFolderEntity> {
        return folderList
    }

    fun getResultList(): ArrayList<ImageEntity> {
        return resultList
    }

    fun addDataToResult(imageBean: ImageEntity?): Boolean {
        return imageBean?.let { resultList.add(it) } ?: false
    }

    fun deleteDataFromResult(imageBean: ImageEntity?): Boolean {
        return resultList.remove(imageBean) ?: false
    }

    fun hasDataInResult(imageBean: ImageEntity?): Boolean {
        return resultList.contains(imageBean) ?: false
    }

    fun indexOfDataInResult(imageBean: ImageEntity?): Int {
        return resultList.indexOf(imageBean)
    }

    fun getResultNum(): Int {
        return resultList.size
    }

    fun scanAllData(context: Context): Boolean{
        try {
            imageList.clear();
            folderList.clear();
            resultList.clear();
            val allImgFolder = ImageFolderEntity(ImageContants.ID_ALL_IMAGE_FOLDER, context.getString(R.string.all_image_folder))
            folderList.add(allImgFolder)

            val folderMap = ArrayMap<String, ImageFolderEntity>()
            val columns = arrayOf(_ID, BUCKET_ID, MIME_TYPE, DATE_MODIFIED, BUCKET_DISPLAY_NAME, DISPLAY_NAME)
            val selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";
            val selectionArgs = arrayOf("image/png", "image/jpg", "image/jpeg", "image/PNG", "image/JPG", "image/JPEG")
            val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";

            val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {

                allImgFolder.num = cursor.count

                val imageIDIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                val imageModifyIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val imageNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val folderIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val folderNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                do {
                    val imageId = cursor.getStringOrNull(imageIDIndex);
                    val lastModify = cursor.getStringOrNull(imageModifyIndex);
                    val folderId = cursor.getStringOrNull(folderIdIndex);
                    val folderName = cursor.getStringOrNull(folderNameIndex);

                    val imagePath = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageId).toString()

                    imageList.add(ImageEntity(imageId, imagePath, lastModify?.toLong(), folderId))

                    val folderBean = if (!folderId.isNullOrBlank() && folderMap.containsKey(folderId))
                        folderMap[folderId]
                    else
                        ImageFolderEntity(folderId, folderName)

                    if (folderBean != null) {
                        folderBean.firstImgPath = imagePath
                        folderBean.gainNum()
                        folderMap[folderId] = folderBean
                    }

                } while (cursor.moveToNext())

                cursor.close();
            }

            Collections.sort(imageList, ImageComparator())

            allImgFolder.firstImgPath = imageList.firstOrNull()?.imagePath

            folderList.addAll(folderMap.values)

            return true
        } catch (e: Exception) {
            Log.e("ImagePicker", "ImagePicker scan data error:" + e);
            return false
        }
    }

    fun getImagesByFolder(folderEntity: ImageFolderEntity?): ArrayList<ImageEntity>? {
        if (folderEntity == null) return null
        val folderId = folderEntity.folderId
        return if (folderId.equals(ImageContants.ID_ALL_IMAGE_FOLDER)) {
            imageList
        } else {
            val resultList: ArrayList<ImageEntity> = ArrayList()
            val size = imageList.size
            for (i in 0 until size) {
                val imageBean = imageList.getOrNull(i)
                if (imageBean != null && folderId.equals(imageBean.folderId))
                    resultList.add(imageBean)
            }
            resultList
        }
    }

    fun clear() {
        imageList.clear()
        folderList.clear()
        resultList.clear()
    }

}