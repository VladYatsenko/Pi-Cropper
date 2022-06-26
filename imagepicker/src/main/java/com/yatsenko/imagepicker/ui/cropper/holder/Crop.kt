package com.yatsenko.imagepicker.ui.cropper.holder

import android.view.View
import androidx.lifecycle.LifecycleEventObserver
import com.yatsenko.imagepicker.model.AspectRatio

interface Crop: LifecycleEventObserver {

    val cropView: View

    fun onRotateStart()

    fun onRotate(angle: Float)

    fun onRotateEnd()

    fun onResetRotation()

    fun applyRatio(aspectRatio: AspectRatio, isDynamic: Boolean)

    fun crop()

    fun cancelCrop()

}