package com.yatsenko.imagepicker.utils.extensions

import java.text.DecimalFormat

object FileUtils {

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
}