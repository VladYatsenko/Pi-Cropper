package com.yatsenko.picropper.model

import android.net.Uri
import androidx.core.net.toFile
import java.io.Serializable

internal sealed class Media(
    open val id: String,
    open val path: String,
    open val lastModified: Long,
    open val folderId: String,
    open val width: Int,
    open val height: Int,
    open val size: Int,
    open val name: String,
    open val indexInResult: Int = -1,
    open val hideInGrid: Boolean = false,
    open val hideInViewer: Boolean = false
) : Serializable {

    open val mediaPath: String = path

    val isSelected: Boolean
        get() = indexInResult != -1

    val shouldAnimate: (Media?) -> Boolean = { media ->
        this.id == media?.id && this.isSelected != media.isSelected
    }

    data class Image(
        override val id: String,
        override val path: String,
        override val lastModified: Long,
        override val folderId: String,
        override val width: Int,
        override val height: Int,
        override val size: Int,
        override val name: String,
        override val indexInResult: Int = -1,
        override val hideInGrid: Boolean = false,
        override val hideInViewer: Boolean = false,
        val croppedImage: Image? = null,
    ) : Media(id, path, lastModified, folderId, width, height, size, name, indexInResult, hideInGrid) {

        val hasCroppedImage: Boolean
            get() = croppedImage != null

        override val mediaPath: String
            get() = croppedImage?.path ?: path

        companion object {
            fun croppedImage(uri: Uri, width: Int, height: Int, size: Int): Image {
                return Image(
                    id = "",
                    path = uri.toString(),
                    lastModified = 0,
                    folderId = "",
                    width = width,
                    height = height,
                    size = size,
                    name = uri.toFile().name,
                    indexInResult = -1,
                    hideInGrid = false,
                    croppedImage = null
                )
            }
        }

    }

    data class SubsamplingImage(
        override val id: String,
        override val path: String,
        override val lastModified: Long,
        override val folderId: String,
        override val width: Int,
        override val height: Int,
        override val size: Int,
        override val name: String,
        override val indexInResult: Int = -1,
        override val hideInGrid: Boolean = false,
        override val hideInViewer: Boolean = false,
        private val croppedImage: Image? = null
    ) : Media(id, path, lastModified, folderId, width, height, size, name, indexInResult) {


        override val mediaPath: String
            get() = croppedImage?.path ?: path

    }

    data class Video(
        override val id: String,
        override val path: String,
        override val lastModified: Long,
        override val folderId: String,
        override val width: Int,
        override val height: Int,
        override val size: Int,
        override val name: String,
        override val indexInResult: Int = -1,
        override val hideInGrid: Boolean = false,
        override val hideInViewer: Boolean = false,
        private val editedPath: String? = null
    ) : Media(id, path, lastModified, folderId, width, height, size, name, indexInResult) {

        val videoPath: String
            get() = editedPath ?: path

    }

    object Camera: Media("", "", 0, "", 0, 0, 0, "")

}

internal sealed class Folder(
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