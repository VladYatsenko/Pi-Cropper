package com.yatsenko.imagepicker.picker.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yatsenko.imagepicker.picker.ImagePicker
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.picker.listeners.OnImageClickListener
import com.yatsenko.imagepicker.picker.model.ImageEntity
import com.yatsenko.imagepicker.picker.model.ImageFolderEntity
import com.yatsenko.imagepicker.picker.model.PickerOptions
import com.yatsenko.imagepicker.picker.ui.adapter.HeaderStockOverviewAdapter
import com.yatsenko.imagepicker.picker.ui.adapter.ImageGripAdapter
import com.yatsenko.imagepicker.picker.utils.ImageDataModel
import com.yatsenko.imagepicker.picker.utils.checkPermissions
import com.yatsenko.imagepicker.picker.utils.checkRequestPermissionsResult
import com.yatsenko.imagepicker.viewer.StfalconImageViewer
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.placeholder_progress_loader.*
import java.util.concurrent.Executors

class PickerActivity : AppCompatActivity() {

    companion object {

        private const val REQUEST_CODE_PERMISSION_STORAGE = 110

        private const val OPTIONS = "options"

        fun start(activity: Activity, options: PickerOptions) {
            activity.startActivityForResult(getIntent(activity, options), ImagePicker.PICKER_REQUEST_CODE)
        }

        fun start(fragment: Fragment, options: PickerOptions) {
            fragment.startActivityForResult(getIntent(fragment.context, options), ImagePicker.PICKER_REQUEST_CODE)
        }

        private fun getIntent(context: Context?, options: PickerOptions): Intent {
            val intent = Intent(context, PickerActivity::class.java)
            intent.putExtra(OPTIONS, options)
            return intent
        }
    }

    private val mCachedThreadService = Executors.newCachedThreadPool()
    private var mHandler: Handler? = null

    private var mCurFolder: ImageFolderEntity? = null

    private var imageAdapter: ImageGripAdapter? = null
    private var folderAdapter: HeaderStockOverviewAdapter? = null

    private lateinit var viewer: StfalconImageViewer<ImageEntity>

    private lateinit var options: PickerOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)

        mHandler = Handler(mainLooper)

        parseIntent()
        setupRV()
        setupViews()

        doScanData()
    }

    private fun parseIntent() {
        options = intent.extras?.getParcelable(OPTIONS) ?: PickerOptions()
    }

    private fun setupRV() {
        imageAdapter = ImageGripAdapter()
        imageAdapter?.single = options.getMaxNum() == 1
        imageAdapter?.listener = object : OnImageClickListener {
            override fun onImageClickListener(position: Int) {
                if (options.getMaxNum() == 1) {
                    //open cropper
                } else {
                    //open fullscreen
                    viewer = StfalconImageViewer.Builder<ImageEntity>(this@PickerActivity, imageAdapter?.getData()?.toMutableList(), ::loadImage)
                        .withHiddenStatusBar(false)
                        .withStartPosition(position)
                        .withTransitionFrom(imageAdapter?.getImageViewByPosition(position))
                        .withImageChangeListener {
                            viewer.updateTransitionImage(imageAdapter?.getImageViewByPosition(it))
                        }
                        .show()
                }
            }

            override fun onCheckboxClickListener(position: Int) {
                imageAdapter?.getData()?.getOrNull(position)?.let {
                    val isSelected: Boolean = ImageDataModel.instance.hasDataInResult(it)

                    if (isSelected) {
                        ImageDataModel.instance.delDataFromResult(it)
                        imageAdapter?.notifyDataSetChanged()
                    } else {
                        if (ImageDataModel.instance.getResultNum() == options.getMaxNum()) {
                            showToast("limit ${options.getMaxNum()}")
                        } else {
                            ImageDataModel.instance.addDataToResult(it)
                            imageAdapter?.notifyDataSetChanged()
                        }
                    }
                }

                doneFab.also {
                    if (ImageDataModel.instance.getResultNum() == 0) it.hide() else it.show()
                }
            }
        }
        imageRV?.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        imageRV?.adapter = imageAdapter
    }

    private fun setupViews() {
        doneFab.hide()
        doneFab.setOnClickListener {
            returnAllSelectedImages()
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun doScanData() {
        val hasPermission: Boolean = checkPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_PERMISSION_STORAGE, R.string.dialog_imagepicker_permission_sdcard_message
        )
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

            mHandler?.post {
                setupToolbarSpinner()
            }

            onFolderChanged(ImageDataModel.instance.getAllFolderList().firstOrNull())
        })
    }

    private fun setupToolbarSpinner() {
        folderAdapter = HeaderStockOverviewAdapter(this, R.layout.item_folder, ImageDataModel.instance.getAllFolderList())
        folderSpinner?.adapter = folderAdapter

        folderSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onFolderChanged(folderAdapter?.getItem(position))
            }

        }
    }

    private fun onDataChanged(imagesByFolder: ArrayList<ImageEntity>?) {
        mHandler?.post {
            imageAdapter?.refreshData(imagesByFolder)
            imageRV?.scrollTo(0, 0)
        }
    }

    private fun onFolderChanged(folder: ImageFolderEntity?) {
        if (mCurFolder != null && folder != null && mCurFolder?.equals(folder) == true)
            return

        mCurFolder = folder

        mHandler?.post {
            folderAdapter?.getPosition(folder)?.let {
                folderSpinner?.setSelection(it)
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
        mHandler?.post { placeholderProgress?.visibility = View.VISIBLE }
    }

    private fun hideLoading() {
        mHandler?.post { placeholderProgress?.visibility = View.GONE }
    }

    private fun returnAllSelectedImages() {
        val intent = Intent()
        intent.putParcelableArrayListExtra(ImagePicker.INTENT_RESULT_DATA, ImageDataModel.instance.getResultList())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun loadImage(imageView: ImageView, image: ImageEntity?) {
        Glide.with(this)
            .load(image?.imagePath)
            .into(imageView)
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
