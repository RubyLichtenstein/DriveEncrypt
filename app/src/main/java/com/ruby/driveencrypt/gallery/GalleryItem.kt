package com.ruby.driveencrypt.gallery

import com.ruby.driveencrypt.files.RemoteFilesManager

data class GalleryItem(
    val path: String,
    val synced: RemoteFilesManager.SyncStatus? = null
) {
    fun key() = path.hashCode().toLong()
}
