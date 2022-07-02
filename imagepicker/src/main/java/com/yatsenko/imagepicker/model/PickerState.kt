package com.yatsenko.imagepicker.model

import com.yatsenko.imagepicker.widgets.crop.AspectRatioWrapper

data class PickerState(
    val folders: List<Folder>,
    val selectedFolder: Folder,
    val media: List<Media>
)

data class OverlayState(
    val media: Media?
)

data class CropperState(
    val selectedRatio: AspectRatio,
    val ratios: List<AspectRatioWrapper>
)