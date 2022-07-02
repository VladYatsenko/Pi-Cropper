package com.yatsenko.imagepicker.core

import com.yatsenko.imagepicker.R

object Theme {

    const val accentColor = "accentColor"
    const val accentDualColor = "accentDualColor"

//    const val statusBarColor = "statusBarColor"
//    const val toolbarBarColor = "toolbarBarColor"
//    const val gridBackgroundColor = "gridBackgroundColor"
//    const val imageBackgroundColor = "imageBackgroundColor"
//    const val toolsBackgroundColor = "toolsBackgroundColor"

    const val checkBoxTextColor = "checkBoxTextColor"
    const val checkBoxBackground = "checkBoxBackground"
    const val checkBoxCheckedBorder = "checkBoxCheckedBorder"
    const val checkBoxUncheckedBorder = "checkBoxUncheckedBorder"
    const val checkBoxCheckedBorderOverlay = "checkBoxCheckedBorderOverlay"

    val dark = mapOf(
        accentColor to R.color.cerulean,
        accentDualColor to R.color.white,
        checkBoxTextColor to R.color.white,
        checkBoxBackground to R.color.transparent_gray_40,
        checkBoxCheckedBorder to R.color.ebony_clay,
        checkBoxUncheckedBorder to R.color.white,
        checkBoxCheckedBorderOverlay to R.color.white
    )

    var theme: Map<String, Int> = dark

    fun themedColor(name: String): Int {
        return theme[name] ?: R.color.ebony_clay
    }

}