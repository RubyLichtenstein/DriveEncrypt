package com.example.driveencrypt.gallery.pager

import android.graphics.drawable.Animatable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.driveencrypt.R
import com.example.driveencrypt.gallery.view.BaseGalleryAdapter
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.image_pager_list_item.view.*
import java.io.File


class ImagePagerAdapter : BaseGalleryAdapter() {

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val galleryItem = data[position]
        val uri = Uri.fromFile(File(galleryItem.path))

        val mPhotoDraweeView = holder.view.page_image//.setImageRequest(imageRequest)

        val controller = Fresco.newDraweeControllerBuilder()
        controller.setUri(uri)
        controller.oldController = mPhotoDraweeView.getController()
        controller.controllerListener = object : BaseControllerListener<ImageInfo?>() {
            override fun onFinalImageSet(
                id: String,
                imageInfo: ImageInfo?,
                animatable: Animatable?
            ) {
                super.onFinalImageSet(id, imageInfo, animatable)
                if (imageInfo == null || mPhotoDraweeView == null) {
                    return
                }
                mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight())
            }
        }
        mPhotoDraweeView.setController(controller.build())
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
