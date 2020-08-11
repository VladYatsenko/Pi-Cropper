package com.yatsenko.imagepicker.picker

import android.app.Activity
import android.content.Context
import android.os.Environment
import androidx.fragment.app.Fragment
import com.yatsenko.imagepicker.picker.model.ImagePickType
import com.yatsenko.imagepicker.picker.model.PickerOptions
import com.yatsenko.imagepicker.picker.ui.PickerActivity

class ImagePicker private constructor(){

    companion object{
        const val INTENT_RESULT_DATA = "picker_activity_result"

        const val PICKER_REQUEST_CODE = 1211

        fun build(): ImagePicker {
            return ImagePicker()
        }

    }

    private val options: PickerOptions = PickerOptions()

    fun pickType(mode: ImagePickType): ImagePicker {
        options.setType(mode)
        return this
    }

    fun maxNum(maxNum: Int): ImagePicker {
        options.setMaxNum(maxNum)
        return this
    }

    fun cachePath(path: String?): ImagePicker? {
        options.setCachePath(path)
        return this
    }

    fun show(fragment: Fragment){
        checkCachePath(fragment.requireContext())
        PickerActivity.start(fragment, options)
    }

    fun show(activity: Activity){
        checkCachePath(activity)
        PickerActivity.start(activity, options)
    }

    private fun checkCachePath(context: Context) {
        if (options.getCachePath().isNullOrBlank()) {
            options.setCachePath(context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath)
        }
    }
}