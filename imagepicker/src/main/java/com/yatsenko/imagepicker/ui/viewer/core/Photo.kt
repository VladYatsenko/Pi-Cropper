package com.yatsenko.imagepicker.ui.viewer.core

import com.yatsenko.imagepicker.ui.viewer.adapter.ItemType

interface Photo {
    fun id(): String
    fun itemType(): @ItemType.Type Int
}
