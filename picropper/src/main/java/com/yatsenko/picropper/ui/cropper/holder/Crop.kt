package com.yatsenko.picropper.ui.cropper.holder

import android.view.View
import androidx.lifecycle.LifecycleEventObserver
import com.yatsenko.picropper.model.AspectRatio

interface Crop: LifecycleEventObserver {

    val cropView: View

    fun load()

    fun onRotateStart()

    fun onRotate(angle: Float)

    fun onRotateEnd()

    fun onResetRotation()

    fun applyRatio(aspectRatio: AspectRatio, isDynamic: Boolean)

    fun crop()

    fun cancelCrop()

    fun exitCrop(): Boolean

}