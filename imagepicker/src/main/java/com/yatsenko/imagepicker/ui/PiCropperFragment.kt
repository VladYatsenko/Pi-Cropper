package com.yatsenko.imagepicker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.Arguments
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.CompressFormat
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseFragment
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.utils.Router
import com.yatsenko.imagepicker.utils.extensions.setupActionBar
import com.yatsenko.imagepicker.utils.extensions.setupBottomBar

class PiCropperFragment : BaseFragment() {

    companion object {

        internal const val ASPECT_RATIO = "ASPECT_RATIO"
        internal const val COLLECT_COUNT = "COLLECT_COUNT"
        internal const val ALL_IMAGES_FOLDER = "ALL_IMAGES_FOLDER"
        internal const val CIRCLE_CROP = "CIRCLE_CROP"
        internal const val FORCE_OPEN_EDITOR = "FORCE_OPEN_EDITOR"
        internal const val QUALITY = "QUALITY"
        internal const val COMPRESS_FORMAT = "COMPRESS_FORMAT"

        const val INTENT_PiCROPPER_RESULT = "intent_picropper_result"
        const val PiCROPPER_RESULT = "picropper_result"
        const val RESULT_MEDIA = "result_media"

        fun prepareOptions(
            aspectRatio: List<AspectRatio> = AspectRatio.defaultList,
            allImagesFolder: String? = null,
            collectCount: Int = 10,
            forceOpenEditor: Boolean = false,
            circleCrop: Boolean = false,
            quality: Int = 80,
            compressFormat: CompressFormat = CompressFormat.JPEG
        ): Bundle {
            return Bundle().apply {
                putParcelableArrayList(ASPECT_RATIO, ArrayList(aspectRatio))
                putString(ALL_IMAGES_FOLDER, allImagesFolder)
                putInt(COLLECT_COUNT, collectCount)
                putBoolean(CIRCLE_CROP, circleCrop)
                putBoolean(FORCE_OPEN_EDITOR, forceOpenEditor)
                putInt(QUALITY, quality)
                putParcelable(COMPRESS_FORMAT, compressFormat)
            }
        }

    }

    override val layoutId: Int
        get() = R.layout.fragment_picropper

    internal val router by lazy { Router(R.id.container, childFragmentManager) }

    internal val args by lazy { Arguments(
        aspectRatioList = (requireArguments().getParcelableArrayList<AspectRatio>(ASPECT_RATIO) as? List<AspectRatio>) ?: AspectRatio.defaultList,
        allImagesFolder = requireArguments().getString(ALL_IMAGES_FOLDER, getString(R.string.all_image_folder)),
        collectCount = requireArguments().getInt(COLLECT_COUNT, 10),
        circleCrop = requireArguments().getBoolean(CIRCLE_CROP, false),
        quality = requireArguments().getInt(QUALITY, 80),
        compressFormat = requireArguments().getParcelable<CompressFormat>(COMPRESS_FORMAT) ?: CompressFormat.JPEG,
        shouldForceOpenEditor = requireArguments().getBoolean(FORCE_OPEN_EDITOR, false)
    ) }

    private val viewModel: PickerViewModel by viewModels(
        factoryProducer = { ViewModelFactory(requireActivity().application, args) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router.openPicker()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().apply {
            setupActionBar()
            setupBottomBar()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    router.canGoBack -> router.goBack()
                    else -> {
                        isEnabled = false
                        router.clearBackStack()
                        requireActivity().onBackPressed()
                    }
                }
            }
        })
    }

    internal fun provideResultToTarget() {
        sendResult(viewModel.selectedImages)
    }

    internal fun provideResultToTarget(media: Media) {
        sendResult(listOf(media))
    }

    private fun sendResult(list: List<Media>) {
        val bundle = Bundle().apply {
            putStringArrayList(RESULT_MEDIA, ArrayList(list.map { it.mediaPath }))
        }

        setFragmentResult(PiCROPPER_RESULT, bundle)
        requireActivity().onBackPressed()
    }

}