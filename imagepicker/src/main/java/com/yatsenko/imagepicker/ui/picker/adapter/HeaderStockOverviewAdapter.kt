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
import com.yatsenko.imagepicker.model.ImageFolderEntity


class HeaderStockOverviewAdapter(context: Context, @LayoutRes resource: Int, objects: ArrayList<ImageFolderEntity>) : ArrayAdapter<ImageFolderEntity>(context, resource, objects) {

    private var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getRowView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View? {
        val view = convertView ?: inflater.inflate(R.layout.item_folder, parent, false)

        val rowItem: ImageFolderEntity? = getItem(position)
        view.findViewById<TextView>(R.id.folderNameTxt).text = rowItem?.folderName
        return view
    }

    private fun getRowView(position: Int, convertView: View?, @Nullable parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_folder_row, parent, false)

        val rowItem: ImageFolderEntity? = getItem(position)
        view.findViewById<TextView>(R.id.folderNameTxt).text = rowItem?.folderName
        view.findViewById<TextView>(R.id.imageContTxt).text = rowItem?.num.toString()

        return view
    }

}