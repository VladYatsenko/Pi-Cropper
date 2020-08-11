package com.yatsenko.imagepicker.viewer.common.extensions

import com.github.chrisbanes.photoview.PhotoView

internal fun PhotoView.resetScale(animate: Boolean) {
    setScale(minimumScale, animate)
}