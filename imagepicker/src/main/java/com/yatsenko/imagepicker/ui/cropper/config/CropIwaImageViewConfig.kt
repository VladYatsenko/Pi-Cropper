package com.yatsenko.imagepicker.ui.cropper.config

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.FloatRange
import com.yatsenko.imagepicker.R
import java.util.ArrayList

class CropIwaImageViewConfig {

    companion object {
        private val DEFAULT_MIN_SCALE = 0.0f
        private val DEFAULT_MAX_SCALE = 10f

        val SCALE_UNSPECIFIED = -0.1f

        @SuppressLint("Range")
        fun createDefault(): CropIwaImageViewConfig {
            return CropIwaImageViewConfig()
                .setMaxScale(DEFAULT_MAX_SCALE)
                .setMinScale(DEFAULT_MIN_SCALE)
                .setImageTranslationEnabled(true)
                .setImageScaleEnabled(true)
                .setScale(SCALE_UNSPECIFIED)
        }

        fun createFromAttributes(c: Context, attrs: AttributeSet?): CropIwaImageViewConfig {
            val config = createDefault()
            if (attrs == null) {
                return config
            }
            val ta = c.obtainStyledAttributes(attrs, R.styleable.CropIwaView)
            try {
                config.setMaxScale(ta.getFloat(R.styleable.CropIwaView_ci_max_scale, config.maxScale))
                config.setImageTranslationEnabled(ta.getBoolean(R.styleable.CropIwaView_ci_translation_enabled, config.isTranslationEnabled))
                config.setImageScaleEnabled(ta.getBoolean(R.styleable.CropIwaView_ci_scale_enabled, config.isScaleEnabled))
                config.setImageInitialPosition(
                    InitialPosition.values()[ta.getInt(R.styleable.CropIwaView_ci_initial_position, 0)]
                )
            } finally {
                ta.recycle()
            }
            return config
        }
    }

    var maxScale = 0f
        private set
    var minScale = 0f
        private set
    var isScaleEnabled = false
        private set
    var isTranslationEnabled = false
        private set
    var scale = 0f
        private set

    var initialPosition: InitialPosition = InitialPosition.CENTER_INSIDE
        private set

    private var configChangeListeners: MutableList<ConfigChangeListener> = ArrayList()

    fun setMinScale(@FloatRange(from = 0.001) minScale: Float): CropIwaImageViewConfig {
        this.minScale = minScale
        return this
    }

    fun setMaxScale(@FloatRange(from = 0.001) maxScale: Float): CropIwaImageViewConfig {
        this.maxScale = maxScale
        return this
    }

    fun setImageScaleEnabled(scaleEnabled: Boolean): CropIwaImageViewConfig {
        isScaleEnabled = scaleEnabled
        return this
    }

    fun setImageTranslationEnabled(imageTranslationEnabled: Boolean): CropIwaImageViewConfig {
        isTranslationEnabled = imageTranslationEnabled
        return this
    }

    fun setImageInitialPosition(initialPosition: InitialPosition): CropIwaImageViewConfig {
        this.initialPosition = initialPosition
        return this
    }

    fun setScale(@FloatRange(from = 0.01, to = 1.0) scale: Float): CropIwaImageViewConfig {
        this.scale = scale
        return this
    }

    fun addConfigChangeListener(configChangeListener: ConfigChangeListener) {
        configChangeListeners.add(configChangeListener)
    }

    fun removeConfigChangeListener(configChangeListener: ConfigChangeListener) {
        configChangeListeners.remove(configChangeListener)
    }

    fun apply() {
        configChangeListeners.forEach { listener ->
            listener.onConfigChanged()
        }
    }


}