package com.yatsenko.picropper.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.cropper.CropDialogFragment
import com.yatsenko.picropper.ui.picker.PickerFragment
import com.yatsenko.picropper.ui.viewer.ImageViewerDialogFragment

internal class Router(private val containerId: Int, private val fragmentManager: FragmentManager) {

    private val backStackCount: Int
        get() = fragmentManager.backStackEntryCount

    val canGoBack: Boolean
        get() = backStackCount > 1

    fun openPicker() {
        showFragment(PickerFragment::class.java)
    }

    fun openViewer(media: Media) {
        ImageViewerDialogFragment.show(media.id, fragmentManager)
    }

    fun openCropper(media: Media) {
        CropDialogFragment.show(media, fragmentManager)
    }

    private fun showFragment(
        fragmentClass: Class<out Fragment?>,
        args: Bundle? = null,
        clearBackstack: Boolean = false,
    ) {
        if (clearBackstack) {
            clearBackStack()
        }
        val fragmentTag = fragmentClass.hashCode().toString()
        val fragmentTransaction = fragmentManager.beginTransaction().setReorderingAllowed(true)

        if (!fragmentManager.isStateSaved) {
            fragmentTransaction.replace(containerId, fragmentClass, args, fragmentTag)
                .addToBackStack(null)
                .commit()
        }
    }

    fun clearBackStack() {
        val backStackFragmentsCount = fragmentManager.backStackEntryCount
        for (i in backStackFragmentsCount - 1 downTo 0) {
            val backStackId = fragmentManager.getBackStackEntryAt(i).id
            fragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun goBack() {
        fragmentManager.popBackStack()
    }

}