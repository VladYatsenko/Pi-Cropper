package com.yatsenko.imagepicker.model

import android.content.Context
import androidx.annotation.IntRange
import com.yatsenko.imagepicker.R
import java.util.*

sealed class AspectRatio(@IntRange(from = 1) open val width: Int, @IntRange(from = 1) open val height: Int){

    companion object {
        @SuppressWarnings("Range")
        val IMG_SRC = Dynamic

        val defaultList = listOf(
            Dynamic,
            Aspect4to3,
            Aspect16to9,
            Aspect1to1,
            Aspect3to4,
            Aspect9to16
        )

        fun createFrom(media: Media): AspectOriginal {
            val factor = greatestCommonFactor(media.width, media.height)
            val widthRatio: Int = media.width / factor
            val heightRatio: Int = media.height / factor
            return AspectOriginal(widthRatio, heightRatio)
        }

        private fun greatestCommonFactor(width: Int, height: Int): Int {
            return if (height == 0) width else greatestCommonFactor(height, width % height)
        }

    }

    @SuppressWarnings("Range")
    object Dynamic: AspectRatio(-1, -1)

    data class AspectOriginal(
        override val width: Int,
        override val height: Int
        ): AspectRatio(width, height)

    object Aspect4to3: AspectRatio(4, 3)

    object Aspect16to9: AspectRatio(16, 9)

    object Aspect1to1: AspectRatio(1, 1)

    object Aspect3to4: AspectRatio(3, 4)

    object Aspect9to16: AspectRatio(9, 16)

    val isSquare: Boolean
        get() = width == height

    val ratio: Float
        get() = width.toFloat() / height

    val ratioString: (Context) -> String = {
        when(this) {
            is AspectOriginal -> it.getString(R.string.original)
            is Dynamic -> it.getString(R.string.dynamic)
            else -> String.format(Locale.US, "%d:%d", width, height)
        }
    }

}