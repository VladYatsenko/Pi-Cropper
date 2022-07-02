package com.yatsenko.imagepicker.core

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.ui.PiCropperActivity
import com.yatsenko.imagepicker.ui.PiCropperFragment

class PiCropper private constructor(private val context: Context) {

    companion object {

        fun builder(context: Context): PiCropper.Builder {
            return PiCropper(context).Builder()
        }

    }

    private var aspectRatio: List<AspectRatio> = AspectRatio.defaultList
    private var collectCount = 1
    private var allImagesFolder: String? = null
    private var quality = 80
    private var circleCrop = false
    private var forceOpenEditor = false

    fun intent(): Intent {
        val options = PiCropperFragment.prepareOptions(
            aspectRatio,
            collectCount,
            allImagesFolder,
            circleCrop,
            forceOpenEditor
        )
        return PiCropperActivity.intent(context, options)
    }

    inner class Builder {

        fun theme(theme: Map<String, Int>): Builder {
            Theme.theme = theme
            return this
        }

        fun collectCount(@IntRange(from = 1) collectCount: Int): Builder {
            this@PiCropper.collectCount = if (collectCount < 1) 1 else collectCount
            return this
        }

        fun aspectRatio(list: List<AspectRatio>): Builder {
            this@PiCropper.aspectRatio = list
            return this
        }

        fun circleCrop(circleCrop: Boolean): Builder {
            this@PiCropper.circleCrop = circleCrop
            return this
        }

        fun forceOpenEditor(forceOpenEditor: Boolean): Builder {
            this@PiCropper.forceOpenEditor = forceOpenEditor
            return this
        }

        fun allImagesFolder(name: String): Builder {
            this@PiCropper.allImagesFolder = name
            return this
        }

        fun quality(@IntRange(from = 1, to = 100) quality: Int): Builder {
            this@PiCropper.quality = quality
            return this
        }

        //jpg, png
//        fun exportFormat(format: String): Builder {
//            this@PiCropper.quality = quality
//            return this
//        }

        fun intent(): Intent {
            return this@PiCropper.intent()
        }

    }

}