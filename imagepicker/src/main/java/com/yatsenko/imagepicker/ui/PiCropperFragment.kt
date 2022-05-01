package com.yatsenko.imagepicker.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.abstraction.BaseFragment
import com.yatsenko.imagepicker.utils.Router

class PiCropperFragment: BaseFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_picropper

    val router by  lazy { Router(R.id.container, childFragmentManager) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (router.backStackCount == 0) {
            router.openPicker()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    router.canGoBack -> router.goBack()
                    else -> {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        })
    }

}