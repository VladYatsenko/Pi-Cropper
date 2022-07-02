package com.yatsenko.imagepicker.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.Arguments
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.model.Media
import com.yatsenko.imagepicker.ui.abstraction.BaseFragment
import com.yatsenko.imagepicker.utils.Router
import com.yatsenko.imagepicker.utils.extensions.toList

class PiCropperFragment : BaseFragment() {

    companion object {

        internal const val ASPECT_RATIO = "ASPECT_RATIO"
        internal const val COLLECT_COUNT = "COLLECT_COUNT"
        internal const val ALL_IMAGES_FOLDER = "ALL_IMAGES_FOLDER"
        internal const val CIRCLE_CROP = "CIRCLE_CROP"
        internal const val FORCE_OPEN_EDITOR = "FORCE_OPEN_EDITOR"

        fun prepareOptions(
            aspectRatio: List<AspectRatio>,
            collectCount: Int,
            allImagesFolder: String?,
            circleCrop: Boolean,
            forceOpenEditor: Boolean
        ): Bundle {
            return Bundle().apply {
                putParcelableArray(ASPECT_RATIO, aspectRatio.toTypedArray())
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
        aspectRatioList = (requireArguments().getParcelableArray(ASPECT_RATIO) as? List<AspectRatio>) ?: AspectRatio.defaultList,
        collectCount = requireArguments().getInt(COLLECT_COUNT, 1),
        circleCrop = requireArguments().getBoolean(CIRCLE_CROP, false),
        allImagesFolder = requireArguments().getString(ALL_IMAGES_FOLDER, getString(R.string.all_image_folder)),
        quality = 80,
        shouldForceOpenEditor = requireArguments().getBoolean(FORCE_OPEN_EDITOR, false)
    ) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (router.backStackCount == 0) {
            router.openPicker()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    router.canGoBack -> router.goBack()
                    else -> {
                        isEnabled = false
                        if (requireActivity() is PiCropperActivity)
                            requireActivity().finish()
                        else requireActivity().onBackPressed()
                    }
                }
            }
        })
    }

    fun provideResultToTarget(media: Media) {

    }

}