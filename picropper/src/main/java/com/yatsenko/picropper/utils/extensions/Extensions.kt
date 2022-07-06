package com.yatsenko.picropper.utils.extensions

import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.view.WindowCompat

internal fun Window.setupActionBar(color: Int, lightStatusBar: Boolean) {
    this.statusBarColor = color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.getInsetsController(this, this.decorView).apply {
            isAppearanceLightStatusBars = !lightStatusBar
        }
    } else {
        var flags = this.decorView.systemUiVisibility
        this.decorView.systemUiVisibility = when (lightStatusBar) {
            true -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv().let { flags = flags and it; flags }
            else -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.let { flags = flags or it; flags }
        }
    }
}

internal fun Window.setupBottomBar(color: Int, lightNavigationBar: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.getInsetsController(this, this.decorView).apply {
            isAppearanceLightNavigationBars = lightNavigationBar
        }
    } else {
        val decorView: View = this.decorView
        var flags = decorView.systemUiVisibility
        flags = if (!lightNavigationBar) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        decorView.systemUiVisibility = flags
        this.navigationBarColor = color
    }
}