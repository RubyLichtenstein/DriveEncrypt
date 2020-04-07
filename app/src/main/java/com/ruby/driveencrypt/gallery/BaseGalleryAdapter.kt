package com.ruby.driveencrypt.gallery

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseGalleryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClick: ((View, GalleryItem) -> Unit)? = null

    protected val data = mutableListOf<GalleryItem>()

    inner class ImageViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    inner class VideoViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount() = data.size

    fun add(path: String) {
        data.add(GalleryItem(path))
        notifyItemInserted(data.size)
    }

    fun addAll(path: List<String>) {
        data.addAll(path.map { GalleryItem(it) })
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }
}
