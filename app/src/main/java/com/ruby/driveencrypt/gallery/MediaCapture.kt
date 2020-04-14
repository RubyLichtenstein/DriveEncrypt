package com.ruby.driveencrypt.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.ruby.driveencrypt.share.getUriForFile
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

val REQUEST_TAKE_PHOTO = 3
val REQUEST_VIDEO_CAPTURE = 4

class MediaCapture {

    var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun dispatchTakeVideoIntent(activity: Activity) {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(activity.packageManager)?.also {
                activity.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }


    fun dispatchTakePictureIntent(activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                // Create the File where the photo should go
//                val photoFile: File? = try {

//                } catch (ex: IOException) {
                // Error occurred while creating the File
//                    ...
//                    null
//                }
                // Continue only if the File was successfully created
                createImageFile(activity).also {
                    val photoURI: Uri = getUriForFile(activity, it)!! // todo
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

}