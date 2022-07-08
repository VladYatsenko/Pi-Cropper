package com.yatsenko.picropper.ui.picker.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.yatsenko.picropper.data.ImageReaderContract
import com.yatsenko.picropper.model.*
import com.yatsenko.picropper.data.DataProvider
import com.yatsenko.picropper.data.MediaDataProvider
import com.yatsenko.picropper.widgets.crop.AspectRatioWrapper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class MediaViewModel(application: Application, private val arguments: Arguments) : AndroidViewModel(application) {

    private val imageReader by lazy { ImageReaderContract(application) }

    private val dataProvider: DataProvider
        get() = MediaDataProvider(media)
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
        grid = emptyList(),
        viewer = emptyList()
    )

    private var overlayStateData = OverlayState(media = null)

    val media: List<Media>
        get() = pickerStateData.grid

    private val _selectedImages = mutableListOf<Media>()
    val selectedImages: List<Media>
        get() = _selectedImages

    private val _pickerState: MutableLiveData<PickerState> = MutableLiveData()
    val pickerState: LiveData<PickerState> = _pickerState

    private val _overlayState: MutableLiveData<OverlayState> = MutableLiveData()
    val overlayState: LiveData<OverlayState> = _overlayState

    val viewerState: LiveData<PagedList<Media>> = dataSourceFactory.toLiveData(pageSize = 1)

    private var fullscreenId: String? = null

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
                    grid = rawData.second
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

    fun openFullscreen(id: String) {
        fullscreenId = id
        refreshFolderImages()
        refreshOverlay()
        refreshViewer()
    }

    fun onFullscreenPageChanged(position: Int) {
        val itemId = pickerStateData.viewer[position].id
        if (fullscreenId == itemId) return

        fullscreenId = itemId
        refreshFolderImages()
        refreshOverlay()
    }

    fun onFullscreenClosed() {
        fullscreenId = null
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

    private fun refreshFolderImages(folder: Folder = pickerStateData.selectedFolder) {
        val images = when (folder) {
            is Folder.All -> rawData.second
            is Folder.Common -> rawData.second.filter { it.folderId == folder.id }.distinctBy { it.lastModified }
        }.mapIndexed(::remapSelectedImage).toMutableList()

        val grid = if (arguments.camera && folder is Folder.All) {
            images.add(0, Media.Camera)
            images
        } else images

        pickerStateData = pickerStateData.copy(
            selectedFolder = folder,
            grid = grid,
            viewer = images
        )
        _pickerState.postValue(pickerStateData)
    }

    private fun refreshViewer() {
        val item = pickerStateData.grid.firstOrNull { it.id == fullscreenId } ?: return
        snapshot = listOf(item)
        dataSource?.invalidate()
    }

    private fun refreshOverlay() {
        overlayStateData = overlayStateData.copy(media = pickerStateData.grid.firstOrNull { it.id == fullscreenId })
        _overlayState.postValue(overlayStateData)
    }

    private fun remapSelectedImage(index: Int, media: Media): Media {
        val indexInResult = _selectedImages.indexOfFirst { it.id == media.id }
        val inFullscreen = media.id == fullscreenId
        val inCropping = media.id == croppingMedia?.id

        return if (media.indexInResult == indexInResult && media.hideInGrid == inFullscreen && media.hideInViewer == inCropping)
            media
        else {
            return when (media) {
                is Media.Image -> {
                    val croppedImage = if (indexInResult != -1) media.croppedImage else null
                    media.copy(indexInResult = indexInResult, hideInGrid = inFullscreen, hideInViewer = inCropping, croppedImage = croppedImage)
                }
                is Media.SubsamplingImage -> media.copy(indexInResult = indexInResult, hideInGrid = inFullscreen)
                is Media.Video -> media.copy(indexInResult = indexInResult, hideInGrid = inFullscreen)
                is Media.Camera -> media
            }
        }
    }

    private fun errorHandler(callback: (Throwable) -> Unit): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            callback(throwable)
        }
    }

}