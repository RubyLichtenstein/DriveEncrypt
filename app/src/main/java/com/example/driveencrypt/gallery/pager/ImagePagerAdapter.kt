package com.example.driveencrypt.gallery.pager

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.driveencrypt.R
import com.example.driveencrypt.gallery.data.GalleryData
import com.example.driveencrypt.gallery.view.BaseGalleryAdapter
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.gallery_list_item.view.*
import kotlinx.android.synthetic.main.image_pager_list_item.view.*

class ImagePagerAdapter : BaseGalleryAdapter() {

    var task: Task<Bitmap?>? = null

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val galleryItem = data[position]

        task = GalleryData
            .get(galleryItem.path)
            .addOnSuccessListener {
                if (it != null) {
                    val into = Glide
                        .with(holder.view.page_image)
                        .load(it)
                        .into(holder.view.page_image)

                    into.clearOnDetach()
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.image_pager_list_item,
                parent,
                false
            )

        return MyViewHolder(rootView)
    }
}
