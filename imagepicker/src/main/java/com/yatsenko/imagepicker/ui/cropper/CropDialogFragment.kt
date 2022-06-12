package com.yatsenko.imagepicker.ui.cropper

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.abstraction.BaseDialogFragment
import com.yatsenko.imagepicker.ui.cropper.view.CropIwaView

class CropDialogFragment: BaseDialogFragment() {

    companion object {
        private const val CROP_URL = "crop_url"
        fun show(imagePath: String, fragmentManager: FragmentManager) {
            val dialog = CropDialogFragment().apply {
                this.arguments = Bundle().apply {
                    putString(CROP_URL, imagePath)
                }
            }
            dialog.show(fragmentManager)
        }
    }

    private lateinit var cropView: CropIwaView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = layoutInflater.inflate(R.layout.fragment_image_cropper_dialog, container, false)
        cropView = view.findViewById(R.id.crop_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri = Uri.parse(requireArguments().getString(CROP_URL))
        cropView.setImageUri(uri)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.dismiss()
    }

}