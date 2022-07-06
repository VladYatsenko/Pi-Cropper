package com.yatsenko.imagepicker.utils.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.yatsenko.imagepicker.ui.picker.adapter.ImageViewHolder

internal fun <T: RecyclerView.ViewHolder> RecyclerView.findViewHolderByAdapterPosition(position: Int): T? {
    return this.findViewHolderForAdapterPosition(position) as? T
}

internal fun <T: RecyclerView.ViewHolder> ViewPager2.findViewHolderByAdapterPosition(position: Int): T? {
    return (getChildAt(0) as? RecyclerView?)?.findViewHolderByAdapterPosition(position)
}