package com.yatsenko.picropper.data

import com.yatsenko.picropper.model.Media

internal class ImageComparator: Comparator<Media> {

    override fun compare(first: Media?, second: Media?): Int {
        val fModify: Long = first?.lastModified ?: 0
        val sModify: Long = second?.lastModified ?: 0
        return if (fModify > sModify) -1 else if (fModify < sModify) 1 else 0
    }
}