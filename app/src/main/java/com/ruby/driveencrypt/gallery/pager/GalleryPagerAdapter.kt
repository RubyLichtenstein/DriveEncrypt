package com.ruby.driveencrypt.gallery.pager

import android.graphics.drawable.Animatable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.common.media.MediaUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.gallery.GalleryItem
import com.ruby.driveencrypt.utils.getMimeType
import kotlinx.android.synthetic.main.pager_image_list_item.view.*
import java.io.File

fun isVideoFile(path: String): Boolean {
    return MediaUtils.isVideo(MediaUtils.extractMime(path))
}

class GalleryPagerAdapter : BaseGalleryAdapter() {

    companion object {
        const val VIEW_TYPE_IMAGE: Int = 0
        const val VIEW_TYPE_VIDEO: Int = 1
    }

    var onTap: ((View, GalleryItem) -> Unit)? = null
    var onTapVideo: ((View, Uri) -> Unit)? = null

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
        val path = galleryItem.path

        if (isVideoFile(path)) {
            val uri = Uri.fromFile(File(path))
            holder.itemView.video_play_btn.visibility = View.VISIBLE
            holder.itemView.video_play_btn.setOnClickListener {
                onTapVideo?.invoke(it, uri)
            }
        } else {
            holder.itemView.video_play_btn.visibility = View.GONE
        }

        val imageUri = getUri(holder.itemView.context, path)

        when (holder) {
            is ImageViewHolder -> {
                bindImage(holder, imageUri, galleryItem, false)
            }
            is VideoViewHolder -> {
                bindImage(holder, imageUri, galleryItem, true)
            }
        }
    }

    private fun bindImage(
        holder: RecyclerView.ViewHolder,
        uri: Uri,
        galleryItem: GalleryItem,
        isVideo: Boolean
    ) {

        val mPhotoDraweeView = holder.itemView.page_image
        val controller = Fresco.newDraweeControllerBuilder()
        controller.setUri(uri)
        controller.oldController = mPhotoDraweeView.controller
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
                mPhotoDraweeView.update(imageInfo.width, imageInfo.getHeight())
            }
        }
        mPhotoDraweeView.setController(controller.build())

        mPhotoDraweeView.setOnPhotoTapListener { view, x, y ->
            onTap?.invoke(view, galleryItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutRes = when (viewType) {
            VIEW_TYPE_IMAGE -> R.layout.pager_image_list_item
            VIEW_TYPE_VIDEO -> R.layout.pager_image_list_item
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
