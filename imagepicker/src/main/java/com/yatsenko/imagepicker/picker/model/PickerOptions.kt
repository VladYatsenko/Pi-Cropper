package com.yatsenko.imagepicker.picker.model

import android.os.Parcel
import android.os.Parcelable

class PickerOptions : Parcelable{

    private var type: ImagePickType = ImagePickType.GALLERY
    private var maxNum = 1
    private var cachePath: String? = null

    constructor()

    constructor(parcel: Parcel) : this() {
        maxNum = parcel.readInt()
        cachePath = parcel.readString()
    }

    fun getType(): ImagePickType? {
        return type
    }

    fun setType(type: ImagePickType) {
        this.type = type
    }

    fun getMaxNum(): Int {
        return maxNum
    }

    fun setMaxNum(maxNum: Int) {
        if (maxNum > 0) this.maxNum = maxNum
    }

    fun getCachePath(): String? {
        return cachePath
    }

    fun setCachePath(cachePath: String?) {
        this.cachePath = cachePath
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(maxNum)
        parcel.writeString(cachePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PickerOptions> {
        override fun createFromParcel(parcel: Parcel): PickerOptions {
            return PickerOptions(parcel)
        }

        override fun newArray(size: Int): Array<PickerOptions?> {
            return arrayOfNulls(size)
        }
    }

}