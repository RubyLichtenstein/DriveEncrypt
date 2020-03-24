package com.example.driveencrypt.gallery

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driveencrypt.R
import com.example.driveencrypt.utils.displayMetrics
import kotlinx.android.synthetic.main.gallery_list_item.view.*


class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.MyViewHolder>() {

    val data = mutableListOf<String>()

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.gallery_list_item,
                parent,
                false
            )

        rootView.gallery_image.layoutParams.height = rootView.displayMetrics().widthPixels / 3
        return MyViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val myBitmap = BitmapFactory.decodeFile(data[position])
        Glide
            .with(holder.view.gallery_image)
            .load(myBitmap)
            .into(holder.view.gallery_image)
    }

    override fun getItemCount() = data.size
}
