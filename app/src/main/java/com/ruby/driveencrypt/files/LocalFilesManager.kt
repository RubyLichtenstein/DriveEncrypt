package com.ruby.driveencrypt.files

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object LocalFilesManager {
    private val mExecutor: Executor =
        Executors.newCachedThreadPool()

    private fun <V> execute(call: () -> V): Task<V> {
        return Tasks.call(mExecutor, Callable {
            // todo try catch
//            try {
            call()
//            } catch (e: IOException) {
//                Log.e("TAG", "execute", e)
//                null
//            }
        })
    }

    fun getLocalFilesPaths(context: Context): List<String> {
        return context
            .filesDir
            .listFiles()
            .map { it.path }
    }

    fun getLocalFilesNames(context: Context): List<String> {
        return context
            .filesDir
            .listFiles()
            .map { it.name }
    }

    fun thumbnailFileName(originalName: String) =
        "thumbnail_${originalName.substringBefore(".")}.PNG"

    private fun saveVideoThumbnail(context: Context, file: File) {
        val fileName = thumbnailFileName(file.name)

        val thumbnail =
            ThumbnailUtils.createVideoThumbnail(
                file.path,
                MediaStore.Video.Thumbnails.MINI_KIND
            )

        saveToLocalFiles(
            context,
            fileName,
            thumbnail
        )
    }

    fun saveToLocalFiles(
        context: Context,
        file: File
    ) = execute {
        context.openFileOutput(
            file.name,
            Context.MODE_PRIVATE
        )
            .use { out ->
                FileInputStream(file).use { inputStream ->
                    val buff = ByteArray(1024)
                    var len: Int

                    while (inputStream.read(buff).also { len = it } > 0) {
                        out.write(buff, 0, len)
                    }
                }
// todo try catch IO
//                try {
//                out.write(file.readBytes())
//                } catch (e: IOException) {
//                    Log.e("TAG", "", e)
//                }
            }
    }

    fun saveToLocalFiles(
        context: Context,
        fileName: String,
        bitmap: Bitmap
    ) {
        context.openFileOutput(
            fileName,
            Context.MODE_PRIVATE
        )
            .use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    out
                )
            }
    }

    fun deleteAllLocalFiles(context: Context) {
        context
            .filesDir
            .listFiles()
            .forEach { it.delete() }
    }
}