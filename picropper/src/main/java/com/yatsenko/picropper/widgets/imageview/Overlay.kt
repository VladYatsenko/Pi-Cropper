package com.yatsenko.picropper.widgets.imageview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yatsenko.picropper.R
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.utils.extensions.*
import com.yatsenko.picropper.utils.extensions.Animations.slideDown
import com.yatsenko.picropper.utils.extensions.Animations.slideUp
import com.yatsenko.picropper.utils.extensions.applyMargin
import com.yatsenko.picropper.widgets.checkbox.CheckBox2

class Overlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    companion object {

        fun create(context: Context): Overlay {
            val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return Overlay(context).apply {
                layoutParams = lp
            }
        }

    }

    private var isOverlayVisible = false

    private val back: ImageView
    private val checkbox: CheckBox2
    private val crop: ImageView
    private val brush: ImageView

    private val fileName: TextView
    private val info: TextView
    private val doneFab: FloatingActionButton

    private val top: View
    private val bottom: View

    internal var data: Data? = null
        set(value) {
            val checkboxAnimation = value?.image?.shouldAnimate?.invoke(field?.image) == true
            val overlayAnimation = value?.image?.hideInViewer != field?.image?.hideInViewer
            field = value
            refreshLayout(checkboxAnimation, overlayAnimation)
        }

    internal var result: (AdapterResult) -> Unit = {}

    init {
        inflate(context, R.layout.view_overlay, this)

        back = findViewById(R.id.back)
        checkbox = findViewById(R.id.checkbox)
        checkbox.setOnClickListener {
            val media = data?.image ?: return@setOnClickListener
            result(AdapterResult.OnSelectImageClicked(media))
        }
        crop = findViewById(R.id.crop)
        brush = findViewById(R.id.brush)

        fileName = findViewById(R.id.fileName)
        info = findViewById(R.id.info)

        top = findViewById(R.id.top)
        bottom = findViewById(R.id.bottom)

        back.setOnClickListener {
            result(AdapterResult.GoBack)
        }
        crop.setOnClickListener {
            val image = data?.image ?: return@setOnClickListener
            val hasCroppedImage = (image as? Media.Image)?.hasCroppedImage ?: false
            if (!hasCroppedImage)
                result(AdapterResult.OnCropImageClicked(image))
        }
        brush.setOnClickListener {}

        doneFab = findViewById(R.id.doneFab)
        doneFab.applyTheming()
        doneFab.setOnClickListener {
            val image = data?.image ?: return@setOnClickListener
            result(AdapterResult.OnProvideImageClicked(image))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        top.updateMargin(top = context.actionBarSize)
        bottom.applyMargin(bottom = context.navigationBarSize)
    }

    private fun refreshLayout(checkboxAnimation: Boolean = false, overlayAnimation: Boolean = false) {
        data?.let { data ->
            val media = data.image
            checkbox.setChecked(media.indexInResult, media.isSelected, checkboxAnimation)
        }
        checkbox.isGone = data?.single == true
        doneFab.isVisible = data?.single == true
        fileName.text = data?.image?.name
        info.text = data?.image?.let {
            "${it.width}x${it.height} • ${FileUtils.stringFileSize(it.size)}"
        }

        val hasCroppedImage = (data?.image as? Media.Image)?.hasCroppedImage == true
        crop.imageTintList = ColorStateList.valueOf(if (hasCroppedImage) ContextCompat.getColor(context, R.color.silver_chalice) else Color.WHITE)

        if (overlayAnimation) {
            if (data?.image?.hideInViewer == true) {
                hide()
            } else {
                show()
            }
        }
    }

    fun show() {
        if (!isOverlayVisible) {
            isOverlayVisible = true

            this.animate()?.setDuration(500)?.alpha(1f)?.start()
            top.slideDown((top.height + context.actionBarSize) * -1f, 0f,)
            bottom.slideUp(bottom.height + context.navigationBarSize.toFloat(), 0f)
        }
    }

    fun hide() {
        if (isOverlayVisible) {
            isOverlayVisible = false

            this.animate()?.setDuration(200)?.alpha(0f)?.start()
            top.slideUp(0f, (top.height + context.actionBarSize) * -1f)
            bottom.slideDown(0f, bottom.height + context.navigationBarSize.toFloat())
        }
    }

    internal data class Data(
        val image: Media,
        val single: Boolean = false
    ) {
        companion object {
            fun createFrom(media: Media?, single: Boolean): Data? {
                return media?.let { Data(it, single) }
            }
        }
    }

}