package com.yatsenko.imagepicker.picker.utils

import com.yatsenko.imagepicker.picker.model.ImageEntity

class ImageComparator: Comparator<ImageEntity> {

    override fun compare(first: ImageEntity?, second: ImageEntity?): Int {
        val fModify: Long = first?.lastModified ?: 0
        val sModify: Long = second?.lastModified ?: 0
        return if (fModify > sModify) -1 else if (fModify < sModify) 1 else 0
    }
}