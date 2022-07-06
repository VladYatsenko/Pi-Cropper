package com.yatsenko.picropper.ui.abstraction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yatsenko.picropper.ui.PiCropperFragment
import com.yatsenko.picropper.utils.Router

abstract class BaseChildFragment : Fragment() {

    internal val router: Router
        get() = (parentFragment as PiCropperFragment).router

    protected val piCropFragment: PiCropperFragment
        get() = requireParentFragment() as PiCropperFragment

    abstract val layoutId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(layoutId, container, false)
    }

}