package com.yatsenko.picropper.ui.viewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.yatsenko.picropper.R
import com.yatsenko.picropper.model.AdapterResult
import com.yatsenko.picropper.model.Media
import com.yatsenko.picropper.ui.abstraction.BaseDialogFragment
import com.yatsenko.picropper.ui.picker.viewmodel.MediaViewModel
import com.yatsenko.picropper.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.picropper.ui.viewer.adapter.ImageViewerAdapter
import com.yatsenko.picropper.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.picropper.ui.viewer.utils.Config
import com.yatsenko.picropper.ui.viewer.utils.Config.offscreenPageLimit
import com.yatsenko.picropper.ui.viewer.utils.OverlayHelper
import com.yatsenko.picropper.utils.transition.TransitionEndHelper
import com.yatsenko.picropper.utils.transition.TransitionStartHelper
import com.yatsenko.picropper.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.picropper.utils.extensions.findViewHolderByAdapterPosition
import com.yatsenko.picropper.utils.transition.CropperTransitionHelper
import com.yatsenko.picropper.utils.transition.ViewerTransitionHelper
import com.yatsenko.picropper.widgets.BackgroundView

open class ImageViewerDialogFragment : BaseDialogFragment() {

    companion object {
        private const val MEDIA_ID = "media_id"

        fun show(mediaId: String, fragmentManager: FragmentManager) {
            val dialog = ImageViewerDialogFragment().also {
                it.arguments = Bundle().apply {
                    putString(MEDIA_ID, mediaId)
                }
            }
            dialog.show(fragmentManager)
        }

        private val viewerUserInputEnabled = MutableLiveData<Boolean>()

        fun setViewerUserInputEnabled(enable: Boolean) {
            if (viewerUserInputEnabled.value != enable) viewerUserInputEnabled.value = enable
        }
    }

    private val initKey by lazy { requireArguments().getString(MEDIA_ID, "") }
    private val adapter by lazy { ImageViewerAdapter(initKey, overlayHelper) }

    private lateinit var pager: ViewPager2
    private lateinit var overlayView: ConstraintLayout
    private lateinit var background: BackgroundView

    private var overlayRefreshGate = true

    private val overlayHelper by lazy { OverlayHelper(piCropFragment.args.single) }

    private val viewModel: MediaViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application, piCropFragment.args) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = layoutInflater.inflate(R.layout.fragment_image_viewer_dialog, container, false)

        pager = view.findViewById(R.id.pager)
        overlayView = view.findViewById(R.id.overlay)
        background = view.findViewById(R.id.background)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.listener = adapterListener
        (pager.getChildAt(0) as? RecyclerView?)?.let {
            it.clipChildren = false
            it.itemAnimator = null
        }
        pager.orientation = Config.VIEWER_ORIENTATION
        pager.registerOnPageChangeCallback(pagerCallback)
        pager.offscreenPageLimit = offscreenPageLimit
        pager.adapter = adapter

        overlayHelper.adapterResult = {
            when(it) {
                AdapterResult.GoBack -> onBackPressed()
                is AdapterResult.OnSelectImageClicked -> viewModel.selectMedia(it.media)
                is AdapterResult.OnCropImageClicked -> {
                    pager.findViewHolderByAdapterPosition<FullscreenViewHolder>(pager.currentItem)?.let { viewHolder ->
                        CropperTransitionHelper.put(it.media.id, viewHolder.endView as ImageView)
                        viewHolder.resetScale()
                    }
                    pager.postDelayed({
                        viewModel.prepareAspectRatio(it.media as Media.Image)
                        router.openCropper(it.media)
                    }, 200)
                }
                is AdapterResult.OnProvideImageClicked -> piCropFragment.provideResultToTarget(it.media)
            }
        }
        overlayView.addView(overlayHelper.provideView(overlayView))

        viewModel.viewerState.observe(viewLifecycleOwner) { list ->
            if (list.size > 1) return@observe
            overlayRefreshGate = list.size == 1 && viewModel.media.size > 1
            adapter.submitList(list)
        }

        viewModel.overlayState.observe(viewLifecycleOwner) {
            overlayHelper.submitData(it.media)
        }

        viewerUserInputEnabled.observe(viewLifecycleOwner) {
            pager.isUserInputEnabled = it ?: true
        }

    }


    private val adapterListener by lazy {
        object : ImageViewerAdapterListener {
            override fun onInit(viewHolder: FullscreenViewHolder) {
                TransitionStartHelper.start(
                    this@ImageViewerDialogFragment,
                    ViewerTransitionHelper.provide(initKey),
                    viewHolder
                )
                background.changeToBackgroundColor(Config.viewBackgroundColor)
            }

            override fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {
                background.updateBackgroundColor(fraction, Config.viewBackgroundColor, Color.TRANSPARENT)
            }

            override fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {
                background.changeToBackgroundColor(Config.viewBackgroundColor)
            }

            override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {
                exit(viewHolder, view)
            }
        }
    }

    private val pagerCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (overlayRefreshGate) {
                    overlayRefreshGate = false
                    return
                }
                viewModel.onFullscreenPageChanged(position)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.listener = null
        pager.unregisterOnPageChangeCallback(pagerCallback)
        pager.adapter = null
    }

    private fun exit(viewHolder: FullscreenViewHolder, view: View) {
        val startView = ViewerTransitionHelper.provide(viewHolder.data?.id ?: return)
        background.changeToBackgroundColor(Color.TRANSPARENT)
        TransitionEndHelper.end(this, startView, viewHolder) {
            viewModel.onFullscreenClosed()
        }
        overlayHelper.onRelease(viewHolder, view)
    }

    override fun onBackPressed() {
        if (TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating)
            return

        pager.findViewHolderByAdapterPosition<FullscreenViewHolder>(pager.currentItem)?.let { viewHolder ->
            if (viewHolder.resetScale())
                return

            exit(viewHolder, viewHolder.endView)
        }
    }

}
