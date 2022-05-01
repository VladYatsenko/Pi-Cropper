package com.yatsenko.imagepicker.model

import java.io.Serializable

data class Image (
    val id: String,
    val path: String,
    val lastModified: Long,
    val folderId: String,
    val isSelected: Boolean = false,
    val indexInResult: Int = -1,
    private val croppedPath: String? = null
): Serializable {

    val imagePath: String
        get() = croppedPath ?: path

    val index: String
        get() = if (isSelected) indexInResult.plus(1).toString() else ""

}

sealed class Folder(
    open val id: String,
    open val name: String?,
    open val firstImagePath: String,
    open val count: Int = 0
) {

    data class All(
        override val name: String?,
        override val firstImagePath: String = "",
        override val count: Int = 0
    ) : Folder("folder_all", name, firstImagePath, count)

    data class Common(
        override val id: String,
        override val name: String?,
        override val firstImagePath: String = "",
        override val count: Int = 0
    ) : Folder(id, name, firstImagePath, count) {

    }

}