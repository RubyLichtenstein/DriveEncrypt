package com.example.driveencrypt.gallery.view

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driveencrypt.R
import com.example.driveencrypt.gallery.data.GalleryData
import com.example.driveencrypt.utils.displayMetrics
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.gallery_list_item.view.*

class GalleryAdapter : BaseGalleryAdapter() {

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

    var onSuccessListener: OnSuccessListener<Bitmap?> = OnSuccessListener { }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val galleryItem = data[position]
        holder.view.gallery_image.setImageDrawable(null)

        onSuccessListener = OnSuccessListener {
            if (it != null) {
                Glide
                    .with(holder.view.gallery_image)
                    .load(it)
                    .into(holder.view.gallery_image)
            }
        }

        GalleryData
            .get(galleryItem.path)
            .addOnSuccessListener(onSuccessListener)

        holder.view.setOnClickListener {
            onClick?.invoke(galleryItem)
        }
    }
}
