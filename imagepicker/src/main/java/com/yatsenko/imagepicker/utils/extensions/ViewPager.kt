package com.yatsenko.imagepicker.utils.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.yatsenko.imagepicker.ui.picker.adapter.ImageViewHolder

internal fun ViewPager.addOnPageChangeListener(
    onPageScrolled: ((position: Int, offset: Float, offsetPixels: Int) -> Unit)? = null,
    onPageSelected: ((position: Int) -> Unit)? = null,
    onPageScrollStateChanged: ((state: Int) -> Unit)? = null
) = object : ViewPager.OnPageChangeListener {
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels)
    }

    override fun onPageSelected(position: Int) {
        onPageSelected?.invoke(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        onPageScrollStateChanged?.invoke(state)
    }
}.also { addOnPageChangeListener(it) }

internal fun <T: RecyclerView.ViewHolder> RecyclerView.findViewHolderByAdapterPosition(position: Int): T? {
    return this.findViewHolderForAdapterPosition(position) as? T
}

internal fun <T: RecyclerView.ViewHolder> ViewPager2.findViewHolderByAdapterPosition(position: Int): T? {
    return (getChildAt(0) as? RecyclerView?)?.findViewHolderByAdapterPosition(position)
}