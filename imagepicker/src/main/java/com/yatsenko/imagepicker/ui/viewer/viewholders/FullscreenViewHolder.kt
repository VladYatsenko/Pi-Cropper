package com.yatsenko.imagepicker.ui.viewer.viewholders

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.utils.transition.TransitionEnd
import com.yatsenko.imagepicker.utils.transition.TransitionStart

abstract class FullscreenViewHolder(view: View): RecyclerView.ViewHolder(view), TransitionStart, TransitionEnd {

    override val viewGroup: ViewGroup
        get() = itemView as ViewGroup

    abstract var data: Media?

    abstract var endView: View

}