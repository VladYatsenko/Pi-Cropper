package com.yatsenko.imagepicker.ui.viewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.ui.abstraction.BaseDialogFragment
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.ui.viewer.adapter.ImageViewerAdapter
import com.yatsenko.imagepicker.ui.viewer.adapter.ImageViewerAdapterListener
import com.yatsenko.imagepicker.ui.viewer.core.*
import com.yatsenko.imagepicker.ui.viewer.utils.Config
import com.yatsenko.imagepicker.ui.viewer.utils.Config.offscreenPageLimit
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionEndHelper
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionStartHelper
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.imagepicker.utils.extensions.findViewHolderByAdapterPosition
import com.yatsenko.imagepicker.widgets.BackgroundView
import com.yatsenko.imagepicker.widgets.imageview.Overlay

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

    }

    private val initKey by lazy { requireArguments().getString(MEDIA_ID, "") }
    private val adapter by lazy { ImageViewerAdapter(initKey, overlayHelper) }
    private var initPosition = RecyclerView.NO_POSITION

    private lateinit var pager: ViewPager2
    private lateinit var overlayView: ConstraintLayout
    private lateinit var background: BackgroundView

    private val overlayHelper by lazy { OverlayHelper() }

    private val viewModel: PickerViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application) }
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
                is AdapterResult.OnSelectImageClicked -> viewModel.selectImage(it.media)
                is AdapterResult.OnCropImageClicked -> router.openCropper(it.media)
            }
        }
        overlayView.addView(overlayHelper.provideView(overlayView))

        viewModel.state.observe(viewLifecycleOwner) {
            val list = it.media
            adapter.submitList(list)
            initPosition = list.indexOfFirst { it.id == initKey }
            pager.setCurrentItem(initPosition, false)
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
                val viewHolder = pager.findViewHolderByAdapterPosition<FullscreenViewHolder>(position) ?: return
                val id = viewHolder.data?.id
                val startView = id?.let { ViewerTransitionHelper.provide(it) }
                ViewerTransitionHelper.transition.keys.forEach {
                    it.alpha = if (startView == it) 0f else 1f
                }

                overlayHelper.onPageSelected(position, viewHolder)
            }
        }
    }

    override fun showFailure(message: String?) {
        super.showFailure(message)
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
        TransitionEndHelper.end(this, startView, viewHolder)
        overlayHelper.onRelease(viewHolder, view)
    }

    override fun onBackPressed() {
        if (TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating)
            return

        pager.findViewHolderByAdapterPosition<FullscreenViewHolder>(pager.currentItem)?.let { viewHolder ->
            exit(viewHolder, viewHolder.endView)
        }
    }

}
