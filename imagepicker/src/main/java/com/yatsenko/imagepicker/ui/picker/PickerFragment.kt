package com.yatsenko.imagepicker.ui.picker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseChildFragment
import com.yatsenko.imagepicker.ui.picker.adapter.ImageGripAdapter
import com.yatsenko.imagepicker.ui.picker.adapter.ImageViewHolder
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.utils.PermissionHelper
import com.yatsenko.imagepicker.utils.extensions.applyTheming
import com.yatsenko.imagepicker.widgets.toolbar.DropdownToolbar

class PickerFragment : BaseChildFragment() {

    override val layoutId: Int = R.layout.fragment_picker

    private lateinit var toolbar: DropdownToolbar
    private lateinit var recycler: RecyclerView
    private lateinit var doneFab: FloatingActionButton

    private val viewModel: PickerViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application, piCropFragment.args) }
    )

    private val imageAdapter by lazy { ImageGripAdapter(piCropFragment.args.single) }
    private val permissionHelper by lazy { PermissionHelper(this, viewModel::extractImages) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dropdown_toolbar)
        recycler = view.findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        recycler.adapter = imageAdapter
        recycler.itemAnimator = null
        recycler.recycledViewPool.setMaxRecycledViews(ImageViewHolder.VIEW_TYPE, 40)

        toolbar.result = ::handleAdapterResult
        imageAdapter.result = ::handleAdapterResult

        viewModel.pickerState.observe(viewLifecycleOwner) { state ->
            toolbar.data = DropdownToolbar.Data(state.selectedFolder, state.folders)
            imageAdapter.submitList(state.media)

            doneFab.apply { if (viewModel.selectedImages.isEmpty()) hide() else show() }
        }

        doneFab = view.findViewById(R.id.doneFab)
        doneFab.setOnClickListener { piCropFragment.provideResultToTarget() }
        doneFab.applyTheming()
        doneFab.hide()

        permissionHelper.checkPermission()
    }

    override fun onResume() {
        super.onResume()
        if (permissionHelper.isReadExternalStorageGranted) {
            viewModel.extractImages()
        }
    }

    private fun handleAdapterResult(result: AdapterResult) {
        when (result) {
            AdapterResult.GoBack -> requireActivity().onBackPressed()
            is AdapterResult.FolderChanged -> {
                recycler.scrollTo(0, 0)
                viewModel.changeFolder(result.folder)
            }
            is AdapterResult.OnImageClicked -> {
                when {
                    piCropFragment.args.forceOpenEditor -> {
                        viewModel.prepareAspectRatio(result.media as Media.Image)
                        router.openCropper(result.media)
                    }
                    else -> {
                        viewModel.openFullscreen(result.position)
                        router.openViewer(result.media)
                    }
                }
            }
            is AdapterResult.OnSelectImageClicked -> viewModel.selectMedia(result.media)
        }
    }

}