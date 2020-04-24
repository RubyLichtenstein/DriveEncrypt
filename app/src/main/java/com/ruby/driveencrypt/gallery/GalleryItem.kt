package com.ruby.driveencrypt.gallery

import androidx.recyclerview.widget.DiffUtil
import com.ruby.driveencrypt.files.RemoteFilesManager

data class GalleryItem(
    val path: String,
    val synced: RemoteFilesManager.SyncStatus? = null
) {
    fun key() = path.hashCode().toLong()

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<GalleryItem>() {
            override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem) =
                oldItem.path == newItem.path

            override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem) =
                oldItem == newItem
        }
    }
}
