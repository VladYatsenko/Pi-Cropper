package com.yatsenko.imagepicker.utils.extensions

import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment

// A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
fun Fragment.enterSharedElement(result: (names: List<String>, sharedElements: MutableMap<String, View>) -> Unit) {
    setEnterSharedElementCallback(
        object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
                result(names, sharedElements)
            }
        }
    )
}

fun Fragment.exitSharedElement(result: (names: List<String>, sharedElements: MutableMap<String, View>) -> Unit) {
    setExitSharedElementCallback(
        object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
                result(names, sharedElements)
            }
        }
    )
}