package com.ruby.driveencrypt.gallery.grid

import androidx.recyclerview.widget.DiffUtil
import com.ruby.driveencrypt.gallery.GalleryItem


class GalleryGridDiffUtilCallback(
    private val newItems: List<GalleryItem>,
    private val oldItems: List<GalleryItem>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = oldItems[oldItemPosition].path == newItems[newItemPosition].path

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = oldItems[oldItemPosition] == newItems[newItemPosition]
}