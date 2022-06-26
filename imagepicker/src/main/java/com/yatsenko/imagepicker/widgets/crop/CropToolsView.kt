package com.yatsenko.imagepicker.widgets.crop

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatsenko.imagepicker.R
import com.yatsenko.imagepicker.model.AdapterResult
import com.yatsenko.imagepicker.model.AspectRatio
import com.yatsenko.imagepicker.utils.extensions.invisible
import com.yatsenko.imagepicker.utils.extensions.navigationBarSize
import com.yatsenko.imagepicker.utils.extensions.visible
import java.util.*

class CropToolsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42

    private val recycler: RecyclerView

    private val apply: AppCompatImageView
    private val cancel: AppCompatImageView
    private val resetRotation: AppCompatImageView
    private val rotateByAngle: AppCompatImageView
    private val wheel: HorizontalProgressWheelView
    private val progress: ProgressBar

    private val rotate: TextView

    private val adapter = AspectRatioAdapter()

    var data: Data? = null
        set(value) {
            field = value
            refreshLayout()
        }

    var rotateAngel: Float = 0f
        set(value) {
            field = value
            rotate.text = String.format(Locale.getDefault(), "%.1f°", value)
        }

    var result: (AdapterResult) -> Unit = {}
    private val internalResult: (AdapterResult) -> Unit = { result(it) }

    init {
        inflate(context, R.layout.layout_crop_tools, this)

        findViewById<View>(R.id.root).updatePadding(bottom = context.navigationBarSize)

        recycler = findViewById(R.id.aspect_recycler)

        recycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = this@CropToolsView.adapter
            itemAnimator = null
        }
        adapter.result = internalResult

        apply = findViewById(R.id.apply)
        apply.setOnClickListener {
            result(AdapterResult.OnApplyCrop)
        }
        cancel = findViewById(R.id.cancel)
        cancel.setOnClickListener {
            result(AdapterResult.OnCancelCrop)
        }
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
        rotate = findViewById(R.id.text_view_rotate)

        resetRotation = findViewById(R.id.reset_rotate)
        resetRotation.setOnClickListener { internalResult(AdapterResult.OnResetRotationClicked) }

        rotateByAngle = findViewById(R.id.rotate_by_angle)
        rotateByAngle.setOnClickListener { internalResult(AdapterResult.OnRotate90Clicked) }
    }

    private fun refreshLayout() {
        data?.let {
            adapter.submitList(it.aspectRatioList)
        }
    }

    fun showLoading() {
        progress.visible()
        apply.invisible()
    }

    data class Data(
        val aspectRatioList: List<AspectRatioAdapter.Data>
    )

}