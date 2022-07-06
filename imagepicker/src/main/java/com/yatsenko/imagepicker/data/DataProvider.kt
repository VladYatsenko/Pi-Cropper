package com.yatsenko.imagepicker.data

import android.os.Handler
import android.os.Looper
import com.yatsenko.imagepicker.model.Media
import kotlin.math.min

internal interface DataProvider {
    fun loadInitial(): List<Media> = emptyList()
    fun loadAfter(key: String, callback: (List<Media>) -> Unit) {}
    fun loadBefore(key: String, callback: (List<Media>) -> Unit) {}
}

internal class SimpleDataProvider(val list: List<Media>) : DataProvider {

    override fun loadInitial() = list

    override fun loadAfter(key: String, callback: (List<Media>) -> Unit) {
        val idx = list.indexOfFirst { it.id == key }
        val result: List<Media> = if (idx < 0) emptyList()
        else list.subList(idx + 1, list.size)
        Handler(Looper.getMainLooper()).post {
            callback(result)
        }
    }

    override fun loadBefore(key: String, callback: (List<Media>) -> Unit) {
        val idx = list.indexOfFirst { it.id == key }
        val result: List<Media> = if (idx < 0) emptyList()
        else list.subList(0, min(idx, list.size))
        Handler(Looper.getMainLooper()).post {
            callback(result)
        }
    }
}