package com.yatsenko.imagepicker.model

import java.io.Serializable

sealed class Media(
    open val id: String,
    open val path: String,
    open val lastModified: Long,
    open val folderId: String,
    open val isSelected: Boolean = false,
    open val indexInResult: Int = -1
) : Serializable {

    open val imagePath: String = path

    val indexString: String
        get() = if (isSelected) indexInResult.plus(1).toString() else ""

    data class Image(
        override val id: String,
        override val path: String,
        override val lastModified: Long,
        override val folderId: String,
        override val isSelected: Boolean = false,
        override val indexInResult: Int = -1,
        private val croppedPath: String? = null
    ) : Media(id, path, lastModified, folderId, isSelected, indexInResult) {

        override val imagePath: String
            get() = croppedPath ?: path

    }

    data class SubsamplingImage(
        override val id: String,
        override val path: String,
        override val lastModified: Long,
        override val folderId: String,
        override val isSelected: Boolean = false,
        override val indexInResult: Int = -1,
        private val croppedPath: String? = null
    ) : Media(id, path, lastModified, folderId, isSelected, indexInResult) {


        override val imagePath: String
            get() = croppedPath ?: path

    }

    data class Video(
        override val id: String,
        override val path: String,
        override val lastModified: Long,
        override val folderId: String,
        override val isSelected: Boolean = false,
        override val indexInResult: Int = -1,
        private val editedPath: String? = null
    ) : Media(id, path, lastModified, folderId, isSelected, indexInResult) {

        val videoPath: String
            get() = editedPath ?: path

    }

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