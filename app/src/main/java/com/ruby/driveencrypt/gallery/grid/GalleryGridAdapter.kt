package com.ruby.driveencrypt.gallery.grid

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.utils.displayMetrics
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter
import kotlinx.android.synthetic.main.gallery_list_item.view.*
import java.io.File

class GalleryGridAdapter : BaseGalleryAdapter() {
    var mResizeOptions: ResizeOptions? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.gallery_list_item,
                parent,
                false
            )

        rootView.gallery_image.layoutParams.height = rootView.displayMetrics().widthPixels / 3
        return ImageViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val galleryItem = data[position]
        val uri = Uri.fromFile(File(galleryItem.path))

        val imageRequest =
            ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setResizeOptions(mResizeOptions)
                .build()

        holder.itemView.gallery_image.setImageRequest(imageRequest)

        holder.itemView.setOnClickListener {
            onClick?.invoke(it, galleryItem)
        }
    }
}
