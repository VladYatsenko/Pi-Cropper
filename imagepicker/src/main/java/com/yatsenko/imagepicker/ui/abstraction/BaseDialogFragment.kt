package com.yatsenko.imagepicker.ui.abstraction

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.PiCropperFragment
import com.yatsenko.imagepicker.utils.Router

open class BaseDialogFragment : DialogFragment() {

    protected val router: Router
        get() = (requireParentFragment() as PiCropperFragment).router

    protected val piCropFragment: PiCropperFragment
        get() = requireParentFragment() as PiCropperFragment

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireActivity(), R.style.Theme_Light_NoTitle_ViewerDialog).apply {
            setCanceledOnTouchOutside(true)
            window?.let(::setWindow)
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.setOnKeyListener { _, keyCode, event ->
            val backPressed = event.action == MotionEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK
            if (backPressed) onBackPressed()
            backPressed
        }
    }

    override fun onResume() {
        super.onResume()
        view?.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.setOnKeyListener(null)
    }

    open fun setWindow(win: Window) {
        win.setWindowAnimations(R.style.Animation_Keep)
        win.decorView.setPadding(0, 0, 0, 0)
        val lp = win.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        win.attributes = lp
        win.setGravity(Gravity.CENTER)

        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }

    fun show(fragmentManager: FragmentManager?) {
        when {
            fragmentManager == null -> showFailure("fragmentManager is detach after parent destroy")
            fragmentManager.isStateSaved -> showFailure("dialog fragment show when fragmentManager isStateSaved")
            else -> show(fragmentManager, javaClass.simpleName)
        }
    }

    open fun showFailure(message: String? = null) {}

    open fun onBackPressed() {}

}