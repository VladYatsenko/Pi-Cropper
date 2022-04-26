package com.yatsenko.imagepicker.utils

import android.widget.TextView
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
        result(AdapterResult.OnSelectImageClicked(image ?: return@setOnClickListener))
    }
}