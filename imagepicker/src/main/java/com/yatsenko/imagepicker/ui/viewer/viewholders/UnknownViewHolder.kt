package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.View
import android.view.ViewGroup
import com.yatsenko.imagepicker.model.Media

internal class UnknownViewHolder(view: View) : FullscreenViewHolder(view) {

    companion object {
        fun create(parent: ViewGroup) = UnknownViewHolder(View(parent.context))
    }

    override var data: Media? = null
    override var endView: View = view

    override fun beforeTransitionStart(startView: View?) {}

    override fun beforeTransitionEnd(startView: View?) {}

    override fun transitionStart() {}

    override fun transitionEnd(startView: View?) {}

    override fun fade(startView: View?) {}

}