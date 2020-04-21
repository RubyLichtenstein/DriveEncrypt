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
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.gallery.GalleryItem
import com.ruby.driveencrypt.utils.getMimeType
import kotlinx.android.synthetic.main.pager_image_list_item.view.*
import java.io.File

class GalleryPagerAdapter : BaseGalleryAdapter() {

    companion object {
        const val VIEW_TYPE_IMAGE: Int = 0
    }

    var onTap: ((View, GalleryItem) -> Unit)? = null
    var onTapVideo: ((View, Uri) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_IMAGE
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val galleryItem = data[position]
        val path = galleryItem.path

        if (LocalFilesManager.isVideoFile(path)) {
            val uri = Uri.fromFile(File(path))
            holder.itemView.video_play_btn.visibility = View.VISIBLE
            holder.itemView.video_play_btn.setOnClickListener {
                onTapVideo?.invoke(it, uri)
            }
        } else {
            holder.itemView.video_play_btn.visibility = View.GONE
        }

        val imageUri = LocalFilesManager.getUriIfVideoThumbnail(holder.itemView.context, path)
        bindImage(holder, imageUri, galleryItem)
    }

    private fun bindImage(
        holder: RecyclerView.ViewHolder,
        uri: Uri,
        galleryItem: GalleryItem
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
        val rootView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.pager_image_list_item,
                parent,
                false
            )

        return ImageViewHolder(rootView)
    }
}
