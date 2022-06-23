package com.yatsenko.imagepicker.ui.cropper

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseDialogFragment
import com.yatsenko.imagepicker.ui.cropper.ui.AspectRatioAdapter
import com.yatsenko.imagepicker.ui.cropper.view.CropIwaView
import com.yatsenko.imagepicker.utils.extensions.navigationBarSize
import com.yatsenko.imagepicker.widgets.aspectRatio.AspectRatioPreviewView

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
    private lateinit var bottomView: View
    private lateinit var recycler: RecyclerView

    private val adapter = AspectRatioAdapter()
    private val list by lazy { AspectRatio.defaultList.map { AspectRatioPreviewView.Data.createFrom(it) }.toMutableList() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = layoutInflater.inflate(R.layout.fragment_image_cropper_dialog, container, false)
        cropView = view.findViewById(R.id.crop_view)
        bottomView = view.findViewById(R.id.bottom_view)
        bottomView.updatePadding(bottom = requireContext().navigationBarSize)

        recycler = view.findViewById(R.id.aspect_recycler)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri = Uri.parse(media.path)
        cropView.setImageUri(uri)

        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = this@CropDialogFragment.adapter
        }

        adapter.result = { result ->
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

        list.add(0, AspectRatioPreviewView.Data.createFrom(AspectRatio.createFrom(media)))
        setSelectedRatio(list.first())
    }

    private fun setSelectedRatio(item: AspectRatioPreviewView.Data) {
        val newList = list.map { AspectRatioPreviewView.Data(it.ratio, it.ratio == item.ratio) }
        list.clear()
        list.addAll(newList)

        adapter.submitList(list)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        this.dismiss()
    }

}