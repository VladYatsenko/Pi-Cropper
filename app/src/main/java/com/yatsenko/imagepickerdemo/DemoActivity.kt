package com.yatsenko.imagepickerdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yatsenko.imagepicker.ui.picker.PickerActivity

class DemoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        PickerActivity.start(this)
    }
}