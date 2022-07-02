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
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseFragment
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.utils.Router

class PiCropperFragment : BaseFragment() {

    companion object {

        internal const val ASPECT_RATIO = "ASPECT_RATIO"
        internal const val COLLECT_COUNT = "COLLECT_COUNT"
        internal const val ALL_IMAGES_FOLDER = "ALL_IMAGES_FOLDER"
        internal const val CIRCLE_CROP = "CIRCLE_CROP"
        internal const val FORCE_OPEN_EDITOR = "FORCE_OPEN_EDITOR"

        const val INTENT_PiCROPPER_RESULT = "intent_picropper_result"
        const val PiCROPPER_RESULT = "picropper_result"
        const val RESULT_MEDIA = "result_media"

        fun prepareOptions(
            aspectRatio: List<AspectRatio> = AspectRatio.defaultList,
            collectCount: Int = 10,
            allImagesFolder: String? = null,
            circleCrop: Boolean = false,
            forceOpenEditor: Boolean = false
        ): Bundle {
            return Bundle().apply {
                putParcelableArrayList(ASPECT_RATIO, ArrayList(aspectRatio))
                putInt(COLLECT_COUNT, collectCount)
                putString(ALL_IMAGES_FOLDER, allImagesFolder)
                putBoolean(CIRCLE_CROP, circleCrop)
                putBoolean(FORCE_OPEN_EDITOR, forceOpenEditor)
            }
        }

    }

    override val layoutId: Int
        get() = R.layout.fragment_picropper

    val router by lazy { Router(R.id.container, childFragmentManager) }

    val args by lazy { Arguments(
        aspectRatioList = (requireArguments().getParcelableArrayList<AspectRatio>(ASPECT_RATIO) as? List<AspectRatio>) ?: AspectRatio.defaultList,
        collectCount = requireArguments().getInt(COLLECT_COUNT, 1),
        circleCrop = requireArguments().getBoolean(CIRCLE_CROP, false),
        allImagesFolder = requireArguments().getString(ALL_IMAGES_FOLDER, getString(R.string.all_image_folder)),
        quality = 80,
        shouldForceOpenEditor = requireArguments().getBoolean(FORCE_OPEN_EDITOR, false)
    ) }

    private val viewModel: PickerViewModel by viewModels(
        factoryProducer = { ViewModelFactory(requireActivity().application, args) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router.openPicker()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    router.canGoBack -> router.goBack()
                    else -> {
                        isEnabled = false
                        if (requireActivity() is PiCropperActivity) {
                            requireActivity().finish()
                        } else {
                            router.clearBackStack()
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        })
    }

    fun provideResultToTarget() {
        sendResult(viewModel.selectedImages)
    }

    fun provideResultToTarget(media: Media) {
        sendResult(listOf(media))
    }

    private fun sendResult(list: List<Media>) {
        val bundle = Bundle().apply {
            putStringArrayList(RESULT_MEDIA, ArrayList(list.map { it.mediaPath }))
        }

        if (requireActivity() is PiCropperActivity) {
            val resultIntent = Intent()
            resultIntent.putExtra(INTENT_PiCROPPER_RESULT, bundle)
            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
            requireActivity().finish()
        } else {
            setFragmentResult(PiCROPPER_RESULT, bundle)
            requireActivity().onBackPressed()
        }

    }

}