package com.yatsenko.imagepicker.widgets.crop

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.core.Theme
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio

class AspectRatioAdapter: RecyclerView.Adapter<AspectRatioAdapter.ViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<AspectRatioWrapper>() {

        override fun areItemsTheSame(oldItem: AspectRatioWrapper, newItem: AspectRatioWrapper): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: AspectRatioWrapper, newItem: AspectRatioWrapper): Boolean {
            return oldItem === newItem
        }

    }

    private val differ = AsyncListDiffer(this, callback)

    var result: (AdapterResult) -> Unit = {}

    fun submitList(ratios: List<AspectRatioWrapper>) {
        differ.submitList(ArrayList(ratios))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.create(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position], result)
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        companion object {
            fun create(parent: ViewGroup) = ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_aspect_ratio, parent, false)
            )
        }

        private val accentColor = ContextCompat.getColor(view.context, Theme.themedColor(Theme.accentColor))
        private val toolsColor = ContextCompat.getColor(view.context, Theme.themedColor(Theme.toolsColor))

        private val dot: View = view.findViewById(R.id.ratio_dot)
        private val title: TextView = view.findViewById(R.id.ratio_title)

        fun bind(item: AspectRatioWrapper, result: (AdapterResult) -> Unit) {
            val color = if (item.isSelected) accentColor else toolsColor

            dot.isVisible = item.isSelected
            dot.backgroundTintList = ColorStateList.valueOf(color)
            title.text = item.ratio.ratioString

            title.setTextColor(color)

            itemView.setOnClickListener {
                if (item.isSelected) return@setOnClickListener
                result(AdapterResult.OnAspectRatioClicked(item))
            }
        }

    }

}


data class AspectRatioWrapper(
    val ratio: AspectRatio,
    val isSelected: Boolean
) {

    companion object {
        fun createFrom(aspect: AspectRatio) = AspectRatioWrapper(aspect, false)
    }
}