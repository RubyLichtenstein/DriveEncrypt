package com.example.driveencrypt.gallery

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driveencrypt.R

data class GalleryItem(val path: String, val src: String)

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.MyViewHolder>() {

    val data = mutableListOf<String>()

    inner class MyViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.gallery_list_item,
                parent,
                false
            ) as ImageView

        return MyViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val myBitmap = BitmapFactory.decodeFile(data[position])
        Glide
            .with(holder.imageView)
            .load(myBitmap)
            .into(holder.imageView)
    }

    override fun getItemCount() = data.size
}
