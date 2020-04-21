package com.ruby.driveencrypt.gallery.grid

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.utils.animateScale
import com.ruby.driveencrypt.utils.displayMetrics
import com.ruby.driveencrypt.utils.gone
import com.ruby.driveencrypt.utils.visible
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.gallery_grid_list_item.view.*
import java.io.File

val GRID_ITEMS = 3

class GalleryGridAdapter : BaseGalleryAdapter() {
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
                R.layout.gallery_grid_list_item,
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

        val uri = if (LocalFilesManager.isVideoFile(path)) {
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

        val icon = when (galleryItem.synced) {
            FilesManager.SyncStatus.Synced -> R.drawable.ic_cloud_done_black_24dp
            FilesManager.SyncStatus.Local -> R.drawable.ic_cloud_off_black_24dp
            FilesManager.SyncStatus.Remote -> null
            null -> null
        }

        with(holder.itemView.grid_item_sync_status) {
            if (icon != null) {
                visible()
                setImageResource(icon)
            } else {
                gone()
            }
        }

        tracker?.let {
            val selected = it.isSelected(getItemId(position))
            if (selected) {
                holder.itemView.grid_item_check.visibility = View.VISIBLE
                animateScale(galleryImage, 0.8F)
            } else {
                holder.itemView.grid_item_check.visibility = View.GONE
                animateScale(galleryImage, 1.0F)
            }
        }
    }

    override fun getItemId(position: Int): Long = data[position].key()

    fun getSelectedItems() = tracker
        ?.selection
        ?.mapNotNull { selected ->
            data.find { it.key() == selected }
        }
}
