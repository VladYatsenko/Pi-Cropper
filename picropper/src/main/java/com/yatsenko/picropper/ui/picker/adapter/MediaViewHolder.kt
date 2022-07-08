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


internal class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = R.layout.item_image
        fun create(parent: ViewGroup) = MediaViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    private val root = view.findViewById<ConstraintLayout>(R.id.root).apply {
        this.setBackgroundColor(Theme.themedColor(context, Theme.imageBackgroundColor))
    }

    private val thumbnail = view.findViewById<ImageView>(R.id.image)
    private val checkBox2 = view.findViewById<CheckBoxGrid>(R.id.checkbox)
    private val positionContainer = view.findViewById<FrameLayout>(R.id.position_container)

    private var media: Media? = null
    private var anim: Animation? = null

    fun bind(media: Media, single: Boolean, result: (AdapterResult) -> Unit) {
        val animate = media.shouldAnimate(this.media)
        this.media = media

        thumbnail.scaleView(media.isSelected, animate)
        checkBox2.setChecked(media.indexInResult, media.isSelected, animate)
        checkBox2.isGone = single

        positionContainer.setOnClickListener {
            result(AdapterResult.OnSelectImageClicked(media))
        }
        thumbnail.loadImage(media)
        thumbnail.alpha = if (media.hideInGrid || media.hideInViewer) 0f else 1f

        itemView.setOnClickListener {
            result(AdapterResult.OnImageClicked(thumbnail, media, bindingAdapterPosition))
        }

        ViewerTransitionHelper.put(media.id, thumbnail)
    }

    fun refreshTransitionView() {
        val id = media?.id ?: return
        ViewerTransitionHelper.put(id, thumbnail)
    }

    private fun View.scaleView(isSelected: Boolean, animate: Boolean) {
        val selectedMargin = dpToPx(16).toInt()

        anim?.cancel()
        if (animate) {
            var isFinished = false
            anim = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if (!isFinished) {
                        val newMargin = (if (isSelected) (selectedMargin * interpolatedTime) else selectedMargin + ((0 - selectedMargin) * interpolatedTime)).toInt()
                        updateMargin(newMargin, newMargin, newMargin, newMargin)
                        isFinished = interpolatedTime == 1f
                    }
                }
            }
            anim?.fillAfter = true // Needed to keep the result of the animation
            anim?.duration = 200
            this.startAnimation(anim)
        } else {
            val margin = (if (isSelected) selectedMargin else 0).toInt()
            updateMargin(margin, margin, margin, margin)
        }
    }

}