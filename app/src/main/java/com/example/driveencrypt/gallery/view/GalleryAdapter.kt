package com.example.driveencrypt.gallery.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.driveencrypt.R
import com.example.driveencrypt.utils.displayMetrics
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.gallery_list_item.view.*
import java.io.File

class GalleryAdapter : BaseGalleryAdapter() {
    var mResizeOptions: ResizeOptions? = null

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
        val galleryItem = data[position]
        val uri = Uri.fromFile(File(galleryItem.path))

        val imageRequest =
            ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setResizeOptions(mResizeOptions)
                .build()

        holder.view.gallery_image.setImageRequest(imageRequest)

        holder.view.setOnClickListener {
            onClick?.invoke(galleryItem)
        }
    }
}
