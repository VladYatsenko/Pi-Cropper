package com.yatsenko.imagepicker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yatsenko.imagepicker.R

class PiCropperActivity : AppCompatActivity() {

    companion object {
        private val OPTIONS = "OPTIONS"

        internal fun intent(context: Context, options: Bundle): Intent {
            return Intent(context, PiCropperActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(OPTIONS, options)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picropper)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, PiCropperFragment().apply {
                arguments = intent.getBundleExtra(OPTIONS)
            })
            .addToBackStack(null)
            .commit()
    }

}