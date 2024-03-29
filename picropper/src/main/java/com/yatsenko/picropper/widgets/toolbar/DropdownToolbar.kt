package com.yatsenko.picropper.widgets.toolbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.yatsenko.picropper.R
import com.yatsenko.picropper.core.Theme
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Folder
import com.yatsenko.picropper.ui.picker.adapter.HeaderStockOverviewAdapter
import com.yatsenko.picropper.utils.extensions.dpToPx

internal class DropdownToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppBarLayout(context, attrs, defStyle) {

    private val toolbar: Toolbar
    private val spinner: Spinner

    internal var data: Data? = null
        set(value) {
            field = value
            refreshLayout()
        }

    private val folders: List<Folder>
        get() = data?.folders ?: emptyList()

    internal var result: (AdapterResult) -> Unit = {}

    private val folderAdapter: HeaderStockOverviewAdapter
        get() = HeaderStockOverviewAdapter(context, objects = folders)

    init {
        inflate(context, R.layout.view_dropdown_toolbar, this)
        ViewCompat.setElevation(this, dpToPx(8))

        toolbar = findViewById(R.id.toolbar)
        toolbar.setBackgroundColor(Theme.themedColor(context, Theme.toolbarColor))
        spinner = findViewById(R.id.spinner)
        spinner.backgroundTintList = ColorStateList.valueOf(Theme.themedColor(context, Theme.toolbarContentColor))
        spinner.setPopupBackgroundDrawable(ColorDrawable(Theme.themedColor(context, Theme.toolbarColor)))
        toolbar.navigationIcon?.setTint(Theme.themedColor(context, Theme.toolbarContentColor))
        toolbar.setNavigationOnClickListener {
            result(AdapterResult.GoBack)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                result(AdapterResult.FolderChanged(folders[position]))
            }
        }
    }

    private fun refreshLayout() {
        spinner.adapter = folderAdapter
        val currentFolder = data?.currentFolder ?: return
        spinner.setSelection(folders.indexOf(currentFolder))
    }

    internal data class Data(
        val currentFolder: Folder,
        val folders: List<Folder>
    )
}