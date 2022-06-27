package com.yatsenko.imagepicker.widgets.crop

import android.annotation.SuppressLint
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
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio

class AspectRatioAdapter: RecyclerView.Adapter<AspectRatioAdapter.ViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Data>() {

        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem === newItem
        }

    }

    private val differ = AsyncListDiffer(this, callback)

    var result: (AdapterResult) -> Unit = {}

    fun submitList(ratios: List<Data>) {
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

        private val dot: View = view.findViewById(R.id.ratio_dot)
        private val title: TextView = view.findViewById(R.id.ratio_title)

        fun bind(item: Data, result: (AdapterResult) -> Unit) {
            dot.isVisible = item.isSelected
            title.text = item.ratio.ratioString(itemView.context)

            val color = if (item.isSelected) R.color.cerulean else R.color.wild_sand
            title.setTextColor(ContextCompat.getColor(itemView.context, color))

            itemView.setOnClickListener {
                if (item.isSelected) return@setOnClickListener
                result(AdapterResult.OnAspectRatioClicked(item))
            }
        }

    }

    data class Data(
        val ratio: AspectRatio,
        val isSelected: Boolean
    ) {

        companion object {
            fun createFrom(aspect: AspectRatio) = Data(aspect, false)
        }
    }

}