package com.yatsenko.imagepicker.ui.picker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.ui.picker.adapter.ImageGripAdapter
import com.yatsenko.imagepicker.ui.picker.adapter.ImageViewHolder
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.utils.PermissionHelper
import com.yatsenko.imagepicker.ui.abstraction.BaseChildFragment
import com.yatsenko.imagepicker.widgets.toolbar.DropdownToolbar

class PickerFragment : BaseChildFragment() {

    override val layoutId: Int = R.layout.fragment_picker

    private lateinit var toolbar: DropdownToolbar
    private lateinit var recycler: RecyclerView


    private val viewModel: PickerViewModel by viewModels (ownerProducer = ::requireParentFragment, factoryProducer = { ViewModelFactory(requireActivity().application) })

    private val imageAdapter by lazy { ImageGripAdapter(false) }
    private val permissionHelper by lazy { PermissionHelper(this, viewModel::extractImages) }

    private val transitionImageView: (position: Int) -> ImageView? = {
        (recycler.findViewHolderForAdapterPosition(it) as? ImageViewHolder)?.transitionImageView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dropdown_toolbar)
        recycler = view.findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        recycler.adapter = imageAdapter
        recycler.itemAnimator = null

        toolbar.result = ::handleAdapterResult
        imageAdapter.result = ::handleAdapterResult

        viewModel.state.observe(this) { state ->
            toolbar.data = DropdownToolbar.Data(state.selectedFolder, state.folders)
            imageAdapter.submitList(state.images)
        }
        permissionHelper.checkPermission()
    }

    private fun handleAdapterResult(result: AdapterResult) {
        when (result) {
            AdapterResult.GoBack -> requireActivity().onBackPressed()
            is AdapterResult.FolderChanged -> viewModel.changeFolder(result.folder)
            is AdapterResult.OnImageClicked -> {
            }
            is AdapterResult.ImageLoaded -> startPostponedEnterTransition()
            is AdapterResult.OnSelectImageClicked -> viewModel.selectImage(result.image)
        }
    }

}