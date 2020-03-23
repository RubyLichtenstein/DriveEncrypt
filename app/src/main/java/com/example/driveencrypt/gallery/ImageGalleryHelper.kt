package com.example.driveencrypt.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log

class ImageGalleryHelper {

    fun selectImage(activity: Activity) {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(pickPhoto, 1)
    }

    fun onResultFromGallery(
        data: Intent,
        contentResolver: ContentResolver,
        onPicturePath: (String) -> Unit
    ) {
        val selectedImage: Uri? = data.data
        val filePathColumn =
            arrayOf(MediaStore.Images.Media.DATA)
        if (selectedImage != null) {

            val cursor: Cursor? = contentResolver.query(
                selectedImage,
                filePathColumn,
                null,
                null,
                null
            )

            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                val picturePath: String = cursor.getString(columnIndex)
                cursor.close()

                onPicturePath(picturePath)
            }
        }
    }
}