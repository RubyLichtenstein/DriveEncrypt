package com.ruby.driveencrypt.gallery

import android.R.attr.data
import android.app.Activity
import android.app.Application
import android.app.RecoverableSecurityException
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


val REQUEST_PICK_IMAGES = 10
val REQUEST_PICK_VIDEO = 20

object MediaStorePicker {

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

    suspend fun queryImages(
        contentResolver: ContentResolver,
        uri: Uri
    ): List<MediaStoreImage> {
        val images = mutableListOf<MediaStoreImage>()

        /**
         * Working with [ContentResolver]s can be slow, so we'll do this off the main
         * thread inside a coroutine.
         */
        withContext(Dispatchers.IO) {

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )

            contentResolver.query(
                uri, //MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                getDataFromCursor(cursor, images)

            }
        }

        return images
    }

    private fun getDataFromCursor(
        cursor: Cursor,
        images: MutableList<MediaStoreImage>
    ) {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dateModifiedColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val displayNameColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        while (cursor.moveToNext()) {

            // Here we'll use the column indexs that we found above.
            val id = cursor.getLong(idColumn)
            val dateModified =
                Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
            val displayName = cursor.getString(displayNameColumn)


            /**
             * This is one of the trickiest parts:
             *
             * Since we're accessing images (using
             * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI], we'll use that
             * as the base URI and append the ID of the image to it.
             *
             * This is the exact same way to do it when working with [MediaStore.Video] and
             * [MediaStore.Audio] as well. Whatever `Media.EXTERNAL_CONTENT_URI` you
             * query to get the items is the base, and the ID is the document to
             * request there.
             */
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val image = MediaStoreImage(id, displayName, dateModified, contentUri)
            images += image

            // For debugging, we'll output the image objects we create to logcat.
            Log.v("TAG", "Added image: $image")
        }
    }
}