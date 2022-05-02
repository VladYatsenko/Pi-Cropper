package com.yatsenko.imagepicker.ui.picker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yatsenko.imagepicker.data.ImageReaderContract
import com.yatsenko.imagepicker.model.Folder
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.model.PickerState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PickerViewModel(application: Application) : AndroidViewModel(application) {

    private val imageReader by lazy { ImageReaderContract(application) }

    private var rawData: Pair<List<Folder>, List<Image>> = Pair(emptyList(), emptyList())
    private val noopFolder = Folder.All("")
    private var imageState = PickerState(
        folders = emptyList(),
        selectedFolder = noopFolder,
        images = emptyList()
    )

    private val selectedImages = mutableListOf<Image>()

    private val _state: MutableLiveData<PickerState> = MutableLiveData()
    val state: LiveData<PickerState> = _state

    var fullscreenPosition: Int = -1

    val images: List<Image>
        get() = imageState.images

    init {
        Log.i("PickerViewModel", "init()")
    }

    fun extractImages() {
        viewModelScope.launch(errorHandler {}) {
            rawData = imageReader.extractImages()

            if (imageState.selectedFolder == noopFolder) {
                imageState = imageState.copy(
                    folders = rawData.first,
                    images = rawData.second
                )
                changeFolder(rawData.first.first())
            }
        }
    }

    fun changeFolder(folder: Folder) {
        if (imageState.selectedFolder == folder)
            return

        refreshFolderImages(folder)
    }

    fun selectImage(image: Image) {
        when (image.isSelected) {
            true -> {
                val withoutImage = selectedImages.filterNot { it.id == image.id }
                selectedImages.clear()
                selectedImages.addAll(withoutImage)
            }
            else -> selectedImages.add(image)
        }

        rawData = Pair(rawData.first, rawData.second.map(::remapSelectedImage))
        refreshFolderImages(imageState.selectedFolder)
    }

    private fun refreshFolderImages(folder: Folder) {
        val images = when (folder) {
            is Folder.All -> rawData.second
            is Folder.Common -> rawData.second.filter { it.folderId == folder.id }.distinctBy { it.lastModified }
        }
        imageState = imageState.copy(
            selectedFolder = folder,
            images = images
        )
        _state.postValue(imageState)
    }

    private fun remapSelectedImage(image: Image): Image {
        val isSelected = selectedImages.firstOrNull { it.id == image.id } != null
        val index = selectedImages.indexOfFirst { it.id == image.id }

        return if (image.isSelected == isSelected && image.indexInResult == index)
            image
        else image.copy(isSelected = isSelected, indexInResult = index)
    }

    private fun errorHandler(callback: (Throwable) -> Unit): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            callback(throwable)
        }
    }


}