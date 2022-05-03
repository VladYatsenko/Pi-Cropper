package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.viewer.core.Photo

abstract class FullscreenViewHolder(view: View): RecyclerView.ViewHolder(view) {

    abstract var data: Media?

    abstract var endView: View

    open fun afterTransitionStart() {}

    abstract fun beforeTransitionStart(startView: View?)

    abstract fun beforeTransitionEnd(startView: View?)

    abstract fun transitionStart()

    abstract fun transitionEnd(startView: View?)

    abstract fun fade(startView: View? = null)
}