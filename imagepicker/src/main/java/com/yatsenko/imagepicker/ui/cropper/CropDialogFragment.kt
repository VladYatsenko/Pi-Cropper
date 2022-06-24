package com.yatsenko.imagepicker.ui.cropper

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseDialogFragment
import com.yatsenko.imagepicker.widgets.crop.AspectRatioAdapter
import com.yatsenko.imagepicker.ui.cropper.view.CropIwaView
import com.yatsenko.imagepicker.utils.extensions.navigationBarSize
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

    private val media by lazy { requireArguments().getSerializable(CROP_URL) as Media }

    private lateinit var cropView: CropIwaView
    private lateinit var cropTools: CropToolsView

    private val list by lazy { AspectRatio.defaultList.map { AspectRatioAdapter.Data.createFrom(it) }.toMutableList() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = layoutInflater.inflate(R.layout.fragment_image_cropper_dialog, container, false)
        cropView = view.findViewById(R.id.crop_view)
        this.cropTools = view.findViewById(R.id.bottom_view)
        this.cropTools.updatePadding(bottom = requireContext().navigationBarSize)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri = Uri.parse(media.path)
        cropView.setImageUri(uri)

        cropTools.result = { result ->
            when (result) {
                is AdapterResult.OnAspectRatioClicked -> {
                    val isDynamic = result.item.ratio is AspectRatio.Dynamic
                    cropView.configureOverlay()
                        .setDynamicCrop(isDynamic)
                        .setAspectRatio(result.item.ratio)
                        .apply()
                    cropView.configureImage()
                        .setScale(0.01f)
                        .apply()
                    setSelectedRatio(result.item)
                }
            }
        }

        list.add(0, AspectRatioAdapter.Data.createFrom(AspectRatio.createFrom(media)))
        setSelectedRatio(list.first())

    }

    private fun setSelectedRatio(item: AspectRatioAdapter.Data) {
        val newList = list.map { AspectRatioAdapter.Data(it.ratio, it.ratio == item.ratio) }
        list.clear()
        list.addAll(newList)

        cropTools.data = CropToolsView.Data(list)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.dismiss()
    }

}