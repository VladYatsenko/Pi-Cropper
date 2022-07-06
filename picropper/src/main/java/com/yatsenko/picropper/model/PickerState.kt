package com.yatsenko.picropper.model

import com.yatsenko.picropper.widgets.crop.AspectRatioWrapper

internal data class PickerState(
    val folders: List<Folder>,
    val selectedFolder: Folder,
    val media: List<Media>
)

internal data class OverlayState(
    val media: Media?
)

internal data class CropperState(
    val selectedRatio: AspectRatio,
    val ratios: List<AspectRatioWrapper>
)