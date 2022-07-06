package com.yatsenko.imagepicker.ui.picker.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.yatsenko.imagepicker.data.ImageReaderContract
import com.yatsenko.imagepicker.model.*
import com.yatsenko.imagepicker.data.DataProvider
import com.yatsenko.imagepicker.data.SimpleDataProvider
import com.yatsenko.imagepicker.widgets.crop.AspectRatioWrapper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class PickerViewModel(application: Application, private val arguments: Arguments) : AndroidViewModel(application) {

    private val imageReader by lazy { ImageReaderContract(application) }

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

    private val _selectedImages = mutableListOf<Media>()
    val selectedImages: List<Media>
        get() = _selectedImages

    private val _pickerState: MutableLiveData<PickerState> = MutableLiveData()
    val pickerState: LiveData<PickerState> = _pickerState

    private val _overlayState: MutableLiveData<OverlayState> = MutableLiveData()
    val overlayState: LiveData<OverlayState> = _overlayState

    val viewerState: LiveData<PagedList<Media>> = dataSourceFactory.toLiveData(pageSize = 1)

    private var fullscreenPosition: Int = -1

    private var croppingMedia: Media.Image? = null
    private var croppedMedia: Media.Image? = null

    private var ratioRawData: List<AspectRatioWrapper>

    private val _cropperState: MutableLiveData<CropperState> = MutableLiveData()
    val cropperState: LiveData<CropperState> = _cropperState

    init {
        ratioRawData = if (arguments.aspectRatioList.isEmpty()) {
            listOf(AspectRatioWrapper(AspectRatio.Custom(1, 1), false))
        } else arguments.aspectRatioList.map { AspectRatioWrapper.createFrom(it) }

        _cropperState.postValue(CropperState(ratioRawData.first().ratio, ratioRawData))
    }

    fun extractImages() {
        viewModelScope.launch(errorHandler {}) {
            rawData = imageReader.extractImages(arguments.allImagesFolder)

            if (pickerStateData.selectedFolder == noopFolder) {
                pickerStateData = pickerStateData.copy(
                    folders = rawData.first,
                    media = rawData.second
                )
                changeFolder(rawData.first.first())
            } else {
                refreshFolderImages()
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
                val withoutImage = _selectedImages.filterNot { it.id == media.id }
                _selectedImages.clear()
                _selectedImages.addAll(withoutImage)
            }
            else -> {
                if (_selectedImages.size < arguments.collectCount)
                    _selectedImages.add(media)
            }
        }

        rawData = Pair(rawData.first, rawData.second.mapIndexed(::remapSelectedImage))
        refreshFolderImages()
        refreshOverlay()
        refreshViewer()
    }

    fun openFullscreen(position: Int) {
        fullscreenPosition = position
        refreshFolderImages()
        refreshOverlay()
        refreshViewer()
    }

    fun onFullscreenPageChanged(position: Int) {
        if (fullscreenPosition == position) return

        fullscreenPosition = position
        refreshFolderImages()
        refreshOverlay()
    }

    fun onFullscreenClosed() {
        fullscreenPosition = -1
        refreshFolderImages()
        refreshOverlay()
    }

    fun prepareAspectRatio(media: Media.Image) {
        croppingMedia = media
        refreshFolderImages()
        refreshOverlay()

        ratioRawData = if (ratioRawData.isEmpty()) {
            listOf(AspectRatioWrapper(AspectRatio.Custom(1, 1), false))
        } else ratioRawData.map {
            AspectRatioWrapper.createFrom(AspectRatio.remapOriginal(it.ratio, media))
        }

        selectAspectRatio(ratioRawData.first())
    }

    fun selectAspectRatio(item: AspectRatioWrapper) {
        val preparedList = changeSelectedAspectRatio(ratioRawData, item)
        _cropperState.postValue(CropperState(item.ratio, preparedList))
    }

    private fun changeSelectedAspectRatio(list: List<AspectRatioWrapper>, item: AspectRatioWrapper): List<AspectRatioWrapper> {
        return list.map { AspectRatioWrapper(it.ratio, it.ratio == item.ratio) }
    }

    fun setCroppedImage(media: Media.Image, croppedImage: Media.Image) {
        this.croppingMedia = media
        this.croppedMedia = croppedImage
    }

    fun imageCropped() {
        if (croppedMedia != null) {
            val indexInResult = if (_selectedImages.isEmpty()) 0 else _selectedImages.size
            val updatedMedia = croppingMedia!!.copy(indexInResult = indexInResult, croppedImage = croppedMedia)
            _selectedImages.add(updatedMedia)
            val index = rawData.second.indexOfFirst { it.id == croppingMedia?.id }
            if (index != -1) {
                val mutableList = rawData.second.toMutableList()
                mutableList.removeAt(index)
                mutableList.add(index, updatedMedia)
                rawData = Pair(rawData.first, mutableList)

            }
            croppedMedia = null
        }
        croppingMedia = null
        refreshFolderImages()
        refreshOverlay()
        refreshViewer()
    }

    fun onCropClosed() {
//        croppingMedia = null
//        refreshFolderImages()
//        refreshOverlay()
    }

    private fun refreshFolderImages(folder: Folder = pickerStateData.selectedFolder) {
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
        val indexInResult = _selectedImages.indexOfFirst { it.id == image.id }
        val inFullscreen = index == fullscreenPosition
        val inCropping = image.id == croppingMedia?.id

        return if (image.indexInResult == indexInResult && image.hideInGrid == inFullscreen && image.hideInViewer == inCropping)
            image
        else {
            return when (image) {
                is Media.Image -> {
                    val croppedImage = if (indexInResult != -1) image.croppedImage else null
                    image.copy(indexInResult = indexInResult, hideInGrid = inFullscreen, hideInViewer = inCropping, croppedImage = croppedImage)
                }
                is Media.SubsamplingImage -> image.copy(indexInResult = indexInResult, hideInGrid = inFullscreen)
                is Media.Video -> image.copy(indexInResult = indexInResult, hideInGrid = inFullscreen)
            }
        }
    }

    private fun errorHandler(callback: (Throwable) -> Unit): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            callback(throwable)
        }
    }

}