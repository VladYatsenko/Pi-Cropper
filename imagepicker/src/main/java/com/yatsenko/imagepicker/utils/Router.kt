package com.yatsenko.imagepicker.utils

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.ui.picker.PickerFragment

class Router(private val containerId: Int, private val fragmentManager: FragmentManager) {

    val backStackCount: Int
        get() = fragmentManager.backStackEntryCount

    val canGoBack: Boolean
        get() = backStackCount > 1

    fun openPicker() {
        showFragment(PickerFragment::class.java)
    }

    fun openViewer(view: ImageView, image: Image) {
//        showFragment(ViewerFragment::class.java, ViewerFragment.prepareArgs(image), sharedElement = view)
    }

    private fun showFragment(
        fragmentClass: Class<out Fragment?>,
        args: Bundle? = null,
        clearBackstack: Boolean = false,
        sharedElement: View? = null
    ) {
        val backStackFragmentsCount = fragmentManager.backStackEntryCount
        if (clearBackstack) {
            for (i in backStackFragmentsCount - 1 downTo 0) {
                val backStackId = fragmentManager.getBackStackEntryAt(i).id
                fragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
        val fragmentTag = fragmentClass.hashCode().toString()
        val fragmentTransaction = fragmentManager.beginTransaction().setReorderingAllowed(true)

        if (!fragmentManager.isStateSaved) {

            sharedElement?.let {
                fragmentTransaction.addSharedElement(sharedElement, sharedElement.transitionName)
            }

            fragmentTransaction.replace(containerId, fragmentClass, args, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
        }
    }

    fun goBack() {
        fragmentManager.popBackStack()
    }

}