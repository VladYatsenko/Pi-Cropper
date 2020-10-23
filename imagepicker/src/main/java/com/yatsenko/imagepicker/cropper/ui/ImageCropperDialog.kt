package com.yatsenko.imagepicker.cropper.ui

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.yatsenko.imagepicker.R
import kotlinx.android.synthetic.main.dialog_crop.view.*
import java.io.File

class ImageCropperDialog: DialogFragment() {

    companion object {
        fun newInstance(fm: FragmentManager, uri: Uri) {
            val cropper = ImageCropperDialog()
            cropper.uri = uri

            cropper.show(fm, cropper.tag)
        }
    }

    private var dialog: AlertDialog? = null
    private var uri: Uri? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_crop, null, false)

        dialog = AlertDialog.Builder(requireContext(), R.style.ImageViewerDialog_Default)
                .setView(view)
                .create()
        view.cropImageView.setImageUriAsync(Uri.fromFile(File(uri.toString())))

        return dialog as AlertDialog
    }

}