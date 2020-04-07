package com.ruby.driveencrypt.gallery.pager

import android.app.Activity
import android.graphics.drawable.Animatable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.gallery.view.BaseGalleryAdapter
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import kotlinx.android.synthetic.main.image_pager_list_item.view.page_image
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

//        mPhotoDraweeView.setOnPhotoTapListener { view, x, y ->
//            mPhotoDraweeView.window().decorView.apply {
//                systemUiVisibility =
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//            }
//        }
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
