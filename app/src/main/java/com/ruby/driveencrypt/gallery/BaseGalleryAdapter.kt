package com.ruby.driveencrypt.gallery

import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.gallery.grid.GalleryGridDiffUtilCallback


abstract class BaseGalleryAdapter :
    ListAdapter<GalleryItem, RecyclerView.ViewHolder>(GalleryItem.DiffCallback) {

    var onClick: ((View, GalleryItem) -> Unit)? = null

//    val data = mutableListOf<GalleryItem>()

    inner class ImageViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = layoutPosition
                override fun getSelectionKey(): Long = itemId
            }
    }

//    override fun getItemCount() = data.size

//    fun addAll(newList: List<GalleryItem>) {
//        val diffResult = DiffUtil.calculateDiff(
//            GalleryGridDiffUtilCallback(
//                newItems = newList,
//                oldItems = this.data
//            )
//        )
//
//        this.data.clear()
//        this.data.addAll(newList)
//        diffResult.dispatchUpdatesTo(this)
//    }
}

