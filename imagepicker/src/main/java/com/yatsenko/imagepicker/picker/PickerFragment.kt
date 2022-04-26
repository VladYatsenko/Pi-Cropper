package com.yatsenko.imagepicker.picker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.picker.adapter.ImageGripAdapter
import com.yatsenko.imagepicker.picker.adapter.ImageViewHolder
import com.yatsenko.imagepicker.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.utils.BaseFragment
import com.yatsenko.imagepicker.utils.PermissionHelper
import com.yatsenko.imagepicker.viewer.ImageViewer
import com.yatsenko.imagepicker.widgets.DropdownToolbar

class PickerFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_picker

    private lateinit var toolbar: DropdownToolbar
    private lateinit var recycler: RecyclerView

    private val imageAdapter by lazy { ImageGripAdapter(false) }

    private val viewModel: PickerViewModel by viewModels { ViewModelFactory(requireActivity().application) }

    private val permissionHelper by lazy { PermissionHelper(this, viewModel::extractImages) }

    private val imageViewer by lazy { ImageViewer(requireActivity(), false, ::handleAdapterResult) }

    private val transitionImageView: (position: Int) -> ImageView? = {
        (recycler.findViewHolderForAdapterPosition(it) as? ImageViewHolder)?.transitionImageView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dropdown_toolbar)
        recycler = view.findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        recycler.adapter = imageAdapter

        toolbar.result = ::handleAdapterResult
        imageAdapter.result = ::handleAdapterResult

        viewModel.state.observe(this) {
            toolbar.data = DropdownToolbar.Data(it.selectedFolder, it.folders)
            imageAdapter.submitList(it.images)
        }

        permissionHelper.checkPermission()
    }

    private fun handleAdapterResult(result: AdapterResult) {
        when (result) {
            is AdapterResult.FolderChanged -> viewModel.changeFolder(result.folder)
            AdapterResult.GoBack -> requireActivity().onBackPressed()
            is AdapterResult.OnImageClicked -> {
                imageViewer.open(
                    images = imageAdapter.list,
                    position = result.position,
                    transitionImage = transitionImageView
                )
            }
            is AdapterResult.OnSelectImageClicked -> viewModel.selectImage(result.image)
        }
    }

}