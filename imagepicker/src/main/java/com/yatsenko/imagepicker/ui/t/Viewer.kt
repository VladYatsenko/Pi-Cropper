package com.yatsenko.imagepicker.ui.t

import android.content.Context
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.github.iielse.imageviewer.core.ImageLoader
import com.github.iielse.imageviewer.core.Photo
import com.github.iielse.imageviewer.core.SimpleDataProvider
import com.github.iielse.imageviewer.core.Transformer
import com.github.iielse.imageviewer.utils.Config
import com.yatsenko.imagepicker.model.Image
import com.yatsenko.imagepicker.utils.extensions.loadImage

class Viewer (private val context: Context) {

    init {
        Config.TRANSITION_OFFSET_Y = context.statusBarHeight()
    }

    fun show(photo: Photo, list: List<Photo>) {
        val builder = ImageViewerBuilder(
            context = context,
            initKey = photo.id(), // photoId
            dataProvider = SimpleDataProvider(list), // 一次性全量加载 // 实现DataProvider接口支持分页加载
            imageLoader = loader, // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
            transformer = transformer, // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
        )
        builder.show()
    }

    private val loader = object: ImageLoader {

        override fun load(view: ImageView, data: Photo, viewHolder: RecyclerView.ViewHolder) {
            view.loadImage((data as? Image)?.imagePath)
        }

        override fun load(
            subsamplingView: SubsamplingScaleImageView,
            data: Photo,
            viewHolder: RecyclerView.ViewHolder
        ) {
            subsamplingView.setImage(ImageSource.uri((data as? Image)?.imagePath ?: ""))
        }

    }

    private val transformer = object : Transformer {
        override fun getView(key: Long): ImageView? = ViewerTransitionHelper.provide(key)
    }
}

object ViewerTransitionHelper {
    private val transition = HashMap<ImageView, Long>()
    fun put(photoId: Long, imageView: ImageView) { // 将photoId和展示这个数据的ImageView'绑定'
        require(isMainThread())
        if (!imageView.isAttachedToWindow) return
        imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View?) = Unit
            override fun onViewDetachedFromWindow(p0: View?) {
                transition.remove(imageView)
                imageView.removeOnAttachStateChangeListener(this)
            }
        })
        transition[imageView] = photoId
    }

    fun provide(photoId: Long): ImageView? {
        transition.keys.forEach {
            if (transition[it] == photoId)
                return it
        }
        return null
    }
}

fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun Context.statusBarHeight(): Int {
    var height = 0
    val resourceId = this.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        height = this.resources.getDimensionPixelSize(resourceId)
    }
    return height
}
