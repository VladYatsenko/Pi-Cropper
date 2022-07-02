package com.yatsenko.imagepicker.utils.extensions

import android.util.SparseArray
import androidx.core.util.forEach

fun <T> SparseArray<T>?.toList(): List<T> {
    val list = ArrayList<T>()
    this?.forEach { _, value ->
        list.add(value)
    }
    return list.toList()
}