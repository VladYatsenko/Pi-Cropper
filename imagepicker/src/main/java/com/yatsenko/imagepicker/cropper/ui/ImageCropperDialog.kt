package com.yatsenko.imagepicker.cropper.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.cropper.CropImage
import com.yatsenko.imagepicker.cropper.CropImageView
import com.yatsenko.imagepicker.viewer.common.extensions.makeVisible
import kotlinx.android.synthetic.main.dialog_crop.view.*
import java.io.File
import java.io.FileOutputStream

class ImageCropperDialog : DialogFragment() {

    companion object {
        fun show(fm: FragmentManager, uri: Uri, result: (uri: Uri) -> Unit) {
            val cropper = ImageCropperDialog()
            cropper.uri = uri
            cropper.callback = result
            cropper.show(fm, cropper.tag)
        }
    }

    private var dialog: AlertDialog? = null
    private var uri: Uri? = null
    private var cropImageView: CropImageView? = null
    private var callback: ((uri: Uri) -> Unit)? = null
    private var dialogView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crop, null, false)

        dialog = AlertDialog.Builder(requireContext(), R.style.ImageViewerDialog_Default)
            .setView(dialogView)
            .create()

        cropImageView = dialogView?.cropImageView

        dialogView?.backBtn?.setOnClickListener {
            dialog?.dismiss()
        }
        dialogView?.rotateImg?.setOnClickListener {
            cropImageView?.rotateImage(90)
        }
        dialogView?.cropBtn?.setOnClickListener {
            cropImageView?.getCroppedImageAsync()
        }
        cropImageView?.setOnCropImageCompleteListener { _, result ->
            handleResult(result)
        }
        cropImageView?.setOnSetImageUriCompleteListener { view, uri, error ->
//            view.toolbar?.visibility = View.VISIBLE
        }
        cropImageView?.setImageUriAsync(Uri.fromFile(File(uri.toString())))

        return dialog as AlertDialog
    }

    override fun onResume() {
        super.onResume()
        if(dialogView?.toolbar?.visibility != View.VISIBLE)
            dialogView?.toolbar?.makeVisible()
    }

    private fun handleResult(result: CropImageView.CropResult) {
        if (result.error == null) {
            //"SAMPLE_SIZE", result.sampleSize
            val uri = if (result.uri != null) {
                result.uri
            } else {
                val bitmap = if (cropImageView?.cropShape === CropImageView.CropShape.OVAL) CropImage.toOvalBitmap(result.bitmap) else result.bitmap
                val file = File(requireContext().externalCacheDir, System.currentTimeMillis().toString() + ".png")
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    output.flush()
                }
                file.toUri()
            }
            callback?.invoke(uri)
        } else {
            Log.e("AIC", "Failed to crop image", result.error)
            Toast.makeText(activity, "Image crop failed: " + result.error.message, Toast.LENGTH_LONG).show()
        }
        dialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cropImageView?.setOnCropImageCompleteListener(null);
    }

}