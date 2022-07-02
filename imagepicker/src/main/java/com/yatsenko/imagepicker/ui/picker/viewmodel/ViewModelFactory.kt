package com.yatsenko.imagepicker.ui.picker.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yatsenko.imagepicker.model.Arguments
import com.yatsenko.imagepicker.ui.PiCropperFragment

class ViewModelFactory(
    private val application: Application,
    private val arguments: Arguments
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PickerViewModel(application, arguments) as T
    }

}