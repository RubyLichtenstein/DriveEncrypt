package com.ruby.driveencrypt.gallery

import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseGalleryAdapter :
    ListAdapter<GalleryItem, RecyclerView.ViewHolder>(GalleryItem.DiffCallback) {

    var onClick: ((View, GalleryItem) -> Unit)? = null
    var startPostponedEnterTransition: () -> Unit = {}

    inner class ImageViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = layoutPosition
                override fun getSelectionKey(): Long = itemId
            }
    }
}

