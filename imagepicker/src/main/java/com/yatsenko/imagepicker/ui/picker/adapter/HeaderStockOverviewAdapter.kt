package com.yatsenko.imagepicker.ui.picker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.core.Theme
import com.yatsenko.imagepicker.model.Folder

internal class HeaderStockOverviewAdapter(
    context: Context,
    @LayoutRes
    resource: Int = R.layout.item_folder,
    objects: List<Folder>
) : ArrayAdapter<Folder>(context, resource, objects) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getRowView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View? {
        val view = convertView ?: inflater.inflate(R.layout.item_folder, parent, false)

        val rowItem: Folder? = getItem(position)
        view.findViewById<TextView>(R.id.folderNameTxt).apply {
            text = rowItem?.name
            setTextColor(Theme.themedColor(context, Theme.toolbarContentColor))
        }
        return view
    }

    private fun getRowView(position: Int, convertView: View?, @Nullable parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_folder_row, parent, false)

        val rowItem: Folder? = getItem(position)
        view.findViewById<TextView>(R.id.folderNameTxt).apply {
            text = rowItem?.name
            setTextColor(Theme.themedColor(context, Theme.toolbarContentColor))
        }
        view.findViewById<TextView>(R.id.imageContTxt).text = rowItem?.count.toString()

        return view
    }

}