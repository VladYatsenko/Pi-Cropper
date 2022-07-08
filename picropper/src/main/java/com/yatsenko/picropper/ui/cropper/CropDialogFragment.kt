package com.yatsenko.picropper.ui.cropper

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.yatsenko.picropper.R
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.abstraction.BaseDialogFragment
import com.yatsenko.picropper.ui.cropper.holder.UCropIml
import com.yatsenko.picropper.ui.picker.viewmodel.MediaViewModel
import com.yatsenko.picropper.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.picropper.ui.viewer.utils.Config
import com.yatsenko.picropper.utils.ImageLoader
import com.yatsenko.picropper.utils.extensions.dpToPxInt
import com.yatsenko.picropper.utils.extensions.invisible
import com.yatsenko.picropper.utils.extensions.loadImage
import com.yatsenko.picropper.utils.extensions.visible
import com.yatsenko.picropper.utils.transition.*
import com.yatsenko.picropper.widgets.BackgroundView
import com.yatsenko.picropper.widgets.crop.CropToolsView

internal class CropDialogFragment : BaseDialogFragment() {

    companion object {
        private const val CROP_URL = "crop_url"
        fun show(media: Media, fragmentManager: FragmentManager) {
            val dialog = CropDialogFragment().apply {
                this.arguments = Bundle().apply {
                    putSerializable(CROP_URL, media)
                }
            }
            dialog.show(fragmentManager)
        }
    }

    private val media by lazy { requireArguments().getSerializable(CROP_URL) as Media.Image }
    private val crop by lazy {
        UCropIml(requireContext(), Uri.parse(media.path), piCropFragment.args, ::handleResult).also {
            lifecycle.addObserver(it)
        }
    }

    private lateinit var root: ConstraintLayout
    private lateinit var cropTools: CropToolsView
    private lateinit var background: BackgroundView
    private lateinit var cropTransitionOverlay: ImageView

    private val transitionHelper: TransitionHelper
        get() = if (piCropFragment.args.forceOpenEditor) ViewerTransitionHelper else CropperTransitionHelper

    private val viewModel: MediaViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application, piCropFragment.args) }
    )

    private val exitHandler = Handler(Looper.getMainLooper())
    private val exitRunnable = Runnable {
        if (piCropFragment.args.forceOpenEditor)
            background.changeToBackgroundColor(Color.TRANSPARENT)

        val startView = transitionHelper.provide(media.id)
        TransitionEndHelper.end(this, startView, transitionEnd) {
            if (!piCropFragment.args.forceOpenEditor)
                background.changeToBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = layoutInflater.inflate(R.layout.fragment_image_cropper_dialog, container, false)
        root = view.findViewById(R.id.root)
        val cropContainer: FrameLayout = view.findViewById(R.id.crop_container)
        cropContainer.addView(crop.cropView)
        cropTools = view.findViewById(R.id.bottom_view)
        background = view.findViewById(R.id.background)
        cropTransitionOverlay = view.findViewById(R.id.crop_transition_overlay)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cropTools.result = ::handleResult
        viewModel.cropperState.observe(viewLifecycleOwner) {
            cropTools.data = CropToolsView.Data(it.ratios)
            crop.applyRatio(it.selectedRatio, it.selectedRatio.isDynamic)
        }

        crop.cropView.invisible()
        ImageLoader.load(cropTransitionOverlay, media)

        root.post {
            cropTools.show()
            background.changeToBackgroundColor(Config.viewBackgroundColor)
            TransitionStartHelper.start(this, transitionHelper.provide(media.id), transitionStart)
        }
    }

    private val transitionStart = object : TransitionStart {

        override fun beforeTransitionStart(startView: View?) {
            cropTransitionOverlay.scaleType = (startView as? ImageView?)?.scaleType ?: ImageView.ScaleType.FIT_CENTER
            cropTransitionOverlay.layoutParams = cropTransitionOverlay.layoutParams.apply {
                width = startView?.width ?: width
                height = startView?.height ?: height
                val location = IntArray(2)
                TransitionStartHelper.getLocationOnScreen(startView, location)
                if (this is ViewGroup.MarginLayoutParams) {
                    marginStart = location[0]
                    topMargin = location[1] - Config.transitionOffsetY
                }
            }
        }

        override fun afterTransitionStart() {
            crop.load()
        }

        override fun transitionStart() {
            cropTransitionOverlay.scaleType = ImageView.ScaleType.FIT_CENTER
            cropTransitionOverlay.layoutParams = cropTransitionOverlay.layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = 0
                if (this is ViewGroup.MarginLayoutParams) {
                    val margin = dpToPxInt(16f)
                    marginStart = margin
                    marginEnd = margin
                    topMargin = margin
                    bottomMargin = 0
                }

                if (this is ConstraintLayout.LayoutParams) {
                    this.topToTop = PARENT_ID
                    this.bottomToTop = cropTools.id
                }
            }
        }

        override val viewGroup: ViewGroup
            get() = root

    }

    private val transitionEnd = object : TransitionEnd {

        override fun beforeTransitionEnd(startView: View?) {}

        override fun transitionEnd(startView: View?) {
            cropTransitionOverlay.scaleType = (startView as? ImageView?)?.scaleType
                ?: ImageView.ScaleType.FIT_CENTER
            cropTransitionOverlay.translationX = 0f
            cropTransitionOverlay.translationY = 0f
            cropTransitionOverlay.scaleX = if (startView != null) 1f else 2f
            cropTransitionOverlay.scaleY = if (startView != null) 1f else 2f
            // photoView.alpha = startView?.alpha ?: 0f
            fade(startView)
            cropTransitionOverlay.layoutParams = cropTransitionOverlay.layoutParams.apply {
                if (this is ConstraintLayout.LayoutParams) {
                    this.bottomToTop = UNSET

                    if (piCropFragment.args.forceOpenEditor) {
                        width = startView?.width ?: width
                        height = startView?.height ?: height
                    } else {
                        this.startToStart = PARENT_ID
                        this.endToEnd = PARENT_ID
                        this.bottomToBottom = PARENT_ID
                    }
                }

                val location = IntArray(2)
                TransitionEndHelper.getLocationOnScreen(startView, location)
                if (this is ViewGroup.MarginLayoutParams) {
                    marginStart = location[0]
                    topMargin = location[1] - Config.transitionOffsetY
                    marginEnd = 0
                    bottomMargin = 0
                }
            }

            viewModel.imageCropped()
        }

        override fun fade(startView: View?) {
            cropTransitionOverlay.alpha = 1f

            crop.cropView.animate()
                .setDuration(50)
                .alpha(0f).start()

            cropTools.hide()
        }

        override val viewGroup: ViewGroup
            get() = root

    }

    private fun handleResult(result: AdapterResult) {
        when (result) {
            is AdapterResult.OnAspectRatioClicked -> viewModel.selectAspectRatio(result.item)
            is AdapterResult.OnRotateStart -> crop.onRotateStart()
            is AdapterResult.OnRotateProgress -> crop.onRotate(result.deltaAngle)
            is AdapterResult.OnRotateEnd -> crop.onRotateEnd()
            is AdapterResult.OnRotate90Clicked -> crop.onRotate(90f)
            is AdapterResult.OnResetRotationClicked -> crop.onResetRotation()
            is AdapterResult.OnImageRotated -> cropTools.rotateAngel = result.angle
            is AdapterResult.OnApplyCrop -> {
                cropTools.showLoading()
                crop.crop()
            }
            is AdapterResult.OnCancelCrop -> onBackPressed()
            is AdapterResult.OnImageCropped -> {
                when {
                    piCropFragment.args.single -> {
                        piCropFragment.provideResultToTarget(result.media)
                    }
                    else -> {
                        cropTransitionOverlay.alpha = 1f
                        cropTransitionOverlay.loadImage(result.media, ::onBackPressed)
                        viewModel.setCroppedImage(media, result.media)
                    }
                }
            }
            is AdapterResult.OnCropImageLoaded -> {
                crop.cropView.visible()
                cropTransitionOverlay.animate()
                    .setDuration(200)
                    .alpha(0f)
                    .start()
            }
        }
    }

    override fun onBackPressed() {
        if (TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating)
            return

        if (exitHandler.hasCallbacks(exitRunnable))
            return

        exitHandler.postDelayed(exitRunnable, if (crop.exitCrop()) 200 else 0)
    }

}