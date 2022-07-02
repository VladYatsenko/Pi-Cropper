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
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val isReadExternalStorageGranted: Boolean
        get() = ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

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