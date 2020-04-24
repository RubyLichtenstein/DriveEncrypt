package com.ruby.driveencrypt.gallery.grid

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import com.ruby.driveencrypt.utils.animateScale
import com.ruby.driveencrypt.utils.gone
import com.ruby.driveencrypt.utils.visible
import kotlinx.android.synthetic.main.gallery_grid_list_item.view.*
import java.io.File

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

        return ImageViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val galleryItem = getItem(position)
        val path = galleryItem.path

        with(holder.itemView.grid_item_video_play) {
            if (LocalFilesManager.isVideoFile(path)) {
                visible()
            } else {
                gone()
            }
        }

        val galleryImage = holder.itemView
            .gallery_image

        val file = File(path)
        val uri = Uri.fromFile(file)

        Glide.with(galleryImage)
            .load(uri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(galleryImage)

        galleryImage.setOnClickListener {
            onClick?.invoke(it, galleryItem)
        }

        val icon = when (galleryItem.synced) {
            RemoteFilesManager.SyncStatus.Synced -> R.drawable.ic_cloud_done_black_24dp
            RemoteFilesManager.SyncStatus.Local -> R.drawable.ic_cloud_off_black_24dp
            RemoteFilesManager.SyncStatus.Remote -> null
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

    override fun getItemId(position: Int): Long = getItem(position).key()

    fun getSelectedItems() = tracker
        ?.selection
        ?.mapNotNull { selected ->
            currentList.find { it.key() == selected }
        }
}
