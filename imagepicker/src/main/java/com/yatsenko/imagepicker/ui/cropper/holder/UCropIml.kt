package com.yatsenko.imagepicker.ui.cropper.holder

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.OverlayView.FREESTYLE_CROP_MODE_DISABLE
import com.yalantis.ucrop.view.OverlayView.FREESTYLE_CROP_MODE_ENABLE
import com.yalantis.ucrop.view.TransformImageView.TransformImageListener
import com.yalantis.ucrop.view.UCropView
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.utils.extensions.FileUtils
import com.yatsenko.imagepicker.utils.extensions.FileUtils.fileUri

class UCropIml(
    context: Context,
    inputUri: Uri,
    private val result: (AdapterResult) -> Unit) : Crop {

    override val cropView = UCropView(context, null).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private val gestureCropImageView = cropView.cropImageView
    private val overlayView = cropView.overlayView

    private val compressFormat = Bitmap.CompressFormat.JPEG
    private val compressQuality = 80

    private val outputFile = FileUtils.tempFile(context, ".jpg")

    init {
        gestureCropImageView.isRotateEnabled = false
        try {
            gestureCropImageView.setImageUri(inputUri, outputFile.fileUri(context))
        } catch (e: Exception) {
            result(AdapterResult.OnCropError(e))
        }

        gestureCropImageView.setTransformImageListener(object : TransformImageListener {
            override fun onRotate(currentAngle: Float) = result(AdapterResult.OnImageRotated(currentAngle))

            override fun onScale(currentScale: Float) {}

            override fun onLoadComplete() {}

            override fun onLoadFailure(e: Exception) = result(AdapterResult.OnCropError(e))
        })

    }

    override fun onRotateStart() {
        gestureCropImageView.cancelAllAnimations()
    }

    override fun onRotate(angle: Float) {
        gestureCropImageView.postRotate(angle)
        gestureCropImageView.setImageToWrapCropBounds()
    }

    override fun onRotateEnd() {
        gestureCropImageView.setImageToWrapCropBounds()
    }

    override fun onResetRotation() {
        gestureCropImageView.postRotate(-gestureCropImageView.currentAngle)
        gestureCropImageView.setImageToWrapCropBounds()
    }

    override fun applyRatio(aspectRatio: AspectRatio, isDynamic: Boolean) {
        val dynamic = if (isDynamic) FREESTYLE_CROP_MODE_ENABLE else FREESTYLE_CROP_MODE_DISABLE
        overlayView.freestyleCropMode = dynamic
        if (!isDynamic) {
            gestureCropImageView.targetAspectRatio = (aspectRatio.ratio)
            gestureCropImageView.setImageToWrapCropBounds()
        }
    }

    override fun crop() {
        gestureCropImageView.cropAndSaveImage(compressFormat, compressQuality, object : BitmapCropCallback {
            override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
                val newMedia = Media.Image.croppedImage(outputFile, imageWidth, imageHeight)
                result(AdapterResult.OnImageCropped(newMedia))
            }

            override fun onCropFailure(t: Throwable) {
                result(AdapterResult.OnCropError(t))
            }
        })
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event) {
            Lifecycle.Event.ON_STOP -> gestureCropImageView.cancelAllAnimations()
        }
    }
}