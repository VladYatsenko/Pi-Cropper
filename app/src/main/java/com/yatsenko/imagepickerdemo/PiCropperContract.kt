package com.yatsenko.imagepickerdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.yatsenko.imagepicker.core.PiCropper

object PiCropperContract: ActivityResultContract<Any?, List<Uri>?>() {

    override fun createIntent(context: Context, input: Any?): Intent {
        return PiCropper.builder(context)
            .collectCount(10)
            .allImagesFolder("Full list")
            .forceOpenEditor(false)
            .intent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri>? {
        if (intent == null || resultCode != Activity.RESULT_OK) return null
        return parseIntent(intent)
    }

    private fun parseIntent(intent: Intent?): List<Uri> {
        return emptyList()
    }

}