package com.yatsenko.imagepicker.picker.utils

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.collection.ArrayMap
import androidx.core.database.getStringOrNull
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.picker.model.ImageContants
import com.yatsenko.imagepicker.picker.model.ImageEntity
import com.yatsenko.imagepicker.picker.model.ImageFolderEntity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ImageDataModel {

    private object HOLDER {
        val INSTANCE = ImageDataModel()
    }

    companion object {
        val instance: ImageDataModel by lazy { HOLDER.INSTANCE }
    }

    private val mAllImgList: ArrayList<ImageEntity> = ArrayList<ImageEntity>()

    private val mAllFolderList: ArrayList<ImageFolderEntity> = ArrayList<ImageFolderEntity>()

    private val mResultList: ArrayList<ImageEntity> = ArrayList<ImageEntity>()

    private var mPagerList: ArrayList<ImageEntity> = ArrayList<ImageEntity>()

//    private val mDisPlayer: IImagePickerDisplayer? = null



//    fun getDisPlayer(): IImagePickerDisplayer? {
//        return if (mDisPlayer != null) mDisPlayer else GlideImagePickerDisplayer().also({ mDisPlayer = it })
//    }
//
//
//    fun setDisPlayer(player: IImagePickerDisplayer) {
//        this.mDisPlayer = player
//    }

    fun getAllImgList(): List<ImageEntity>? {
        return mAllImgList
    }

    fun getAllFolderList(): ArrayList<ImageFolderEntity> {
        return mAllFolderList
    }

    fun getResultList(): ArrayList<ImageEntity> {
        return mResultList
    }

    fun addDataToResult(imageBean: ImageEntity?): Boolean {
        return imageBean?.let { mResultList.add(it) } ?: false
    }

    fun delDataFromResult(imageBean: ImageEntity?): Boolean {
        return mResultList.remove(imageBean) ?: false
    }

    fun hasDataInResult(imageBean: ImageEntity?): Boolean {
        return mResultList.contains(imageBean) ?: false
    }

    fun indexOfDataInResult(imageBean: ImageEntity?): Int {
        return mResultList.indexOf(imageBean)
    }

    fun getResultNum(): Int {
        return mResultList.size
    }

    fun setPagerDataList(list: ArrayList<ImageEntity>?) {
        mPagerList.clear()
        list?.let{
            mPagerList.addAll(it)
        }
    }

    fun getPagerDataList(): List<ImageEntity?>? {
        return mPagerList
    }

    fun clearPagerDataList() {
        mPagerList.clear()
    }

    fun scanAllData(context: Context): Boolean {
        try {
//            val context = context.applicationContext;
            mAllImgList.clear();
            mAllFolderList.clear();
            mResultList.clear();
            //创建“全部图片”的文件夹
            val allImgFolder = ImageFolderEntity(
                ImageContants.ID_ALL_IMAGE_FOLDER, context.getString(R.string.all_image_folder)
            )
            mAllFolderList.add(allImgFolder);
            //临时存储所有文件夹对象的Map
            val folderMap = ArrayMap<String, ImageFolderEntity>()

            //索引字段
            val columns =
                arrayOf(
                    MediaStore.Images.Media._ID,//照片id
                    MediaStore.Images.Media.BUCKET_ID,//所属文件夹id
                    //                        MediaStore.Images.Media.PICASA_ID,
                    MediaStore.Images.Media.DATA,//图片地址
                    MediaStore.Images.Media.WIDTH,//图片宽度
                    MediaStore.Images.Media.HEIGHT,//图片高度
                    MediaStore.Images.Media.DISPLAY_NAME,//图片全名，带后缀
                    //                        MediaStore.Images.Media.TITLE,
                    //                        MediaStore.Images.Media.DATE_ADDED,//创建时间？
                    MediaStore.Images.Media.DATE_MODIFIED,//最后修改时间
                    //                        MediaStore.Images.Media.DATE_TAKEN,
                    //                        MediaStore.Images.Media.SIZE,//图片文件大小
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME//所属文件夹名字
                )


            val selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";

            val selectionArgs = arrayOf("image/png", "image/jpg", "image/jpeg", "image/PNG", "image/JPG", "image/JPEG")
            val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";

            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {

                allImgFolder.num = cursor.count

                val imageIDIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                val imagePathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                val imageModifyIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val imageWidthIndex = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val imageHeightIndex =  cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val imageNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val folderIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val folderNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                do {
                    val imageId = cursor.getStringOrNull(imageIDIndex);
                    val imagePath = cursor.getStringOrNull(imagePathIndex);
                    val lastModify = cursor.getStringOrNull(imageModifyIndex);
                    val width = cursor.getStringOrNull(imageWidthIndex)
                    val height = cursor.getStringOrNull(imageHeightIndex)
                    val folderId = cursor.getStringOrNull(folderIdIndex);
                    val folderName = cursor.getStringOrNull(folderNameIndex);
                    var name = cursor.getStringOrNull(imageNameIndex);
                    if (name.isNullOrBlank()) {
                        if (imagePath.isNullOrBlank())
                            continue
                        val index = imagePath.lastIndexOf (File.separator)
                        name = imagePath.substring(index + 1);
                        if (name.isNullOrBlank())
                            continue
                    }

                    mAllImgList.add(ImageEntity(imageId, imagePath, lastModify?.toLong(), width?.toInt(), height?.toInt(), folderId))

                    var folderBean: ImageFolderEntity? = null;
                    if (!folderId.isNullOrBlank() && folderMap.containsKey(folderId))
                        folderBean = folderMap[folderId];
                    else
                        folderBean = ImageFolderEntity(folderId, folderName);
                    if (folderBean != null) {
                        folderBean.firstImgPath = imagePath
                        folderBean.gainNum()
                        folderMap[folderId] = folderBean
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            Collections.sort(mAllImgList, ImageComparator())

            allImgFolder.firstImgPath = mAllImgList.firstOrNull()?.imagePath

            mAllFolderList.addAll(folderMap.values)
            return true;
        } catch (e: Exception) {
            Log.e("ImagePicker", "ImagePicker scan data error:" + e);
            return false;
        }
    }

    fun getImagesByFolder(folderEntity: ImageFolderEntity?): ArrayList<ImageEntity>? {
        if (folderEntity == null) return null
        val folderId = folderEntity.folderId
        return if (folderId.equals(ImageContants.ID_ALL_IMAGE_FOLDER)) {
            mAllImgList
        } else {
            val resultList: ArrayList<ImageEntity> = ArrayList()
            val size = mAllImgList.size
            for (i in 0 until size) {
                val imageBean = mAllImgList.getOrNull(i)
                if (imageBean != null && folderId.equals(imageBean.folderId))
                    resultList.add(imageBean)
            }
            resultList
        }
    }

    fun clear() {
//        mDisPlayer = null
        mAllImgList.clear()
        mAllFolderList.clear()
        mResultList.clear()
        mPagerList.clear()
    }

}