package com.yatsenko.imagepicker.ui.cropper.holder

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.OverlayView.FREESTYLE_CROP_MODE_DISABLE
import com.yalantis.ucrop.view.OverlayView.FREESTYLE_CROP_MODE_ENABLE
import com.yalantis.ucrop.view.TransformImageView.TransformImageListener
import com.yalantis.ucrop.view.UCropView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Arguments
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.utils.extensions.FileUtils
import com.yatsenko.imagepicker.utils.extensions.FileUtils.fileUri
import com.yatsenko.imagepicker.utils.extensions.actionBarSize
import kotlinx.android.synthetic.main.layout_crop_tools.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class UCropIml(
    private val context: Context,
    private val inputUri: Uri,
    arguments: Arguments,
    private val result: (AdapterResult) -> Unit) : Crop {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private var cropJob: Job? = null

    private var internalResult: (AdapterResult) -> Unit = { result(it) }

    override val cropView = UCropView(context, null).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private val gestureCropImageView = cropView.cropImageView
    private val overlayView = cropView.overlayView

    private val compressFormat = arguments.bitmapCompressFormat
    private val compressQuality = arguments.quality

    private val outputFile = FileUtils.tempFile(context, arguments.compressFormatExtension)

    private var initScale = -1f
    private var wasCropped = false

    init {
        gestureCropImageView.isRotateEnabled = false
        overlayView.setCircleDimmedLayer(arguments.circleCrop)
        gestureCropImageView.setTransformImageListener(object : TransformImageListener {
            override fun onRotate(currentAngle: Float) = internalResult(AdapterResult.OnImageRotated(currentAngle))

            override fun onScale(currentScale: Float) {
                if (initScale == -1f)
                    initScale = gestureCropImageView.currentScale
            }

            override fun onLoadComplete() {
                result(AdapterResult.OnCropImageLoaded)

//                gestureCropImageView.setPadding(
//                    gestureCropImageView.paddingLeft,
//                    gestureCropImageView.paddingTop + context.actionBarSize,
//                    gestureCropImageView.paddingRight,
//                    gestureCropImageView.paddingBottom + context.resources.getDimensionPixelSize(R.dimen.overlap_crop_tools_height)
//                )
            }

            override fun onLoadFailure(e: Exception) = internalResult(AdapterResult.OnCropError(e))
        })

    }

    override fun load() {
        try {
            gestureCropImageView.setImageUri(inputUri, outputFile.fileUri(context))
        } catch (e: Exception) {
            internalResult(AdapterResult.OnCropError(e))
        }
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
        cropJob?.cancel()
        cropJob = scope.launch {
            try {
                gestureCropImageView.cropAndSaveImage(compressFormat, compressQuality, object : BitmapCropCallback {
                    override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
                        wasCropped = true
                        val newMedia = Media.Image.croppedImage(outputFile, imageWidth, imageHeight)
                        internalResult(AdapterResult.OnImageCropped(newMedia))
                    }

                    override fun onCropFailure(t: Throwable) {
                        internalResult(AdapterResult.OnCropError(t))
                    }
                })
            } catch (t: Throwable) {
                internalResult(AdapterResult.OnCropError(t))
            }
        }
    }

    override fun cancelCrop() {
        cropJob?.cancel()
    }

    override fun exitCrop(): Boolean {
        val value = (gestureCropImageView.currentScale != initScale || gestureCropImageView.currentAngle != 0f) && !wasCropped
        onResetRotation()
        gestureCropImageView.zoomOutImage(initScale)

        return value
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event) {
            Lifecycle.Event.ON_STOP -> gestureCropImageView.cancelAllAnimations()
            Lifecycle.Event.ON_DESTROY -> {
                internalResult = {}
                cancelCrop()
            }
        }
    }

}