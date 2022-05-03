package com.yatsenko.imagepicker.model

data class PickerState(
    val folders: List<Folder>,
    val selectedFolder: Folder,
    val media: List<Media>
)