package com.yatsenko.imagepicker.model

class ImageFolderEntity constructor(

    var folderId: String?,
    var folderName: String?,
    var firstImgPath: String? = null,
    var num: Int? = null

) {
    fun gainNum() {
        num?.plus(1)
    }

//    private val folderId: String? = null
//    private val folderName: String? = null
//    private val firstImgPath: String? = null
//    private val num = 0

}