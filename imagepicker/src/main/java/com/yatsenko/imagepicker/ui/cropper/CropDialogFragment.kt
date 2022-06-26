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
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseDialogFragment
import com.yatsenko.imagepicker.ui.cropper.holder.UCropIml
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.utils.extensions.navigationBarSize
import com.yatsenko.imagepicker.widgets.crop.AspectRatioAdapter
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

    private val list by lazy { AspectRatio.defaultList.map { AspectRatioAdapter.Data.createFrom(it) }.toMutableList() }

    private val viewModel: PickerViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application) }
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
        list.add(0, AspectRatioAdapter.Data.createFrom(AspectRatio.createFrom(media)))
        setSelectedRatio(list.first())
    }

    private fun handleResult(result: AdapterResult) {
        when (result) {
            is AdapterResult.OnAspectRatioClicked -> setSelectedRatio(result.item)
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
                viewModel.imageCropped(media, result.media)
                onBackPressed()
            }
        }
    }

    private fun setSelectedRatio(item: AspectRatioAdapter.Data) {
        val newList = list.map { AspectRatioAdapter.Data(it.ratio, it.ratio == item.ratio) }
        list.clear()
        list.addAll(newList)

        cropTools.data = CropToolsView.Data(list)
        crop.applyRatio(item.ratio, item.ratio.isDynamic)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.dismiss()
    }

}