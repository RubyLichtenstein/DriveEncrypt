package com.example.driveencrypt.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log


class ImageGalleryHelper {

    fun selectImage(activity: Activity) {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(pickPhoto, 1)
    }

    fun onResultFromGallery(
        data: Intent,
        contentResolver: ContentResolver,
        onPicturePath: (List<String>) -> Unit
    ) {
        val selectedImage: Uri? = data.data
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val clipData = data.clipData
        if (clipData != null) {
            val paths = ArrayList<String>()
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                val uri = item.uri
                // Get the cursor
                val cursor: Cursor? = contentResolver
                    .query(uri, filePathColumn, null, null, null)
                // Move to first row
                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    paths.add(cursor.getString(columnIndex))
                    cursor.close()
                }
            }
            onPicturePath(paths)
        }

//        if (selectedImage != null) {
//
//            val cursor: Cursor? = contentResolver.query(
//                selectedImage,
//                filePathColumn,
//                null,
//                null,
//                null
//            )
//
//            if (cursor != null) {
//                cursor.moveToFirst()
//                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
//                val picturePath: String = cursor.getString(columnIndex)
//                cursor.close()
//
//                onPicturePath(picturePath)
//            }
//        }
    }
}