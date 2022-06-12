package com.yatsenko.imagepicker.ui.abstraction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yatsenko.imagepicker.ui.PiCropperFragment
import com.yatsenko.imagepicker.utils.Router

abstract class BaseChildFragment : Fragment() {

    val router: Router
        get() = (parentFragment as PiCropperFragment).router

    abstract val layoutId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(layoutId, container, false)
    }

}