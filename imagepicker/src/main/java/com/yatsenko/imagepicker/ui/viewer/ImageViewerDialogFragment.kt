package com.yatsenko.imagepicker.ui.viewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.picker.viewmodel.PickerViewModel
import com.yatsenko.imagepicker.ui.picker.viewmodel.ViewModelFactory
import com.yatsenko.imagepicker.ui.viewer.adapter.ImageViewerAdapter
import com.yatsenko.imagepicker.ui.viewer.core.Components
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireInitKey
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireOverlayCustomizer
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireTransformer
import com.yatsenko.imagepicker.ui.viewer.core.Components.requireViewerCallback
import com.yatsenko.imagepicker.ui.viewer.utils.Config
import com.yatsenko.imagepicker.ui.viewer.utils.Config.offscreenPageLimit
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionEndHelper
import com.yatsenko.imagepicker.ui.viewer.utils.TransitionStartHelper
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.imagepicker.utils.extensions.findViewHolderByAdapterPosition
import com.yatsenko.imagepicker.widgets.BackgroundView

open class ImageViewerDialogFragment : BaseDialogFragment() {

    private val userCallback by lazy { requireViewerCallback() }
    private val initKey by lazy { requireInitKey() }
    private val transformer by lazy { requireTransformer() }
    private val adapter by lazy { ImageViewerAdapter(initKey) }
    private var initPosition = RecyclerView.NO_POSITION

    private lateinit var pager: ViewPager2
    private lateinit var overlayView: ConstraintLayout
    private lateinit var background: BackgroundView

    private val viewModel: PickerViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = { ViewModelFactory(requireActivity().application) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Components.working) dismissAllowingStateLoss()
    }

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

        requireOverlayCustomizer().provideView(overlayView)?.let(overlayView::addView)

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
                TransitionStartHelper.start(this@ImageViewerDialogFragment, transformer.getView(initKey), viewHolder)
                background.changeToBackgroundColor(Config.viewBackgroundColor)
                userCallback.onInit(viewHolder)

                if (initPosition > 0) userCallback.onPageSelected(initPosition, viewHolder)
            }

            override fun onDrag(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {
                background.updateBackgroundColor(fraction, Config.viewBackgroundColor, Color.TRANSPARENT)
                userCallback.onDrag(viewHolder, view, fraction)
            }

            override fun onRestore(viewHolder: FullscreenViewHolder, view: View, fraction: Float) {
                background.changeToBackgroundColor(Config.viewBackgroundColor)
                userCallback.onRestore(viewHolder, view, fraction)
            }

            override fun onRelease(viewHolder: FullscreenViewHolder, view: View) {
                val id = viewHolder.data?.id ?: return
                val startView = transformer.getView(id)
                TransitionEndHelper.end(this@ImageViewerDialogFragment, startView, viewHolder)
                background.changeToBackgroundColor(Color.TRANSPARENT)
                userCallback.onRelease(viewHolder, view)
            }
        }
    }

    private val pagerCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                userCallback.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                userCallback.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                pager.findViewHolderByAdapterPosition<FullscreenViewHolder>(position)?.let { holder ->
                    userCallback.onPageSelected(position, holder)
                }
            }
        }
    }

    override fun showFailure(message: String?) {
        super.showFailure(message)
        Components.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.listener = null
        pager.unregisterOnPageChangeCallback(pagerCallback)
        pager.adapter = null
        Components.release()
    }

    override fun onBackPressed() {
        if (TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating)
            return

        pager.findViewHolderByAdapterPosition<FullscreenViewHolder>(pager.currentItem)?.let { holder ->
            val startView = transformer.getView(holder.data?.id ?: return@let)
            background.changeToBackgroundColor(Color.TRANSPARENT)

            val endView = holder.endView
            TransitionEndHelper.end(this, startView, holder)
            userCallback.onRelease(holder, holder.itemView)

            TransitionEndHelper.end(this, startView, holder)
            userCallback.onRelease(holder, endView)
        }
    }

    open class Factory {
        open fun build(): ImageViewerDialogFragment = ImageViewerDialogFragment()
    }
}
