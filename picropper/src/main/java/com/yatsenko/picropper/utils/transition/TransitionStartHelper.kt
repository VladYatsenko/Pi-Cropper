package com.yatsenko.picropper.utils.transition

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.transition.*
import com.yatsenko.picropper.R
import com.yatsenko.picropper.ui.viewer.utils.Config

object TransitionStartHelper {

    val transitionAnimating: Boolean
        get() = animating

    private var animating = false

    private val transitionSet: Transition
        get() = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(ChangeImageTransform())
            // https://github.com/davemorrissey/subsampling-scale-image-view/issues/313
            duration = Config.durationTransition
            interpolator = DecelerateInterpolator()
        }

    fun start(owner: LifecycleOwner, startView: View?, transitionStart: TransitionStart) {
        transitionStart.beforeTransitionStart(startView)
        val doTransition = {
            TransitionManager.beginDelayedTransition(transitionStart.viewGroup, transitionSet.also {
                it.addListener(object : TransitionListenerAdapter() {
                    override fun onTransitionStart(transition: Transition) {
                        animating = true
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        if (!animating) return
                        animating = false
                        transitionStart.afterTransitionStart()
                    }
                })
            })
            transitionStart.transitionStart()
        }
        transitionStart.viewGroup.postDelayed(doTransition, 50)

        owner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    owner.lifecycle.removeObserver(this)
                    animating = false
                    transitionStart.viewGroup.removeCallbacks(doTransition)
                    TransitionManager.endTransitions(transitionStart.viewGroup)
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