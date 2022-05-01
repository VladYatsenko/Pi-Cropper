package com.yatsenko.imagepicker.ui.viewer

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.viewer.listeners.ImageLoader
import com.yatsenko.imagepicker.ui.viewer.model.BuilderData
import com.yatsenko.imagepicker.ui.viewer.dialog.ImageViewerDialog
import com.yatsenko.imagepicker.ui.viewer.listeners.OnDismissListener
import com.yatsenko.imagepicker.ui.viewer.listeners.OnImageChangeListener
import kotlin.math.roundToInt

class ImageViewer<T>(
    private val context: Context,
    private val builderData: BuilderData<T>
) {

    private val dialog: ImageViewerDialog<T> = ImageViewerDialog(context, builderData)
    var currentPosition: Int
        get() = dialog.getCurrentPosition()
        set(value) {
            dialog.setCurrentPosition(value)
        }

    fun show() = show(true)

    fun show(animate: Boolean) {
        if (builderData.images.isNotEmpty())
            dialog.show(animate)
        else
            Log.w(
                context.getString(R.string.app_name),
                "Images list cannot be empty! Viewer ignored."
            )
    }


    fun close() = dialog.close()

    fun dismiss() = dialog.dismiss()
    
    fun updateImages(images: List<T>) {
        if (images.isNotEmpty()) {
            dialog.updateImages(images)
        } else {
            dialog.close()
        }
    }

    fun updateTransitionImage(imageView: ImageView?) {
        dialog.updateTransitionImage(imageView)
    }

    fun resetScale() = dialog.resetScale()

    companion object {

        class Builder<T>(
            private val context: Context,
            private val images: List<T>,
            private val imageLoader: ImageLoader<T>
        ) {

            private val data: BuilderData<T> = BuilderData(images, imageLoader)

            /**
             * Sets a position to start viewer from.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withStartPosition(position: Int): Builder<T> {
                data.startPosition = position
                return this
            }

            /**
             * Sets a background color value for the viewer
             *
             * @return This Builder object to allow calls chaining
             */
            fun withBackgroundColor(@ColorInt color: Int): Builder<T> {
                data.backgroundColor = color
                return this
            }

            /**
             * Sets a background color resource for the viewer
             *
             * @return This Builder object to allow calls chaining
             */
            fun withBackgroundColorResource(@ColorRes color: Int): Builder<T> {
                return withBackgroundColor(ContextCompat.getColor(context, color))
            }

            /**
             * Sets custom overlay view to be shown over the viewer.
             * Commonly used for image description or counter displaying.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withOverlayView(view: View?): Builder<T> {
                data.overlayView = view
                return this
            }

            /**
             * Sets space between the images using dimension.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withImagesMargin(@DimenRes dimen: Int): Builder<T> {
                data.imageMarginPixels = context.resources.getDimension(dimen).roundToInt()
                return this
            }

            /**
             * Sets space between the images in pixels.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withImageMarginPixels(marginPixels: Int): Builder<T> {
                data.imageMarginPixels = marginPixels
                return this
            }

            /**
             * Sets overall padding for zooming and scrolling area using dimension.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withContainerPadding(@DimenRes padding: Int): Builder<T> {
                val paddingPx = context.resources.getDimension(padding).roundToInt()
                return withContainerPaddingPixels(paddingPx, paddingPx, paddingPx, paddingPx)
            }

            /**
             * Sets `start`, `top`, `end` and `bottom` padding for zooming and scrolling area using dimension.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withContainerPadding(
                @DimenRes start: Int, @DimenRes top: Int,
                @DimenRes end: Int, @DimenRes bottom: Int
            ): Builder<T> {
                withContainerPaddingPixels(
                    context.resources.getDimension(start).roundToInt(),
                    context.resources.getDimension(top).roundToInt(),
                    context.resources.getDimension(end).roundToInt(),
                    context.resources.getDimension(bottom).roundToInt()
                )
                return this
            }

            /**
             * Sets overall padding for zooming and scrolling area in pixels.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withContainerPaddingPixels(@Px padding: Int): Builder<T> {
                data.containerPaddingPixels = intArrayOf(padding, padding, padding, padding)
                return this
            }

            /**
             * Sets `start`, `top`, `end` and `bottom` padding for zooming and scrolling area in pixels.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withContainerPaddingPixels(
                start: Int,
                top: Int,
                end: Int,
                bottom: Int
            ): Builder<T> {
                data.containerPaddingPixels = intArrayOf(start, top, end, bottom)
                return this
            }

            /**
             * Sets status bar visibility. True by default.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withHiddenStatusBar(value: Boolean): Builder<T> {
                data.shouldStatusBarHide = value
                return this
            }

            /**
             * Enables or disables zooming. True by default.
             *
             * @return This Builder object to allow calls chaining
             */
            fun allowZooming(value: Boolean): Builder<T> {
                data.isZoomingAllowed = value
                return this
            }

            /**
             * Enables or disables the "Swipe to Dismiss" gesture. True by default.
             *
             * @return This Builder object to allow calls chaining
             */
            fun allowSwipeToDismiss(value: Boolean): Builder<T> {
                data.isSwipeToDismissAllowed = value
                return this
            }

            /**
             * Sets a target [ImageView] to be part of transition when opening or closing the viewer/
             *
             * @return This Builder object to allow calls chaining
             */
            fun withTransitionFrom(imageView: ImageView?): Builder<T> {
                data.transitionView = imageView
                return this
            }

            /**
             * Sets [OnImageChangeListener] for the viewer.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withImageChangeListener(imageChangeListener: OnImageChangeListener): Builder<T> {
                data.imageChangeListener = imageChangeListener
                return this
            }

            /**
             * Sets [OnDismissListener] for viewer.
             *
             * @return This Builder object to allow calls chaining
             */
            fun withDismissListener(onDismissListener: OnDismissListener): Builder<T> {
                data.onDismissListener = onDismissListener
                return this
            }

            /**
             * Creates a [ImageViewer] with the arguments supplied to this builder. It does not
             * show the dialog. This allows the user to do any extra processing
             * before displaying the dialog. Use [.show] if you don't have any other processing
             * to do and want this to be created and displayed.
             */
            fun build(): ImageViewer<T> {
                return ImageViewer(context, data)
            }

            /**
             * Creates the [ImageViewer] with the arguments supplied to this builder and
             * shows the dialog.
             */
            fun show(): ImageViewer<T> {
                return show(true)
            }

            /**
             * Creates the [ImageViewer] with the arguments supplied to this builder and
             * shows the dialog.
             *
             * @param animate whether the passed transition view should be animated on open. Useful for screen rotation handling.
             */
            fun show(animate: Boolean): ImageViewer<T> {
                val viewer = build()
                viewer.show(animate)
                return viewer
            }
        }

    }

}