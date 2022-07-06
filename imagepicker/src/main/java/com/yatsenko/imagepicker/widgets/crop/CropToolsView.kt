package com.yatsenko.imagepicker.widgets.crop

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.core.Theme
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.utils.extensions.Animations.slideDown
import com.yatsenko.imagepicker.utils.extensions.Animations.slideUp
import com.yatsenko.imagepicker.utils.extensions.actionBarSize
import com.yatsenko.imagepicker.utils.extensions.invisible
import com.yatsenko.imagepicker.utils.extensions.navigationBarSize
import com.yatsenko.imagepicker.utils.extensions.visible
import java.util.*

class CropToolsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42

    private val accentColor = ContextCompat.getColor(context, Theme.themedColor(Theme.accentColor))
    private val accentColorList = ColorStateList.valueOf(accentColor)
    private val toolsColor = ContextCompat.getColor(context, Theme.themedColor(Theme.toolsColor))

    private val recycler: RecyclerView

    private val apply: AppCompatImageView
    private val cancel: AppCompatImageView
    private val resetRotation: AppCompatImageView
    private val rotateByAngle: AppCompatImageView
    private val wheel: HorizontalProgressWheelView
    private val progress: ProgressBar

    private val rotate: TextView

    private val adapter = AspectRatioAdapter()

    internal var data: Data? = null
        set(value) {
            field = value
            refreshLayout()
        }

    var rotateAngel: Float = 0f
        set(value) {
            field = value
            rotate.text = String.format(Locale.getDefault(), "%.1f°", value)
        }

    internal var result: (AdapterResult) -> Unit = {}
    private val internalResult: (AdapterResult) -> Unit = { result(it) }

    init {
        inflate(context, R.layout.layout_crop_tools, this)

        findViewById<View>(R.id.root).apply {
            updatePadding(bottom = context.navigationBarSize)
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, Theme.themedColor(Theme.toolsBackgroundColor)))
        }

        recycler = findViewById(R.id.aspect_recycler)

        recycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = this@CropToolsView.adapter
            itemAnimator = null
        }
        adapter.result = internalResult

        apply = findViewById(R.id.apply)
        apply.imageTintList = ColorStateList.valueOf((ContextCompat.getColor(context, Theme.themedColor(Theme.accentColor))))

        apply.setOnClickListener {
            result(AdapterResult.OnApplyCrop)
        }
        cancel = findViewById(R.id.cancel)
        cancel.setOnClickListener {
            result(AdapterResult.OnCancelCrop)
        }
        cancel.imageTintList = ColorStateList.valueOf(toolsColor)

        wheel = findViewById(R.id.rotate_scroll_wheel)
        wheel.scrollingListener = object : HorizontalProgressWheelView.ScrollingListener {
            override fun onScrollStart() {
                internalResult(AdapterResult.OnRotateStart)
            }

            override fun onScroll(delta: Float, totalDistance: Float) {
                internalResult(AdapterResult.OnRotateProgress(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT))
            }

            override fun onScrollEnd() {
                internalResult(AdapterResult.OnRotateEnd)
            }
        }

        progress = findViewById(R.id.progress)
        progress.isIndeterminate = true
        progress.indeterminateTintList = accentColorList

        rotate = findViewById(R.id.text_view_rotate)
        rotate.setTextColor(accentColor)

        resetRotation = findViewById(R.id.reset_rotate)
        resetRotation.setOnClickListener { internalResult(AdapterResult.OnResetRotationClicked) }
        resetRotation.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, Theme.themedColor(Theme.toolsResetRotationColor)))

        rotateByAngle = findViewById(R.id.rotate_by_angle)
        rotateByAngle.setOnClickListener { internalResult(AdapterResult.OnRotate90Clicked) }
        rotateByAngle.imageTintList = ColorStateList.valueOf(toolsColor)

        this.slideDown(0f, this.height.toFloat(), 0)
        this.alpha = 0f
    }

    private fun refreshLayout() {
        data?.let {
            adapter.submitList(it.aspectRatioList)
            recycler.isGone = it.aspectRatioList.size < 2
        }
    }

    fun showLoading() {
        progress.visible()
        apply.invisible()
    }

    fun show() {
        this.animate()?.setDuration(200)?.alpha(1f)?.start()
        this.slideUp(this.height.toFloat(), 0f)
    }

    fun hide() {
        this.animate()?.setDuration(200)?.alpha(0f)?.start()
        this.slideDown(0f, this.height.toFloat())
    }

    internal data class Data(
        val aspectRatioList: List<AspectRatioWrapper>
    )

}