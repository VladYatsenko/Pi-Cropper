package com.yatsenko.imagepicker.ui.cropper.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.yatsenko.imagepicker.ui.cropper.config.ConfigChangeListener
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaImageViewConfig
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaOverlayConfig
import com.yatsenko.imagepicker.ui.cropper.config.CropIwaSaveConfig
import com.yatsenko.imagepicker.ui.cropper.image.CropArea
import com.yatsenko.imagepicker.ui.cropper.image.CropIwaBitmapManager
import com.yatsenko.imagepicker.ui.cropper.image.CropIwaResultReceiver
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaShapeMask
import com.yatsenko.imagepicker.ui.cropper.utils.LoadBitmapCommand

class CropIwaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val overlayConfig = CropIwaOverlayConfig.createFromAttributes(context, attrs).apply {
        addConfigChangeListener(ReInitOverlayOnResizeModeChange())
    }
    private val imageConfig = CropIwaImageViewConfig.createFromAttributes(context, attrs)

    private val imageView = CropIwaImageView(context, imageConfig).apply {
        setBackgroundColor(Color.BLACK)
        this@CropIwaView.addView(this)
    }
    private var overlayView = if (overlayConfig.isDynamicCrop())
        CropIwaDynamicOverlayView(context, overlayConfig)
    else CropIwaOverlayView(context, overlayConfig)

    private val gestureDetector: CropIwaImageView.GestureProcessor
        get() = imageView.gestureDetector

    private var imageUri: Uri? = null
    private var loadBitmapCommand: LoadBitmapCommand? = null

    private var errorListener: ErrorListener? = null
    private var cropSaveCompleteListener: CropSaveCompleteListener? = null

    private val cropIwaResultReceiver = CropIwaResultReceiver().apply {
        register(context)
        setListener(CropResultRouter())
    }

    init {
        initOverlayView()
    }

    private fun initOverlayView() {
        overlayView = if (overlayConfig.isDynamicCrop())
            CropIwaDynamicOverlayView(context, overlayConfig)
        else CropIwaOverlayView(context, overlayConfig)

        overlayView.newBoundsListener = imageView
        imageView.setImagePositionedListener(overlayView)
        addView(overlayView)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        loadBitmapCommand?.let {
            it.setDimensions(w, h)
            it.tryExecute(context)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        //I think this "redundant" if statements improve code readability
        return try {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                gestureDetector.onDown(ev)
                return false
            }
            !(overlayView.isResizing() || overlayView.isDraggingCropArea())
        } catch (e: IllegalArgumentException) {
            //e.printStackTrace();
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return try {
            event?.let { gestureDetector.onTouchEvent(it) }
            super.onTouchEvent(event)
        } catch (e: IllegalArgumentException) {
            //e.printStackTrace();
            false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        imageView.measure(widthMeasureSpec, heightMeasureSpec)
        overlayView.measure(
            imageView.measuredWidthAndState,
            imageView.measuredHeightAndState
        )
        imageView.notifyImagePositioned()
        setMeasuredDimension(
            imageView.measuredWidthAndState,
            imageView.measuredHeightAndState
        )
    }

    override fun invalidate() {
        imageView.invalidate()
        overlayView.invalidate()
    }

    fun configureOverlay(): CropIwaOverlayConfig {
        return overlayConfig
    }

    fun configureImage(): CropIwaImageViewConfig {
        return imageConfig
    }

    fun setImageUri(uri: Uri?) {
        imageUri = uri
        loadBitmapCommand = LoadBitmapCommand(
            uri!!, width, height,
            BitmapLoadListener()
        )
        loadBitmapCommand!!.tryExecute(context)
    }

    fun setImage(bitmap: Bitmap?) {
        imageView.setImageBitmap(bitmap)
        overlayView.shouldDrawOverlay = true
    }

    fun crop(saveConfig: CropIwaSaveConfig?) {
        val cropArea: CropArea = CropArea.create(
            imageView.getImageRect(),
            imageView.getImageRect(),
            overlayView.getCropRec()
        )
        val mask: CropIwaShapeMask? = overlayConfig.getCropShape().getMask()
        CropIwaBitmapManager.instance.crop(
            context, cropArea, mask,
            imageUri, saveConfig
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (imageUri != null) {
            val loader: CropIwaBitmapManager = CropIwaBitmapManager.instance
            loader.unregisterLoadListenerFor(imageUri!!)
            loader.removeIfCached(imageUri)
        }
        cropIwaResultReceiver.unregister(context)
    }

    fun setErrorListener(errorListener: ErrorListener?) {
        this.errorListener = errorListener
    }

    fun setCropSaveCompleteListener(cropSaveCompleteListener: CropSaveCompleteListener?) {
        this.cropSaveCompleteListener = cropSaveCompleteListener
    }

    private inner class BitmapLoadListener : CropIwaBitmapManager.BitmapLoadListener {
        override fun onBitmapLoaded(imageUri: Uri?, bitmap: Bitmap?) {
            setImage(bitmap)
        }

        override fun onLoadFailed(e: Throwable?) {
//            CropIwaLog.e("CropIwa Image loading from [$imageUri] failed", e)
            overlayView.shouldDrawOverlay = false
            if (errorListener != null) {
                errorListener!!.onError(e)
            }
        }
    }

    private inner class CropResultRouter : CropIwaResultReceiver.Listener {
        override fun onCropSuccess(croppedUri: Uri?) {
            if (cropSaveCompleteListener != null) {
                cropSaveCompleteListener!!.onCroppedRegionSaved(croppedUri)
            }
        }

        override fun onCropFailed(e: Throwable?) {
            if (errorListener != null) {
                errorListener!!.onError(e)
            }
        }
    }

    private inner class ReInitOverlayOnResizeModeChange : ConfigChangeListener {
        override fun onConfigChanged() {
            if (shouldReInit()) {
                overlayConfig.removeConfigChangeListener(overlayView)
                val shouldDrawOverlay: Boolean = overlayView.isDrawn()
                removeView(overlayView)
                initOverlayView()
                overlayView.shouldDrawOverlay = shouldDrawOverlay
                invalidate()
            }
        }

        private fun shouldReInit(): Boolean {
            return overlayConfig.isDynamicCrop() !== overlayView is CropIwaDynamicOverlayView
        }
    }

    interface CropSaveCompleteListener {
        fun onCroppedRegionSaved(bitmapUri: Uri?)
    }

    interface ErrorListener {
        fun onError(e: Throwable?)
    }

}