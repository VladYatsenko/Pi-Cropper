package com.yatsenko.picropper.ui.picker

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yatsenko.picropper.R
import com.yatsenko.picropper.core.Theme
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.abstraction.BaseChildFragment
import com.yatsenko.picropper.ui.picker.adapter.MediaGripAdapter
import com.yatsenko.picropper.ui.picker.adapter.MediaViewHolder
import com.yatsenko.picropper.ui.picker.viewmodel.MediaViewModel
import com.yatsenko.picropper.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.picropper.utils.PermissionHelper
import com.yatsenko.picropper.utils.extensions.FileUtils
import com.yatsenko.picropper.utils.extensions.applyTheming
import com.yatsenko.picropper.widgets.toolbar.DropdownToolbar


class PickerFragment : BaseChildFragment() {

    override val layoutId: Int = R.layout.fragment_picker

    private lateinit var toolbar: DropdownToolbar
    private lateinit var recycler: RecyclerView
    private lateinit var doneFab: FloatingActionButton

    private val viewModel: MediaViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application, piCropFragment.args) }
    )

    private val imageAdapter by lazy { MediaGripAdapter(piCropFragment.args.single) }
    private val permissionHelper by lazy { PermissionHelper(this, viewModel::extractImages) }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) extractImages()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dropdown_toolbar)
        recycler = view.findViewById(R.id.recycler)
        recycler.setBackgroundColor(Theme.themedColor(requireContext(), Theme.gridBackgroundColor))
        recycler.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        recycler.adapter = imageAdapter
        recycler.itemAnimator = null
        recycler.recycledViewPool.setMaxRecycledViews(MediaViewHolder.VIEW_TYPE, 40)

        toolbar.result = ::handleAdapterResult
        imageAdapter.result = ::handleAdapterResult

        viewModel.pickerState.observe(viewLifecycleOwner) { state ->
            toolbar.data = DropdownToolbar.Data(state.selectedFolder, state.folders)
            imageAdapter.submitList(state.grid)

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
        extractImages()
    }

    private fun extractImages() {
        if (permissionHelper.isReadExternalStorageGranted) {
            viewModel.extractImages()
        }
    }

    private fun handleAdapterResult(result: AdapterResult) {
        when (result) {
            AdapterResult.GoBack -> requireActivity().onBackPressed()
            is AdapterResult.TakeFromCamera -> {
                val uri = FileUtils.cameraUri(requireActivity(), "Crop", piCropFragment.args.compressFormatExtension)
                takePicture.launch(uri)
            }
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
                        viewModel.openFullscreen(result.media.id)
                        router.openViewer(result.media)
                    }
                }
            }
            is AdapterResult.OnSelectImageClicked -> viewModel.selectMedia(result.media)
        }
    }

}