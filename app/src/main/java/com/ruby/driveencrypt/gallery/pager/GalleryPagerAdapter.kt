package com.ruby.driveencrypt.gallery.pager

import android.graphics.drawable.Animatable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.gallery.GalleryItem
import com.ruby.driveencrypt.utils.getMimeType
import kotlinx.android.synthetic.main.pager_image_list_item.view.*
import kotlinx.android.synthetic.main.pager_video_list_item.view.*
import java.io.File

fun isVideoFile(path: String): Boolean {
    val mimeType = getMimeType(path) ?: throw Exception("mime type in null, path: $path")
    return mimeType.startsWith("video")
}

class GalleryPagerAdapter : BaseGalleryAdapter() {

    companion object {
        const val VIEW_TYPE_IMAGE: Int = 0
        const val VIEW_TYPE_VIDEO: Int = 1
    }

    var onTap: ((View, GalleryItem) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        val path = data[position].path
        val mimeType = getMimeType(path) ?: throw Exception("mime type in null, path: $path")

        return when {
            mimeType.startsWith("image") -> VIEW_TYPE_IMAGE
            isVideoFile(path) -> VIEW_TYPE_VIDEO
            else -> throw Exception("mime type not supported, path: $path")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val galleryItem = data[position]
        val uri = Uri.fromFile(File(galleryItem.path))

//        holder.itemView.setOnClickListener {
//            onTap?.invoke(it, galleryItem)
//        }
        when (holder) {
            is ImageViewHolder -> {

                val mPhotoDraweeView = holder.itemView.page_image

                val controller = Fresco.newDraweeControllerBuilder()
                controller.setUri(uri)
                controller.oldController = mPhotoDraweeView.getController()
                controller.controllerListener = object : BaseControllerListener<ImageInfo?>() {
                    override fun onFinalImageSet(
                        id: String,
                        imageInfo: ImageInfo?,
                        animatable: Animatable?
                    ) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        if (imageInfo == null || mPhotoDraweeView == null) {
                            return
                        }
                        mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight())
                    }
                }
                mPhotoDraweeView.setController(controller.build())

//                mPhotoDraweeView.window().decorView.apply {
//                    systemUiVisibility =
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//                }

                mPhotoDraweeView.setOnPhotoTapListener { view, x, y ->
                    onTap?.invoke(view, galleryItem)
                }
            }
            is VideoViewHolder -> {
                bindVideo(holder, uri, galleryItem)
            }
        }
    }

    var player: SimpleExoPlayer? = null
    private fun bindVideo(
        holder: RecyclerView.ViewHolder,
        uri: Uri,
        galleryItem: GalleryItem
    ) {
        val playerView = holder.itemView.gallery_player_view
        val context = holder.itemView.context
        player = SimpleExoPlayer.Builder(context).build()
        playerView.setPlayer(player)
        playerView.setOnClickListener {
            onTap?.invoke(it, galleryItem)
        }

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "yourApplicationName")
        )

        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
        player?.prepare(videoSource)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
//        player?.stop()
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
//        player?.stop()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutRes = when (viewType) {
            VIEW_TYPE_IMAGE -> R.layout.pager_image_list_item
            VIEW_TYPE_VIDEO -> R.layout.pager_video_list_item
            else -> throw Exception("viewType not supported viewType: $viewType")
        }

        val rootView = LayoutInflater.from(parent.context)
            .inflate(
                layoutRes,
                parent,
                false
            )

        return when (viewType) {
            VIEW_TYPE_IMAGE -> ImageViewHolder(rootView)
            VIEW_TYPE_VIDEO -> VideoViewHolder(rootView)
            else -> throw Exception("viewType not supported viewType: $viewType")
        }
    }
}
