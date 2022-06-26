package com.yatsenko.imagepicker.ui.picker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.yatsenko.imagepicker.data.ImageReaderContract
import com.yatsenko.imagepicker.model.Folder
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.model.OverlayState
import com.yatsenko.imagepicker.model.PickerState
import com.yatsenko.imagepicker.ui.viewer.core.DataProvider
import com.yatsenko.imagepicker.ui.viewer.core.SimpleDataProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PickerViewModel(application: Application) : AndroidViewModel(application) {

    private val imageReader by lazy { ImageReaderContract(application) }

    private var rawData: Pair<List<Folder>, List<Media>> = Pair(emptyList(), emptyList())
    private val noopFolder = Folder.All("")
    private var pickerStateData = PickerState(
        folders = emptyList(),
        selectedFolder = noopFolder,
        media = emptyList()
    )

    private var overlayStateData = OverlayState(media = null)

    val media: List<Media>
        get() = pickerStateData.media
    private val selectedImages = mutableListOf<Media>()

    private val _pickerState: MutableLiveData<PickerState> = MutableLiveData()
    val pickerState: LiveData<PickerState> = _pickerState

    private val _overlayState: MutableLiveData<OverlayState> = MutableLiveData()
    val overlayState: LiveData<OverlayState> = _overlayState

    private var fullscreenPosition: Int = -1

    private val dataProvider: DataProvider
        get() = SimpleDataProvider(media)
    private val lock = Any()
    private var snapshot: List<Media>? = null
    private var dataSource: DataSource<String, Media>? = null
    private val dataSourceFactory = object : DataSource.Factory<String, Media>() {
        override fun create() = dataSource().also { dataSource = it }
    }

    private fun dataSource() = object : ItemKeyedDataSource<String, Media>() {

        override fun getKey(item: Media) = item.id

        override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<Media>
        ) {
            val result: List<Media>
            synchronized(lock) {
                result = snapshot ?: dataProvider.loadInitial()
                snapshot = result
            }
            callback.onResult(result, 0, result.size)
        }

        override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Media>) {
            dataProvider.loadAfter(params.key) {
                synchronized(lock) {
                    snapshot = snapshot?.toMutableList()?.apply { addAll(it) }
                }
                callback.onResult(it)
            }
        }

        override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Media>) {
            dataProvider.loadBefore(params.key) {
                synchronized(lock) {
                    snapshot = snapshot?.toMutableList()?.apply { addAll(0, it) }
                }
                callback.onResult(it)
            }
        }
    }

    val viewerState: LiveData<PagedList<Media>> = dataSourceFactory.toLiveData(pageSize = 1)

    init {
        Log.i("PickerViewModel", "init()")
    }

    fun extractImages() {
        viewModelScope.launch(errorHandler {}) {
            rawData = imageReader.extractImages()

            if (pickerStateData.selectedFolder == noopFolder) {
                pickerStateData = pickerStateData.copy(
                    folders = rawData.first,
                    media = rawData.second
                )
                changeFolder(rawData.first.first())
            }
        }
    }

    fun changeFolder(folder: Folder) {
        if (pickerStateData.selectedFolder == folder)
            return

        refreshFolderImages(folder)
    }

    fun selectMedia(media: Media) {
        when (media.isSelected) {
            true -> {
                val withoutImage = selectedImages.filterNot { it.id == media.id }
                selectedImages.clear()
                selectedImages.addAll(withoutImage)
            }
            else -> selectedImages.add(media)
        }

        rawData = Pair(rawData.first, rawData.second.mapIndexed(::remapSelectedImage))
        refreshFolderImages(pickerStateData.selectedFolder)
        refreshOverlay()
        refreshViewer()
    }

    fun openFullscreen(position: Int) {
        fullscreenPosition = position
        refreshFolderImages(pickerStateData.selectedFolder)
        refreshOverlay()
        refreshViewer()
    }

    fun onFullscreenPageChanged(position: Int) {
        if (fullscreenPosition == position) return

        fullscreenPosition = position
        refreshFolderImages(pickerStateData.selectedFolder)
        refreshOverlay()
    }

    fun onFullscreenClosed() {
        fullscreenPosition = -1
        refreshFolderImages(pickerStateData.selectedFolder)
        refreshOverlay()
    }

    fun imageCropped(media: Media.Image, croppedImage: Media.Image) {
        val indexInResult = if(selectedImages.isEmpty()) 0 else selectedImages.size
        val updatedMedia = media.copy(indexInResult = indexInResult, croppedImage = croppedImage)
        selectedImages.add(updatedMedia)
        val index = rawData.second.indexOfFirst { it.id == media.id }
        if (index != -1) {
            val mutableList = rawData.second.toMutableList()
            mutableList.removeAt(index)
            mutableList.add(index, updatedMedia)
            rawData = Pair(rawData.first, mutableList)
            refreshFolderImages(pickerStateData.selectedFolder)
            refreshOverlay()
            refreshViewer()
        }
    }

    private fun refreshFolderImages(folder: Folder) {
        val images = when (folder) {
            is Folder.All -> rawData.second
            is Folder.Common -> rawData.second.filter { it.folderId == folder.id }.distinctBy { it.lastModified }
        }.mapIndexed(::remapSelectedImage)
        pickerStateData = pickerStateData.copy(
            selectedFolder = folder,
            media = images
        )
        _pickerState.postValue(pickerStateData)
    }

    private fun refreshViewer() {
        val item = pickerStateData.media.getOrNull(fullscreenPosition) ?: return
        snapshot = listOf(item)
        dataSource?.invalidate()
    }

    private fun refreshOverlay() {
        overlayStateData = overlayStateData.copy(media = pickerStateData.media.getOrNull(fullscreenPosition))
        _overlayState.postValue(overlayStateData)
    }

    private fun remapSelectedImage(index: Int, image: Media): Media {
        val indexInResult = selectedImages.indexOfFirst { it.id == image.id }
        val inFullscreen = index == fullscreenPosition

        return if (image.indexInResult == indexInResult && image.inFullscreen == inFullscreen)
            image
        else {
            return when (image) {
                is Media.Image -> {
                    val croppedImage = if (indexInResult != -1) image.croppedImage else null
                    image.copy(indexInResult = indexInResult, inFullscreen = inFullscreen, croppedImage = croppedImage)
                }
                is Media.SubsamplingImage -> image.copy(indexInResult = indexInResult, inFullscreen = inFullscreen)
                is Media.Video -> image.copy(indexInResult = indexInResult, inFullscreen = inFullscreen)
            }
        }
    }

    private fun errorHandler(callback: (Throwable) -> Unit): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            callback(throwable)
        }
    }


}