package com.ruby.driveencrypt.gallery

import com.ruby.driveencrypt.files.FilesManager

data class GalleryItem(
    val path: String,
    val synced: FilesManager.SyncStatus? = null
) {
    fun key() = path.hashCode().toLong()
}
