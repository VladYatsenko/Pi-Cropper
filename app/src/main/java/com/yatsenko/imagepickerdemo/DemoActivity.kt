package com.yatsenko.imagepickerdemo

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DemoActivity : AppCompatActivity() {

    private lateinit var crop: FloatingActionButton

    private val contract = registerForActivityResult(PiCropperContract) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        crop = findViewById(R.id.crop)
        crop.setOnClickListener {
            contract.launch(null)
        }

    }

}