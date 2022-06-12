package com.yatsenko.imagepicker.ui.cropper

import androidx.annotation.IntRange

sealed class AspectRatio(@IntRange(from = 1) open val width: Int, @IntRange(from = 1) open val height: Int){

    companion object {
        @SuppressWarnings("Range")
        val IMG_SRC = Dynamic

    }

    @SuppressWarnings("Range")
    object Dynamic: AspectRatio(-1, -1)

    data class AspectOriginal(
        @IntRange(from = 1) override val width: Int,
        @IntRange(from = 1) override val height: Int
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

}