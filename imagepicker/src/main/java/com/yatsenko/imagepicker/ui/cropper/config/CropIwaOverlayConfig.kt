package com.yatsenko.imagepicker.ui.cropper.config

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaOvalShape
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaRectShape
import com.yatsenko.imagepicker.ui.cropper.shape.CropIwaShape
import java.util.ArrayList

class CropIwaOverlayConfig {

    companion object {

        private const val DEFAULT_CROP_SCALE = 0.8f

        fun createDefault(context: Context): CropIwaOverlayConfig {
            val config: CropIwaOverlayConfig = CropIwaOverlayConfig()
                .setBorderColor(ContextCompat.getColor(context, R.color.cropiwa_default_border_color))
                .setCornerColor(ContextCompat.getColor(context, R.color.cropiwa_default_corner_color))
                .setGridColor(ContextCompat.getColor(context, R.color.cropiwa_default_grid_color))
                .setOverlayColor(ContextCompat.getColor(context, R.color.cropiwa_default_overlay_color))
                .setBorderStrokeWidth(context.resources.getDimensionPixelSize(R.dimen.cropiwa_default_border_stroke_width))
                .setCornerStrokeWidth(context.resources.getDimension(R.dimen.cropiwa_default_corner_stroke_width))
                .setCropScale(DEFAULT_CROP_SCALE)
                .setGridStrokeWidth(context.resources.getDimension(R.dimen.cropiwa_default_grid_stroke_width))
                .setMinWidth(context.resources.getDimensionPixelSize(R.dimen.cropiwa_default_min_width))
                .setMinHeight(context.resources.getDimensionPixelSize(R.dimen.cropiwa_default_min_height))
                .setAspectRatio(AspectRatio.AspectOriginal(2, 1))
                .setShouldDrawGrid(true)
                .setDynamicCrop(true)
            val shape: CropIwaShape = CropIwaRectShape(config)
            config.setCropShape(shape)
            return config
        }

        fun createFromAttributes(context: Context, attrs: AttributeSet?): CropIwaOverlayConfig {
            val c = createDefault(context)
            if (attrs == null) {
                return c
            }
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CropIwaView)
            try {
                c.setMinWidth(
                    ta.getDimensionPixelSize(
                        R.styleable.CropIwaView_ci_min_crop_width,
                        c.getMinWidth()
                    )
                )
                c.setMinHeight(
                    ta.getDimensionPixelSize(
                        R.styleable.CropIwaView_ci_min_crop_height,
                        c.getMinHeight()
                    )
                )
                c.setAspectRatio(
                    AspectRatio.AspectOriginal(
                        ta.getInteger(R.styleable.CropIwaView_ci_aspect_ratio_w, 1),
                        ta.getInteger(R.styleable.CropIwaView_ci_aspect_ratio_h, 1)
                    )
                )
                c.setCropScale(
                    ta.getFloat(
                        R.styleable.CropIwaView_ci_crop_scale,
                        c.getCropScale()
                    )
                )
                c.setBorderColor(
                    ta.getColor(
                        R.styleable.CropIwaView_ci_border_color,
                        c.getBorderColor()
                    )
                )
                c.setBorderStrokeWidth(
                    ta.getDimensionPixelSize(
                        R.styleable.CropIwaView_ci_border_width,
                        c.getBorderStrokeWidth()
                    )
                )
                c.setCornerColor(
                    ta.getColor(
                        R.styleable.CropIwaView_ci_corner_color,
                        c.getCornerColor()
                    )
                )
                c.setCornerStrokeWidth(
                    ta.getDimension(
                        R.styleable.CropIwaView_ci_corner_width,
                        c.getCornerStrokeWidth()
                    )
                )
                c.setGridColor(
                    ta.getColor(
                        R.styleable.CropIwaView_ci_grid_color,
                        c.getGridColor()
                    )
                )
                c.setGridStrokeWidth(
                    ta.getDimension(
                        R.styleable.CropIwaView_ci_grid_width,
                        c.getGridStrokeWidth()
                    )
                )
                c.setShouldDrawGrid(
                    ta.getBoolean(
                        R.styleable.CropIwaView_ci_draw_grid,
                        c.shouldDrawGrid()
                    )
                )
                c.setOverlayColor(
                    ta.getColor(
                        R.styleable.CropIwaView_ci_overlay_color,
                        c.getOverlayColor()
                    )
                )
                c.setCropShape(if (ta.getInt(R.styleable.CropIwaView_ci_crop_shape, 0) == 0) CropIwaRectShape(c) else CropIwaOvalShape(c))
                c.setDynamicCrop(
                    ta.getBoolean(
                        R.styleable.CropIwaView_ci_dynamic_aspect_ratio,
                        c.isDynamicCrop()
                    )
                )
            } finally {
                ta.recycle()
            }
            return c
        }

    }

    private var overlayColor = 0

    private var borderColor = 0
    private var cornerColor = 0
    private var gridColor = 0
    private var borderStrokeWidth = 0

    private var cornerStrokeWidth = 0f
    private var gridStrokeWidth = 0f

    private var minHeight = 0
    private var minWidth = 0

    private var aspectRatio: AspectRatio = AspectRatio.IMG_SRC

    private var cropScale = 0f

    private var isDynamicCrop = false
    private var shouldDrawGrid = false
    private var cropShape: CropIwaShape? = null

    private var listeners: MutableList<ConfigChangeListener> = ArrayList()
    private var iterationList: MutableList<ConfigChangeListener> = ArrayList()

    fun getOverlayColor(): Int {
        return overlayColor
    }

    fun getBorderColor(): Int {
        return borderColor
    }

    fun getCornerColor(): Int {
        return cornerColor
    }

    fun getBorderStrokeWidth(): Int {
        return borderStrokeWidth
    }

    fun getCornerStrokeWidth(): Float {
        return cornerStrokeWidth
    }

    fun getMinHeight(): Int {
        return minHeight
    }

    fun getMinWidth(): Int {
        return minWidth
    }

    fun getGridColor(): Int {
        return gridColor
    }

    fun getGridStrokeWidth(): Float {
        return gridStrokeWidth
    }

    fun shouldDrawGrid(): Boolean {
        return shouldDrawGrid
    }

    fun getCropShape(): CropIwaShape {
        return cropShape!!
    }

    fun isDynamicCrop(): Boolean {
        return isDynamicCrop
    }

    fun getCropScale(): Float {
        return cropScale
    }

    fun getAspectRatio(): AspectRatio {
        return aspectRatio
    }

    fun setOverlayColor(overlayColor: Int): CropIwaOverlayConfig {
        this.overlayColor = overlayColor
        return this
    }

    fun setBorderColor(borderColor: Int): CropIwaOverlayConfig {
        this.borderColor = borderColor
        return this
    }

    fun setCornerColor(cornerColor: Int): CropIwaOverlayConfig {
        this.cornerColor = cornerColor
        return this
    }

    fun setGridColor(gridColor: Int): CropIwaOverlayConfig {
        this.gridColor = gridColor
        return this
    }

    fun setBorderStrokeWidth(borderStrokeWidth: Int): CropIwaOverlayConfig {
        this.borderStrokeWidth = borderStrokeWidth
        return this
    }

    fun setCornerStrokeWidth(cornerStrokeWidth: Float): CropIwaOverlayConfig {
        this.cornerStrokeWidth = cornerStrokeWidth
        return this
    }

    fun setGridStrokeWidth(gridStrokeWidth: Float): CropIwaOverlayConfig {
        this.gridStrokeWidth = gridStrokeWidth
        return this
    }

    fun setCropScale(@FloatRange(from = 0.01, to = 1.0) cropScale: Float): CropIwaOverlayConfig {
        this.cropScale = cropScale
        return this
    }

    fun setMinHeight(minHeight: Int): CropIwaOverlayConfig {
        this.minHeight = minHeight
        return this
    }

    fun setMinWidth(minWidth: Int): CropIwaOverlayConfig {
        this.minWidth = minWidth
        return this
    }

    fun setAspectRatio(ratio: AspectRatio): CropIwaOverlayConfig {
        aspectRatio = ratio
        return this
    }

    fun setShouldDrawGrid(shouldDrawGrid: Boolean): CropIwaOverlayConfig {
        this.shouldDrawGrid = shouldDrawGrid
        return this
    }

    fun setCropShape(cropShape: CropIwaShape): CropIwaOverlayConfig {
        if (this.cropShape != null) {
            removeConfigChangeListener(this.cropShape)
        }
        this.cropShape = cropShape
        return this
    }

    fun setDynamicCrop(enabled: Boolean): CropIwaOverlayConfig {
        isDynamicCrop = enabled
        return this
    }

    fun addConfigChangeListener(listener: ConfigChangeListener?) {
        if (listener != null) {
            listeners.add(listener)
        }
    }

    fun removeConfigChangeListener(listener: ConfigChangeListener?) {
        listeners.remove(listener)
    }

    fun apply() {
        iterationList.addAll(listeners)
        for (listener in iterationList) {
            listener.onConfigChanged()
        }
        iterationList.clear()
    }
}