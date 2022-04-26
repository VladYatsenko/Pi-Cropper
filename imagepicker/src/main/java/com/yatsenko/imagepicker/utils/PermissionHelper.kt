package com.yatsenko.imagepicker.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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