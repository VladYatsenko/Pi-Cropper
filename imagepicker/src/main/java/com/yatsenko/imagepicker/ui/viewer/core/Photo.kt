package com.yatsenko.imagepicker.ui.viewer.core

import com.yatsenko.imagepicker.ui.viewer.adapter.ItemType

interface Photo {
    fun id(): Long
    fun itemType(): @ItemType.Type Int
}
