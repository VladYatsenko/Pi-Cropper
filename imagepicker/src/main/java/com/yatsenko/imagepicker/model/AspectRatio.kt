package com.yatsenko.imagepicker.model

import android.os.Parcelable
import androidx.annotation.IntRange
import kotlinx.android.parcel.Parcelize
import java.util.*

sealed class AspectRatio(
    @IntRange(from = 1) open val width: Int,
    @IntRange(from = 1) open val height: Int,
    open val name: String?
): Parcelable {

    companion object {

        val defaultList = listOf(
            Original(),
            Dynamic(),
            Custom(4, 3),
            Custom(16, 9),
            Custom(1, 1),
            Custom(3, 4),
            Custom(9, 16)
        )

        internal fun remapOriginal(ratio: AspectRatio, media: Media): AspectRatio {
            return if (ratio is Original) {
                val factor = greatestCommonFactor(media.width, media.height)
                val widthRatio: Int = media.width / factor
                val heightRatio: Int = media.height / factor
                Original(ratio.name, widthRatio, heightRatio)
            } else ratio
        }

        private fun greatestCommonFactor(width: Int, height: Int): Int {
            return if (height == 0) width else greatestCommonFactor(height, width % height)
        }

    }

    @Parcelize
    data class Dynamic(
        override val name: String = "Free"
    ) : AspectRatio(Int.MIN_VALUE, Int.MIN_VALUE, name)

    @Parcelize
    data class Original(
        override val name: String = "Original",
        override val width: Int = -1,
        override val height: Int = -1
    ) : AspectRatio(width, height, name)

    @Parcelize
    data class Custom(
        override val width: Int,
        override val height: Int,
        override val name: String? = null
    ) : AspectRatio(width, height, name)

    val isDynamic: Boolean
        get() = this is Dynamic

    val ratio: Float
        get() = width.toFloat() / height

    val ratioString: String
        get() = name ?: String.format(Locale.US, "%d:%d", width, height)

}