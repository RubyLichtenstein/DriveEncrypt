package com.ruby.driveencrypt.gallery.grid

import android.animation.ValueAnimator
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.ruby.driveencrypt.utils.setMargins
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
            val file = File(path)
            Uri.fromFile(File(LocalFilesManager.tnPath(context, file)))
        }

        val size = context.displayMetrics().widthPixels / GRID_ITEMS

        val galleryImage = holder.itemView
            .gallery_image

        Picasso
            .get()
            .load(uri)
            .resize(size, size)
            .centerCrop()
            .into(galleryImage)

        galleryImage.setOnClickListener {
            onClick?.invoke(it, galleryItem)
        }

        tracker?.let {
            val selected = it.isSelected(galleryItem.hashCode().toLong())
            if (selected) {
                holder.itemView.grid_item_check.visibility = View.VISIBLE
                animateScale(galleryImage, 0.8F)
            } else {
                holder.itemView.grid_item_check.visibility = View.GONE
                animateScale(galleryImage, 1.0F)
            }
        }
    }

    private fun animateScale(galleryImage: ImageView, scale: Float) {
        galleryImage
            .animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(200)
            .withEndAction {
                galleryImage.scaleX = scale
                galleryImage.scaleY = scale
            }
            .start()
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