package com.yatsenko.picropper.ui.viewer.viewholders

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.utils.transition.TransitionEnd
import com.yatsenko.picropper.utils.transition.TransitionStart

internal abstract class FullscreenViewHolder(view: View): RecyclerView.ViewHolder(view), TransitionStart, TransitionEnd {

    override val viewGroup: ViewGroup
        get() = itemView as ViewGroup

    abstract var data: Media?

    abstract var endView: View

    open fun resetScale(): Boolean = false

}