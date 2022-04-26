package com.yatsenko.imagepicker.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.model.Image

fun ImageView.loadImage(image: Image?) {
    Glide.with(this)
        .load(image?.imagePath)
        .into(this)
}