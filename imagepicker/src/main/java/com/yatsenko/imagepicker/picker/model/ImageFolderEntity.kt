package com.yatsenko.imagepicker.picker.model

class ImageFolderEntity constructor(

    var folderId: String?,
    var folderName: String?,
    var firstImgPath: String? = null,
    var num: Int? = null

) {
    fun gainNum() {
        num?.plus(1)
    }

}