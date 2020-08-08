package com.yatsenko.imagepicker.model

class ImageEntity constructor(

    val imageId: String?,
    val imagePath: String?,
    val lastModified: Long?,
    val width: Int?,
    val height: Int?,
    val folderId: String?

) : Comparable<ImageEntity> {

    override fun compareTo(other: ImageEntity): Int {
        val fModify: Long = this.lastModified ?: 0
        val sModify: Long = other.lastModified ?: 0
        return if (fModify > sModify) -1 else if (fModify < sModify) 1 else 0
    }

//    private val imageId: String? = null
//
//    private val imagePath: String? = null
//
//    private val lastModified: Long? = null
//
//    private val width = 0
//
//    private val height = 0
//
//    private val folderId: String? = null
}