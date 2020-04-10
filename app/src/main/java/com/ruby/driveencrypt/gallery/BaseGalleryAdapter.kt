package com.ruby.driveencrypt.gallery

import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.gallery.grid.GalleryGridDiffUtilCallback


abstract class BaseGalleryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClick: ((View, GalleryItem) -> Unit)? = null

    val data = mutableListOf<GalleryItem>()

    inner class ImageViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = layoutPosition
                override fun getSelectionKey(): Long = itemId
            }
    }

    inner class VideoViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun getItemCount() = data.size

    fun add(path: String) {
        data.add(GalleryItem(path))
        notifyItemInserted(data.size)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun addAll(newList: List<String>) {
        val newItems = newList.map { GalleryItem(it) }

        val diffResult = DiffUtil.calculateDiff(
            GalleryGridDiffUtilCallback(
                newItems = newItems,
                oldItems = this.data
            )
        )
        this.data.clear()
        this.data.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }
}

