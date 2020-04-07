package com.ruby.driveencrypt.gallery.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.gallery.GalleryItem

abstract class BaseGalleryAdapter : RecyclerView.Adapter<BaseGalleryAdapter.MyViewHolder>() {

    var onClick: ((View, GalleryItem) -> Unit)? = null

    protected val data = mutableListOf<GalleryItem>()

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

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
