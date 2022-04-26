package com.yatsenko.imagepicker.data

import com.yatsenko.imagepicker.model.Image

class ImageComparator: Comparator<Image> {

    override fun compare(first: Image?, second: Image?): Int {
        val fModify: Long = first?.lastModified ?: 0
        val sModify: Long = second?.lastModified ?: 0
        return if (fModify > sModify) -1 else if (fModify < sModify) 1 else 0
    }
}