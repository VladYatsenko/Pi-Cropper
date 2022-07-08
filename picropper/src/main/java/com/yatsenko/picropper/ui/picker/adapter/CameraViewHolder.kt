package com.yatsenko.picropper.ui.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.picropper.R
import com.yatsenko.picropper.core.Theme
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.utils.transition.ViewerTransitionHelper
import com.yatsenko.picropper.utils.extensions.dpToPx
import com.yatsenko.picropper.utils.extensions.loadImage
import com.yatsenko.picropper.utils.extensions.updateMargin
import com.yatsenko.picropper.widgets.checkbox.CheckBoxGrid


internal class CameraViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = R.layout.item_camera
        fun create(parent: ViewGroup) = CameraViewHolder(
            LayoutInflater.from(parent.context).inflate(VIEW_TYPE, parent, false)
        )
    }

    init {
        view.findViewById<ConstraintLayout>(R.id.root).apply {
            this.setBackgroundColor(Theme.themedColor(context, Theme.imageBackgroundColor))
        }
    }

    fun bind(result: (AdapterResult) -> Unit) {
        itemView.setOnClickListener {
            result(AdapterResult.TakeFromCamera)
        }
    }

}