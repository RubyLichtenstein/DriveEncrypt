package com.ruby.driveencrypt.gallery.grid

import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.gallery.pager.isVideoFile
import com.ruby.driveencrypt.utils.displayMetrics
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.gallery_list_item.view.*
import java.io.File

val GRID_ITEMS = 3

class GalleryGridAdapter : BaseGalleryAdapter() {
    var mResizeOptions: ResizeOptions? = null
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.gallery_list_item,
                parent,
                false
            )

        rootView.gallery_image.layoutParams.height =
            rootView.displayMetrics().widthPixels / GRID_ITEMS
        return ImageViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val galleryItem = data[position]
        val path = galleryItem.path
        val context = holder.itemView.context

        val uri = if (isVideoFile(path)) {
            holder.itemView.grid_item_video_play.visibility = View.VISIBLE
            val file = File(path)
            Uri.fromFile(File(LocalFilesManager.tnPath(context, file)))
        } else {
            holder.itemView.grid_item_video_play.visibility = View.GONE
            Uri.fromFile(File(path))
        }

        Picasso
            .get()
            .load(uri)
            .apply {
                if (mResizeOptions != null) {
                    resize(mResizeOptions!!.width, mResizeOptions!!.height)
                    centerCrop()
                }
            }
            .into(holder.itemView.gallery_image)

//        holder.itemView.gallery_image.setImageRequest(imageRequest)
        holder.itemView.gallery_image.setOnClickListener {
            onClick?.invoke(it, galleryItem)
        }

        tracker?.let {
            val selected = it.isSelected(galleryItem.hashCode().toLong())
            holder.itemView.grid_item_check.visibility = if (selected) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun getItemId(position: Int): Long = data[position].hashCode().toLong()
}

class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as BaseGalleryAdapter.ImageViewHolder)
                .getItemDetails()
        }
        return null
    }

}