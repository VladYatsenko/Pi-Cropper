package com.yatsenko.imagepicker

import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MediaAdapter: RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    private val list: MutableList<Uri> = mutableListOf()

    fun submitList(list: List<Uri>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(viewGroup)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(list[position])
    }

    override fun getItemCount() = list.size

    class ViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {
        companion object {
            fun create(viewGroup: ViewGroup): ViewHolder {
                return ViewHolder(ImageView(viewGroup.context).apply {
                    adjustViewBounds = true
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })
            }
        }

        fun bind(url: Uri) {
            Glide.with(view)
                .load(url)
                .apply(RequestOptions().dontTransform())
                .into(view)
        }
    }

}