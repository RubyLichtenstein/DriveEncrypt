package com.ruby.driveencrypt.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

val REQUEST_PICK_IMAGES = 10
val REQUEST_PICK_VIDEO = 20

class MediaStorePicker {

    fun selectImages(activity: Activity) {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(pickPhoto, REQUEST_PICK_IMAGES)
    }

    fun selectVideos(activity: Activity) {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(pickPhoto, REQUEST_PICK_VIDEO)
    }

    fun onResultFromGallery(
        intent: Intent,
        onPicturePath: (List<Uri>) -> Unit
    ) {
        val clipData = intent.clipData
        if (clipData != null) {
            val paths = ArrayList<Uri>()
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                val uri = item.uri
                paths.add(uri)
            }
            onPicturePath(paths)
        }
    }
}