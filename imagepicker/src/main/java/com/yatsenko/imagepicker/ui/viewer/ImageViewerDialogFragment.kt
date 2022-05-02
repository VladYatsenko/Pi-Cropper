package com.yatsenko.imagepicker.ui.viewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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

open class ImageViewerDialogFragment : BaseDialogFragment() {

    private val userCallback by lazy { requireViewerCallback() }
    private val initKey by lazy { requireInitKey() }
    private val transformer by lazy { requireTransformer() }
    private val adapter by lazy { ImageViewerAdapter(initKey) }
    private var initPosition = RecyclerView.NO_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Components.working) dismissAllowingStateLoss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setListener(adapterListener)
        (viewer.getChildAt(0) as? RecyclerView?)?.let {
            it.clipChildren = false
            it.itemAnimator = null
        }
        viewer.orientation = Config.VIEWER_ORIENTATION
        viewer.registerOnPageChangeCallback(pagerCallback)
        viewer.offscreenPageLimit = offscreenPageLimit
        viewer.adapter = adapter

        requireOverlayCustomizer().provideView(overlayView)?.let(overlayView::addView)

        viewModel.dataList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            initPosition = list.indexOfFirst { it.id == initKey }
            viewer.setCurrentItem(initPosition, false)
        }

        viewModel.viewerUserInputEnabled.observe(viewLifecycleOwner) {
            viewer.isUserInputEnabled = it ?: true
        }

    }


    private val adapterListener by lazy {
        object : ImageViewerAdapterListener {
            override fun onInit(viewHolder: RecyclerView.ViewHolder) {
                TransitionStartHelper.start(this@ImageViewerDialogFragment, transformer.getView(initKey), viewHolder)
                background.changeToBackgroundColor(Config.viewBackgroundColor)
                userCallback.onInit(viewHolder)

                if (initPosition > 0) userCallback.onPageSelected(initPosition, viewHolder)
            }

            override fun onDrag(viewHolder: RecyclerView.ViewHolder, view: View, fraction: Float) {
                background.updateBackgroundColor(fraction, Config.viewBackgroundColor, Color.TRANSPARENT)
                userCallback.onDrag(viewHolder, view, fraction)
            }

            override fun onRestore(viewHolder: RecyclerView.ViewHolder, view: View, fraction: Float) {
                background.changeToBackgroundColor(Config.viewBackgroundColor)
                userCallback.onRestore(viewHolder, view, fraction)
            }

            override fun onRelease(viewHolder: RecyclerView.ViewHolder, view: View) {
                val startView = (view.getTag(R.id.viewer_adapter_item_key) as? Long?)?.let { transformer.getView(it) }
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
                val currentKey = adapter.getItemId(position)
                val holder = viewer.findViewWithKeyTag(R.id.viewer_adapter_item_key, currentKey)
                        ?.getTag(R.id.viewer_adapter_item_holder) as? RecyclerView.ViewHolder?
                        ?: return
                userCallback.onPageSelected(position, holder)
            }
        }
    }

    override fun showFailure(message: String?) {
        super.showFailure(message)
        Components.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.setListener(null)
        viewer.unregisterOnPageChangeCallback(pagerCallback)
        viewer.adapter = null
        innerBinding = null
        Components.release()
    }

    override fun onBackPressed() {
        if (TransitionStartHelper.transitionAnimating || TransitionEndHelper.transitionAnimating) return

        val currentKey = adapter.getItemId(viewer.currentItem)
        viewer.findViewWithKeyTag(R.id.viewer_adapter_item_key, currentKey)?.let { endView ->
            val startView = transformer.getView(currentKey)
            background.changeToBackgroundColor(Color.TRANSPARENT)

            (endView.getTag(R.id.viewer_adapter_item_holder) as? RecyclerView.ViewHolder?)?.let {
                TransitionEndHelper.end(this, startView, it)
                userCallback.onRelease(it, endView)
            }
        }
    }

    open class Factory {
        open fun build(): ImageViewerDialogFragment = ImageViewerDialogFragment()
    }
}
