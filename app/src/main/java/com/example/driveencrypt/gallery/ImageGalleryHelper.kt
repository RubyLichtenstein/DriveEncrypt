package com.example.driveencrypt.gallery

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
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
        activity.startActivityForResult(pickPhoto, 1)

//
//        val options =
//            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
//        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
//        builder.setTitle("Choose your profile picture")
//        builder.setItems(options) { dialog, item ->
//            when {
//                options[item] == "Take Photo" -> {
//                    val takePicture =
//                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    activity.startActivityForResult(takePicture, 0)
//                }
//                options[item] == "Choose from Gallery" -> {
//                    val pickPhoto = Intent(
//                        Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                    )
//                    activity.startActivityForResult(pickPhoto, 1)
//                }
//                options[item] == "Cancel" -> {
//                    dialog.dismiss()
//                }
//            }
//        }
//        builder.show()
    }

    fun onResultFromGallery(
        data: Intent,
        contentResolver: ContentResolver,
        onPicturePath: (String?) -> Unit
    ) {
        val selectedImage: Uri? = data.data
        Log.d("TAG", selectedImage.toString())

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
            } else {
                onPicturePath(null)
            }
        } else {
            onPicturePath(null)
        }
    }
}