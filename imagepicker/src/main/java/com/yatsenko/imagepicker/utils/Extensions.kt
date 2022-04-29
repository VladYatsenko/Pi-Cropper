package com.yatsenko.imagepicker.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Image

fun TextView.checkboxPosition(image: Image?, single: Boolean, result: (AdapterResult) -> Unit) {
    isGone = single
    text = image?.index
    val circle = if (image?.isSelected == true) R.drawable.circle_selected else R.drawable.circle
    background = ContextCompat.getDrawable(context, circle)

    setOnClickListener {
        val img = image ?: return@setOnClickListener
        result(AdapterResult.OnSelectImageClicked(img))
    }
}

fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics)
}