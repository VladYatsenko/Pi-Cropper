package com.yatsenko.imagepicker.ui.viewer.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import androidx.transition.*
import com.yatsenko.imagepicker.ui.viewer.viewholders.FullscreenViewHolder
import com.yatsenko.imagepicker.R

object TransitionEndHelper {

    val transitionAnimating: Boolean
        get() = animating

    private var animating = false

    private val transitionSet: Transition
        get() = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(ChangeImageTransform())
            addTransition(ChangeTransform())
            // addTransition(Fade())
            duration = Config.durationTransition
            interpolator = DecelerateInterpolator()
        }

    fun end(fragment: DialogFragment, startView: View?, holder: FullscreenViewHolder) {
        holder.beforeTransitionEnd(startView)
        val doTransition = {
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup, transitionSet.also {
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
            holder.transitionEnd(startView)
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

    fun getLocationOnScreen(startView: View?, location: IntArray) {
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