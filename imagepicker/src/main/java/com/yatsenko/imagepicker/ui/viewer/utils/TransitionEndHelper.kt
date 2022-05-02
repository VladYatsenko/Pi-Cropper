package com.yatsenko.imagepicker.ui.viewer.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.yatsenko.imagepicker.ui.viewer.viewholders.PhotoViewHolder
import com.yatsenko.imagepicker.ui.viewer.viewholders.SubsamplingViewHolder
import com.yatsenko.imagepicker.ui.viewer.viewholders.VideoViewHolder
import kotlin.math.max

object TransitionEndHelper {
    val transitionAnimating get() = animating
    private var animating = false

    fun end(fragment: DialogFragment, startView: View?, holder: RecyclerView.ViewHolder) {
        beforeTransition(startView, holder)
        val doTransition = {
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup, transitionSet().also {
                it.addListener(object : TransitionListenerAdapter() {
                    override fun onTransitionStart(transition: Transition) {
                        animating = true
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        if (!animating) return
                        animating = false
                        fragment.dismissAllowingStateLoss()
                    }
                })
            })
            transition(startView, holder)
        }
        holder.itemView.post(doTransition)

        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    fragment.lifecycle.removeObserver(this)
                    animating = false
                    holder.itemView.removeCallbacks(doTransition)
                    TransitionManager.endTransitions(holder.itemView as ViewGroup)
                }
            }
        })
    }

    private fun beforeTransition(startView: View?, holder: RecyclerView.ViewHolder) {
        when (holder) {
            is VideoViewHolder -> {
                holder.imageView.translationX = holder.videoView.translationX
                holder.imageView.translationY = holder.videoView.translationY
                holder.imageView.scaleX = holder.videoView.scaleX
                holder.imageView.scaleY = holder.videoView.scaleY
                holder.imageView.visibility = View.VISIBLE
                holder.videoView.visibility = View.GONE
            }
        }
    }

    private fun transition(startView: View?, holder: RecyclerView.ViewHolder) {
        when (holder) {
            is PhotoViewHolder -> {
                holder.photoView.scaleType = (startView as? ImageView?)?.scaleType
                        ?: ImageView.ScaleType.FIT_CENTER
                holder.photoView.translationX = 0f
                holder.photoView.translationY = 0f
                holder.photoView.scaleX = if (startView != null) 1f else 2f
                holder.photoView.scaleY = if (startView != null) 1f else 2f
                // holder.photoView.alpha = startView?.alpha ?: 0f
                fade(holder, startView)
                holder.photoView.layoutParams = holder.photoView.layoutParams.apply {
                    width = startView?.width ?: width
                    height = startView?.height ?: height
                    val location = IntArray(2)
                    getLocationOnScreen(startView, location)
                    if (this is ViewGroup.MarginLayoutParams) {
                        marginStart = location[0]
                        topMargin = location[1] - Config.transitionOffsetY
                    }
                }
            }
            is SubsamplingViewHolder -> {
                holder.subsamplingView.translationX = 0f
                holder.subsamplingView.translationY = 0f
                holder.subsamplingView.scaleX = 2f
                holder.subsamplingView.scaleY = 2f
                // holder.photoView.alpha = startView?.alpha ?: 0f
                fade(holder) // https://github.com/davemorrissey/subsampling-scale-image-view/issues/313
                holder.subsamplingView.layoutParams = holder.subsamplingView.layoutParams.apply {
                    width = startView?.width ?: width
                    height = startView?.height ?: height
                    val location = IntArray(2)
                    getLocationOnScreen(startView, location)
                    if (this is ViewGroup.MarginLayoutParams) {
                        marginStart = location[0]
                        topMargin = location[1] - Config.transitionOffsetY
                    }
                }
            }
            is VideoViewHolder -> {
                holder.imageView.translationX = 0f
                holder.imageView.translationY = 0f
                holder.imageView.scaleX = if (startView != null) 1f else 2f
                holder.imageView.scaleY = if (startView != null) 1f else 2f
                // holder.photoView.alpha = startView?.alpha ?: 0f
                fade(holder, startView)
                holder.videoView.pause()
                holder.imageView.layoutParams = holder.imageView.layoutParams.apply {
                    width = startView?.width ?: width
                    height = startView?.height ?: height
                    val location = IntArray(2)
                    getLocationOnScreen(startView, location)
                    if (this is ViewGroup.MarginLayoutParams) {
                        marginStart = location[0]
                        topMargin = location[1] - Config.transitionOffsetY
                    }
                }
            }
        }
    }

    private fun transitionSet(): Transition {
        return TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(ChangeImageTransform())
            addTransition(ChangeTransform())
            // addTransition(Fade())
            duration = Config.durationTransition
            interpolator = DecelerateInterpolator()
        }
    }

    private fun fade(holder: RecyclerView.ViewHolder, startView: View? = null) {
        when (holder) {
            is PhotoViewHolder -> {
                if (startView != null) {
                    holder.photoView.animate()
                            .setDuration(0)
                            .setStartDelay(max(Config.durationTransition - 20, 0))
                            .alpha(0f).start()
                } else {
                    holder.photoView.animate().setDuration(Config.durationTransition)
                            .alpha(0f).start()
                }
            }
            is SubsamplingViewHolder -> {
                holder.subsamplingView.animate().setDuration(Config.durationTransition)
                        .alpha(0f).start()
            }
            is VideoViewHolder -> {
                if (startView != null) {
                    holder.imageView.animate()
                            .setDuration(0)
                            .setStartDelay(max(Config.durationTransition - 20, 0))
                            .alpha(0f).start()
                } else {
                    holder.imageView.animate().setDuration(Config.durationTransition)
                            .alpha(0f).start()
                }
            }
        }
    }

    private fun getLocationOnScreen(startView: View?, location: IntArray) {
        startView?.getLocationOnScreen(location)

        if (location[0] == 0) {
            location[0] = (startView?.getTag(R.id.viewer_start_view_location_0) as? Int) ?: 0
        }
        if (location[1] == 0) {
            location[1] = (startView?.getTag(R.id.viewer_start_view_location_1) as? Int) ?: 0
        }

        if (startView?.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            location[0] = startView.context.resources.displayMetrics.widthPixels - location[0] - startView.width
        }
    }
}