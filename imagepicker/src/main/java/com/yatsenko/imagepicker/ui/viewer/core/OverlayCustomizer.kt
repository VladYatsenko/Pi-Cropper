package com.yatsenko.imagepicker.ui.viewer.core

import android.view.View
import android.view.ViewGroup

interface OverlayCustomizer {
    fun provideView(parent: ViewGroup): View? = null
}