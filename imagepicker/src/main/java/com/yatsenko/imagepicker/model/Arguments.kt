package com.yatsenko.imagepicker.model

data class Arguments(
    val aspectRatioList: List<AspectRatio>,
    val collectCount: Int,
    val circleCrop: Boolean,
    val allImagesFolder: String,
    val quality: Int,
    private val shouldForceOpenEditor: Boolean
) {

    val single: Boolean
        get() = collectCount == 1

    val forceOpenEditor: Boolean
        get() = shouldForceOpenEditor && single

}