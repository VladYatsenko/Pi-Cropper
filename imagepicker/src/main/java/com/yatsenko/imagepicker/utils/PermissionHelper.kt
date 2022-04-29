package com.yatsenko.imagepicker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yatsenko.imagepicker.R
import java.util.ArrayList

class PermissionHelper(
    private val fragment: Fragment,
    private val onPermissionGranted: () -> Unit
) {

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val requestMultiplePermissions by lazy {
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val granted = it.value
                val permission = it.key
                if (!granted) {
                    val neverAskAgain =
                        !ActivityCompat.shouldShowRequestPermissionRationale(fragment.requireActivity(), permission)
                    if (neverAskAgain) {
                        //user click "never ask again"
                    } else {
                        //show explain dialog
                    }
                    return@registerForActivityResult
                }
            }
            onPermissionGranted()
        }
    }

    fun checkPermission() {
        permissions.forEach { p ->
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), p) == PackageManager.PERMISSION_DENIED) {
                requestMultiplePermissions.launch(permissions)
                return
            }
        }
        onPermissionGranted()
    }

}

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.M)
fun Activity.checkPermissions(permissions: Array<String>, requestCode: Int, dialogMsgForRationale: Int): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

    val needList: MutableList<String> = ArrayList()
    var needShowRationale = false
    val length = permissions.size
    for (i in 0 until length) {
        val permisson = permissions[i]
        if (this.checkSelfPermission(permisson)
            != PackageManager.PERMISSION_GRANTED
        ) {
            needList.add(permisson)
            if (this.shouldShowRequestPermissionRationale(permisson)) needShowRationale = true
        }
    }
    return if (needList.size != 0) {
        if (needShowRationale) {
            AlertDialog.Builder(this).setCancelable(false)
                .setTitle(R.string.dialog_imagepicker_permission_title)
                .setMessage(dialogMsgForRationale)
                .setPositiveButton(R.string.dialog_imagepicker_permission_confirm) { dialog, which ->
                    dialog.dismiss()
                    this.requestPermissions(needList.toTypedArray(), requestCode)
                }.create().show()
            return false
        }
        this.requestPermissions(needList.toTypedArray(), requestCode)
        false
    } else {
        true
    }
}

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.M)
fun Activity.checkRequestPermissionsResult(permissions: Array<String?>, grantResults: IntArray, finishAfterCancelDialog: Boolean, dialogMsgForNerverAsk: Int): BooleanArray? {
    var result = true
    var isNerverAsk = false
    val length = grantResults.size
    for (i in 0 until length) {
        val permission = permissions[i]
        val grandResult = grantResults[i]
        if (grandResult == PackageManager.PERMISSION_DENIED) {
            result = false
            if (!this.shouldShowRequestPermissionRationale(permission.toString())) isNerverAsk = true
        }
    }
    if (!result) {
        Toast.makeText(this, R.string.toast_imagepicker_permission_denied, Toast.LENGTH_SHORT).show()
        if (isNerverAsk) {
            //处理NerverAsk
            AlertDialog.Builder(this).setCancelable(false)
                .setTitle(R.string.dialog_imagepicker_permission_title)
                .setMessage(dialogMsgForNerverAsk)
                .setNegativeButton(R.string.dialog_imagepicker_permission_nerver_ask_cancel) { dialog, which ->
                    dialog.dismiss()
                    if (finishAfterCancelDialog)
                        this.finish()
                }
                .setPositiveButton(R.string.dialog_imagepicker_permission_nerver_ask_confirm) { dialog, which ->
                    dialog.dismiss()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:" + this.packageName)
                    this.startActivity(intent)
                    this.finish()
                }.create().show()
        }
    }
    return booleanArrayOf(result, isNerverAsk)
}
