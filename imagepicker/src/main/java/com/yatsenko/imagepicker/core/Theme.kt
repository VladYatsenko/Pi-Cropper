package com.yatsenko.imagepicker.core

import android.content.Context
import androidx.core.content.ContextCompat
import com.yatsenko.imagepicker.R

object Theme {

    const val accentColor = "accentColor"
    const val accentDualColor = "accentDualColor"

    const val statusBarColor = "statusBarColor"
    const val navigationBarColor = "navigationBarColor"
    const val toolbarColor = "toolbarColor"
    const val toolbarContentColor = "toolbarContentColor"
    const val gridBackgroundColor = "gridBackgroundColor"
    const val imageBackgroundColor = "imageBackgroundColor"

    const val toolsColor = "toolsColor"
    const val toolsResetRotationColor = "toolsResetRotationColor"
    const val toolsBackgroundColor = "toolsBackgroundColor"

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
        checkBoxCheckedBorderOverlay to R.color.white,

        toolbarColor to R.color.ebony_clay,
        toolbarContentColor to R.color.white,

        statusBarColor to R.color.mirage,
        navigationBarColor to R.color.mirage,

        gridBackgroundColor to R.color.mirage,

        toolsColor to R.color.white,
        toolsResetRotationColor to R.color.silver_chalice,
        toolsBackgroundColor to R.color.mirage
    )

    var theme: Map<String, Int> = dark

    fun themedColor(name: String): Int {
        return theme[name] ?: R.color.ebony_clay
    }

    fun themedColor(context: Context, name: String): Int {
        val color = theme[name] ?: R.color.ebony_clay
        return ContextCompat.getColor(context, color)
    }

}