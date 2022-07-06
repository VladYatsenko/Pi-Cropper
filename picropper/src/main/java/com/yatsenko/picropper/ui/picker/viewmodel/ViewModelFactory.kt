package com.yatsenko.picropper.ui.picker.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yatsenko.picropper.model.Arguments
import com.yatsenko.picropper.ui.PiCropperFragment

internal class ViewModelFactory(
    private val application: Application,
    private val arguments: Arguments
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PickerViewModel(application, arguments) as T
    }

}