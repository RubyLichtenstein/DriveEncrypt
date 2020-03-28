package com.example.driveencrypt.gallery.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.example.driveencrypt.utils.LocalFilesExecutor
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

object GalleryData {
    //    private val images = HashMap<String, Bitmap>()
    private lateinit var images: LruCache<String, Bitmap>
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

    init {
        val cacheSize = maxMemory / 2

        images = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }

    fun get(path: String): Task<Bitmap?> {
        val get: Bitmap? = images.get(path)
        return if (get != null) {
            Tasks.forResult(get)
        } else {
            LocalFilesExecutor
                .execute {
                    BitmapFactory.decodeFile(path)
                }
                .addOnSuccessListener {
                    it?.let { images.put(path, it) }
                }
        }
    }

//    fun clear(
//        data: MutableList<GalleryItem>,
//        startIndex: Int,
//        endIndex: Int
//    ) {
//        val delta = 6;
//
//        val start = if (startIndex - delta < 0)
//            startIndex
//        else
//            startIndex - delta
//
//        val end = if (endIndex + delta > images.size)
//            endIndex
//        else
//            endIndex + delta
//
//        (0..start).forEach {
//            val path = data[it].path
//            images.remove(path)
//        }
//
//        (end..images.size).forEach {
//            val path = data[it].path
//            images.remove(path)
//        }
//    }
}