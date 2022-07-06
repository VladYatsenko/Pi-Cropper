package com.yatsenko.imagepicker.utils.transition

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import androidx.transition.*
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.ui.viewer.utils.Config

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

    fun end(fragment: DialogFragment, startView: View?, transitionEnd: TransitionEnd, onTransitionEnd: () -> Unit) {
        transitionEnd.beforeTransitionEnd(startView)
        val doTransition = {
            TransitionManager.beginDelayedTransition(transitionEnd.viewGroup, transitionSet.also {
                it.addListener(object : TransitionListenerAdapter() {
                    override fun onTransitionStart(transition: Transition) {
                        animating = true
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        if (!animating) return
                        animating = false
                        fragment.dismissAllowingStateLoss()

                        onTransitionEnd()
                    }
                })
            })
            transitionEnd.transitionEnd(startView)
        }
        transitionEnd.viewGroup.post(doTransition)

        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    fragment.lifecycle.removeObserver(this)
                    animating = false
                    transitionEnd.viewGroup.removeCallbacks(doTransition)
                    TransitionManager.endTransitions(transitionEnd.viewGroup)
                }
            }
        })
    }

    fun getLocationOnScreen(startView: View?, location: IntArray) {
        startView?.getLocationOnScreen(location)
        if (startView?.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            location[0] = startView.context.resources.displayMetrics.widthPixels - location[0] - startView.width
        }
    }
}