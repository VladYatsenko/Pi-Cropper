package com.yatsenko.picropper.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yatsenko.picropper.R


internal class PermissionHelper(
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
                    val neverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(fragment.requireActivity(), permission)
                    if (neverAskAgain) {
                        //user click "never ask again"
                        neverAskAgainDialog()
                    } else {
                        //show explain dialog
                        explainDialog()
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

    private fun explainDialog() {
        AlertDialog.Builder(fragment.requireContext(), R.style.PermissionDialog)
            .setMessage(R.string.permission_read_storage)
            .setPositiveButton(R.string.permission_read_storage_ok) { dialog, which ->
                checkPermission()
            }
            .show()
    }

    private fun neverAskAgainDialog() {
        AlertDialog.Builder(fragment.requireContext(), R.style.PermissionDialog)
            .setMessage(R.string.permission_never_ask_again)
            .setPositiveButton(R.string.permission_read_storage_ok) { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.fromParts("package", fragment.requireActivity().packageName, null)
                }
                fragment.startActivity(intent)
            }
            .show()
    }
}