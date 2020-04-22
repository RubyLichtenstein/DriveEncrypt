package com.ruby.driveencrypt.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import com.facebook.common.util.UriUtil


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
        contentResolver: ContentResolver,
        onPicturePath: (List<String>) -> Unit
    ) {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val clipData = intent.clipData
        if (clipData != null) {
            val paths = ArrayList<String>()
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                val uri = item.uri
                val path = UriUtil.getRealPathFromUri(
                    contentResolver,
                    uri
                )
                if (path != null) path.let {
                    paths.add(it)
                } else {
                    Log.e("TAG", "path is null")
                }
            }
            onPicturePath(paths)
        }
    }
}