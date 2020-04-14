package com.ruby.driveencrypt.media

import android.content.ContentProvider
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size

object ThumbnailUtils {
    fun a(
        contentResolver: ContentResolver,
        uri: Uri
    ) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            contentResolver.loadThumbnail(
//                uri,
//                Size(1, 1),
//                null
//            )
//        } else {
//            MediaStore.Images.Thumbnails.getThumbnail(
//                contentResolver,
//
//                )
//        }
    }
}