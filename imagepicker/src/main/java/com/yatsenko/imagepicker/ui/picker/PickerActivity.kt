package com.yatsenko.imagepicker.ui.picker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.ImageEntity
import com.yatsenko.imagepicker.model.ImageFolderEntity
import com.yatsenko.imagepicker.ui.picker.adapter.HeaderStockOverviewAdapter
import com.yatsenko.imagepicker.ui.picker.adapter.ImageGripAdapter
import com.yatsenko.imagepicker.utils.ImageDataModel
import com.yatsenko.imagepicker.utils.checkPermissions
import com.yatsenko.imagepicker.utils.checkRequestPermissionsResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.placeholder_progress_loader.*
import java.util.concurrent.Executors

class PickerActivity : AppCompatActivity() {

    companion object {

        const val REQUEST_CODE_PERMISSION_STORAGE = 110


        fun start(context: Context?) {
            context?.startActivity(Intent(context, PickerActivity::class.java))
        }
    }

    private val mCachedThreadService = Executors.newCachedThreadPool()
    private var mHandler: Handler? = null

    private var mCurFolder: ImageFolderEntity? = null

    private var imageAdapter: ImageGripAdapter? = null
    private var folderAdapter: HeaderStockOverviewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHandler = Handler(mainLooper)

        setupRV()

        doScanData()
    }

    private fun setupRV() {
        imageAdapter = ImageGripAdapter()
        imageRV.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        imageRV.adapter = imageAdapter
    }

    private fun doScanData() {
        val hasPermission: Boolean = checkPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION_STORAGE, R.string.dialog_imagepicker_permission_sdcard_message)
        if (hasPermission) {
            scanData()
        }
    }

    private fun scanData() {
        addNewRunnable(Runnable {
            showLoading()
            val success: Boolean = ImageDataModel.instance.scanAllData(this)
            hideLoading()
            if (!success)
                showToast("Scan failed")

            setupToolbarSpinner()

            onFolderChanged(ImageDataModel.instance.getAllFolderList().firstOrNull())
        })
    }

    private fun setupToolbarSpinner() {
        folderAdapter = HeaderStockOverviewAdapter(this, R.layout.item_folder, ImageDataModel.instance.getAllFolderList())
        folderSpinner.adapter = folderAdapter

        folderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onFolderChanged(folderAdapter?.getItem(position))
            }

        }
    }

    private fun onDataChanged(imagesByFolder: ArrayList<ImageEntity>?) {
        mHandler?.post {
            imageAdapter?.refreshData(imagesByFolder)
            imageRV.scrollTo(0, 0)
        }
    }

    private fun onFolderChanged(folder: ImageFolderEntity?) {
        if (mCurFolder != null && folder != null && mCurFolder?.equals(folder) == true)
            return

        mCurFolder = folder

        mHandler?.post {
            folderAdapter?.getPosition(folder)?.let {
                folderSpinner.setSelection(it)
            }
        }

        checkDataByFloder(folder)
    }

    private fun checkDataByFloder(folder: ImageFolderEntity?) {
        addNewRunnable(Runnable {
            onDataChanged(ImageDataModel.instance.getImagesByFolder(folder))
        })
    }

    private fun addNewRunnable(runnable: Runnable) {
        mCachedThreadService.execute(runnable)
    }

    private fun showToast(text: String?) {
        mHandler?.post { Toast.makeText(this, text ?: "", Toast.LENGTH_SHORT).show() }
    }

    private fun showLoading() {
        mHandler?.post { placeholderProgress.visibility = View.VISIBLE }
    }

    private fun hideLoading() {
        mHandler?.post { placeholderProgress.visibility = View.GONE }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_STORAGE -> {
                val result = checkRequestPermissionsResult(
                    permissions, grantResults, false, R.string.dialog_imagepicker_permission_sdcard_nerver_ask_message
                )
                if (result?.get(0) == true)
                    scanData()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mCachedThreadService.shutdownNow()
            ImageDataModel.instance.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
