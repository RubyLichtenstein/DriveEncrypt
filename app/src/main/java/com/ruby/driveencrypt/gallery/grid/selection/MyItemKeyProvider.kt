package com.ruby.driveencrypt.gallery.grid.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter

class MyItemKeyProvider(private val rvAdapter: BaseGalleryAdapter) :
    ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long = rvAdapter.data[position].key()
    override fun getPosition(key: Long): Int = rvAdapter.data.indexOfFirst {
        key == it.key()
    }
}