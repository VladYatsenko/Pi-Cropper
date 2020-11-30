package com.yatsenko.imagepicker.picker.model

import android.os.Parcel
import android.os.Parcelable

class ImageEntity : Parcelable {

    var imageId: String? = null
    var imagePath: String? = null
    var croppedImagePath: String? = null
    var lastModified: Long? = null
    var width: Int? = null
    var height: Int? = null
    var folderId: String? = null

    constructor(imageId: String?, imagePath: String?, lastModified: Long?, folderId: String?){
        this.imageId = imageId
        this.imagePath = imagePath
        this.lastModified = lastModified
        this.folderId = folderId
    }

    constructor(imageId: String?, imagePath: String?, lastModified: Long?, width: Int?, height: Int?, folderId: String?){
        this.imageId = imageId
        this.imagePath = imagePath
        this.lastModified = lastModified
        this.width = width
        this.height = height
        this.folderId = folderId
    }

    private constructor (parcel: Parcel) {
        imageId = parcel.readString()
        imagePath = parcel.readString()
        lastModified = parcel.readValue(Long::class.java.classLoader) as Long?
        width = parcel.readInt()
        height = parcel.readInt()
        folderId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageId)
        parcel.writeString(imagePath)
        parcel.writeValue(lastModified)
        parcel.writeValue(width)
        parcel.writeValue(height)
        parcel.writeString(folderId)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getImage(): String? {
        return if (croppedImagePath != null) croppedImagePath else imagePath
    }

    companion object CREATOR : Parcelable.Creator<ImageEntity> {
        override fun createFromParcel(parcel: Parcel): ImageEntity {
            return ImageEntity(parcel)
        }

        override fun newArray(size: Int): Array<ImageEntity?> {
            return arrayOfNulls(size)
        }
    }


}