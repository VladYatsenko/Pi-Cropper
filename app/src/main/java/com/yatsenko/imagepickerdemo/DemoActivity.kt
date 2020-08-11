package com.yatsenko.imagepickerdemo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yatsenko.imagepicker.picker.ImagePicker
import com.yatsenko.imagepicker.picker.model.ImageEntity
import com.yatsenko.imagepicker.picker.model.ImagePickType
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        titleTxt.setOnClickListener {
            ImagePicker.build()
                .maxNum(10)
                .pickType(ImagePickType.GALLERY)
                .show(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == ImagePicker.PICKER_REQUEST_CODE){
                val list = data?.extras?.getParcelableArrayList<ImageEntity>(ImagePicker.INTENT_RESULT_DATA)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}