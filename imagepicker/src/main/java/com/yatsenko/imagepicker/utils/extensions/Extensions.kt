package com.yatsenko.imagepicker.utils.extensions

import android.app.Activity
import android.os.Build
import android.util.SparseArray
import android.view.View
import androidx.core.util.forEach
import androidx.core.view.ViewCompat
import com.yatsenko.imagepicker.core.Theme

internal fun <T> SparseArray<T>?.toList(): List<T> {
    val list = ArrayList<T>()
    this?.forEach { _, value ->
        list.add(value)
    }
    return list.toList()
}

internal fun Activity.setupActionBar() {
    val color = Theme.themedColor(this, Theme.statusBarColor)
    val lightStatusBar = true
    window.statusBarColor = color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        ViewCompat.getWindowInsetsController(window.decorView)?.apply {
            isAppearanceLightStatusBars = !lightStatusBar
        }
    } else {
        var flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = when (lightStatusBar) {
            true -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv().let { flags = flags and it; flags }
            else -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.let { flags = flags or it; flags }
        }
    }
}

internal fun Activity.setupBottomBar() {
    val color = Theme.themedColor(this, Theme.navigationBarColor)
    val lightStatusBar = true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val decorView: View = window.decorView
        var flags = decorView.systemUiVisibility
        flags = if (!lightStatusBar) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        decorView.systemUiVisibility = flags
        window.navigationBarColor = color
    }
}