package com.yatsenko.imagepicker.widgets.imageview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.utils.extensions.*
import com.yatsenko.imagepicker.utils.extensions.Animations.slideDown
import com.yatsenko.imagepicker.utils.extensions.Animations.slideUp
import com.yatsenko.imagepicker.utils.extensions.EdgeToEdge.updateMargin
import com.yatsenko.imagepicker.utils.extensions.applyMargin

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

    private val back: ImageView
    private val position: TextView
    private val crop: ImageView
    private val brush: ImageView

    private val fileName: TextView
    private val info: TextView

    private val top: View
    private val bottom: View

    var data: Data? = null
        set(value) {
            field = value
            refreshLayout()
        }

    var result: (AdapterResult) -> Unit = {}

    init {
        inflate(context, R.layout.view_overlay, this)

        back = findViewById(R.id.back)
        position = findViewById(R.id.position)
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
            data?.image?.let {
                result(AdapterResult.OnCropImageClicked(it))
            }
        }
        brush.setOnClickListener {}
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        top.updateMargin(top = context.actionBarSize)
        bottom.applyMargin(bottom = context.navigationBarSize)
    }

    private fun refreshLayout() {
        data?.let {
            position.checkboxPosition(it.image, it.single) { adapterResult ->
                result(adapterResult)
            }
        }
        fileName.text = data?.image?.name
        info.text = data?.image?.let {
            "${it.width}x${it.height} • ${FileUtils.stringFileSize(it.size)}"
        }
    }

    fun show() {
        this.animate()?.setDuration(500)?.alpha(1f)?.start()
    }

    fun hide() {
        this.animate()?.setDuration(200)?.alpha(0f)?.start()
        top.slideUp(0f, (top.height + context.actionBarSize) * -1f)
        bottom.slideDown(0f, bottom.height + context.navigationBarSize.toFloat())
    }

    data class Data(
        val image: Media,
        val single: Boolean = false
    ) {
        companion object {
            fun createFrom(media: Media?): Data? {
                return media?.let { Data(it) }
            }
        }
    }

}