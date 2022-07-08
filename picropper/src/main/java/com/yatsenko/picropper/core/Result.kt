package com.yatsenko.picropper.core

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.yatsenko.picropper.ui.PiCropperFragment

fun Fragment.piCropperFragmentResultListener(result: (List<Uri>) -> Unit) {
    setFragmentResultListener(PiCropperFragment.PiCROPPER_RESULT) { key, bundle ->
        bundle.getStringArrayList(PiCropperFragment.RESULT_MEDIA)?.mapNotNull { Uri.parse(it) }?.let {
            result(it)
        }
    }
}