package com.yatsenko.imagepicker.ui.cropper

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseDialogFragment
import com.yatsenko.imagepicker.ui.cropper.holder.UCropIml
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.widgets.crop.CropToolsView

class CropDialogFragment : BaseDialogFragment() {

    companion object {
        private const val CROP_URL = "crop_url"
        fun show(media: Media, fragmentManager: FragmentManager) {
            val dialog = CropDialogFragment().apply {
                this.arguments = Bundle().apply {
                    putSerializable(CROP_URL, media)
                }
            }
            dialog.show(fragmentManager)
        }
    }

    private lateinit var cropTools: CropToolsView

    private val media by lazy { requireArguments().getSerializable(CROP_URL) as Media.Image }
    private val crop by lazy { UCropIml(requireContext(), Uri.parse(media.path), ::handleResult).also {
        lifecycle.addObserver(it)
    } }

    private val viewModel: PickerViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application, piCropFragment.args) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = layoutInflater.inflate(R.layout.fragment_image_cropper_dialog, container, false)
        val cropContainer: FrameLayout = view.findViewById(R.id.crop_container)
        cropContainer.addView(crop.cropView)
        cropTools = view.findViewById(R.id.bottom_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cropTools.result = ::handleResult

        viewModel.cropperState.observe(viewLifecycleOwner) {
            cropTools.data = CropToolsView.Data(it.ratios)
            crop.applyRatio(it.selectedRatio, it.selectedRatio.isDynamic)
        }
    }

    private fun handleResult(result: AdapterResult) {
        when (result) {
            is AdapterResult.OnAspectRatioClicked -> viewModel.selectAspectRatio(result.item)
            is AdapterResult.OnRotateStart -> crop.onRotateStart()
            is AdapterResult.OnRotateProgress -> crop.onRotate(result.deltaAngle)
            is AdapterResult.OnRotateEnd -> crop.onRotateEnd()
            is AdapterResult.OnRotate90Clicked -> crop.onRotate(90f)
            is AdapterResult.OnResetRotationClicked -> crop.onResetRotation()
            is AdapterResult.OnImageRotated -> cropTools.rotateAngel = result.angle
            is AdapterResult.OnApplyCrop -> {
                cropTools.showLoading()
                crop.crop()
            }
            is AdapterResult.OnCancelCrop -> onBackPressed()
            is AdapterResult.OnImageCropped -> {
                when {
                    piCropFragment.args.single -> {
                        piCropFragment.provideResultToTarget(result.media)
                    }
                    else -> {
                        viewModel.imageCropped(media, result.media)
                        onBackPressed()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.dismiss()
    }

}