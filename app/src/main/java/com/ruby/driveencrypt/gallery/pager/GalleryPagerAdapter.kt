package com.ruby.driveencrypt.gallery.pager

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.gallery.GalleryItem
import com.ruby.driveencrypt.utils.MediaUtils
import kotlinx.android.synthetic.main.pager_image_list_item.view.*
import java.io.File

class GalleryPagerAdapter : BaseGalleryAdapter() {

    var onTap: ((View, GalleryItem) -> Unit)? = null
    var onTapVideo: ((View, Uri) -> Unit)? = null

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val galleryItem = getItem(position)
        val path = galleryItem.path
        val uri = Uri.fromFile(File(path))

        if (MediaUtils.isVideoFile(path)) {
            holder.itemView.video_play_btn.visibility = View.VISIBLE
            holder.itemView.video_play_btn.setOnClickListener {
                onTapVideo?.invoke(it, uri)
            }
        } else {
            holder.itemView.video_play_btn.visibility = View.GONE
        }

        bindImage(holder, uri, galleryItem)
    }

    private fun bindImage(
        holder: RecyclerView.ViewHolder,
        uri: Uri,
        galleryItem: GalleryItem
    ) {
        val imageView = holder.itemView.page_image

        Glide.with(imageView)
            .load(uri)
            .thumbnail(0.33f)
            .into(imageView)

        imageView.setOnClickListener { view ->
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
