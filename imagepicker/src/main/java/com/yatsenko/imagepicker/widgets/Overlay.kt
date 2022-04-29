package com.yatsenko.imagepicker.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.utils.checkboxPosition

class Overlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val back: ImageView
    private val position: TextView
    private val crop: ImageView
    private val brush: ImageView

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

        back.setOnClickListener {
            result(AdapterResult.GoBack)
        }
        crop.setOnClickListener {
//            viewer.resetScale()
        }
        brush.setOnClickListener {}
    }

    private fun refreshLayout() {
        data?.let {
            position.checkboxPosition(it.image, it.single) { adapterResult ->
                result(adapterResult)
            }
        }
    }

    data class Data(
        val image: Image,
        val single: Boolean
    )

}