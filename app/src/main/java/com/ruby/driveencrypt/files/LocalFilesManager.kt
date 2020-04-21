package com.ruby.driveencrypt.files

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import com.facebook.common.media.MediaUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


object LocalFilesManager {
    private val mExecutor: Executor =
        Executors.newCachedThreadPool()

    private const val THUMBNAIL_PREFIX = "thumbnail_"
    const val DIR_VIDEO = "video"
    const val DIR_IMAGE = "image"
    const val DIR_THUMBNAIL = "thumbnail"

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

    fun tnPath(context: Context, file: File) =
        context.filesDir.path + "/" + thumbnailFileName(
            file.name
        )

    fun getUriIfVideoThumbnail(
        context: Context,
        path: String
    ): Uri {
        return if (isVideoFile(path)) {
            val file = File(path)
            Uri.fromFile(File(LocalFilesManager.tnPath(context, file)))
        } else {
            Uri.fromFile(File(path))
        }
    }

    fun isVideoFile(path: String): Boolean {
        return MediaUtils.isVideo(MediaUtils.extractMime(path))
    }

    fun getLocalFilesPaths(context: Context) =
        filesDirListFiles(context)
            .map { it.path }
            .filterNot { it.contains(THUMBNAIL_PREFIX) }
            .sorted()

    fun getLocalFilesNames(context: Context) =
        filesDirListFiles(context)
            .map { it.name }
            .filterNot { it.contains(THUMBNAIL_PREFIX) }
            .sorted()

    fun thumbnailFileName(originalName: String) =
        "thumbnail_${originalName.substringBefore(".")}.PNG"

    fun cropCenter(bitmap: Bitmap, size: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(bitmap, size, size)
    }

    private fun saveVideoThumbnail(context: Context, file: File) {
        val fileName = thumbnailFileName(file.name)

        val thumbnail = ThumbnailUtils.createVideoThumbnail(
            file.path,
            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
        )!!

// val size = context.displayMetrics().widthPixels / 3
// cropCenter(thumbnail, size)

        saveBitmapLocalFiles(
            context,
            fileName,
            thumbnail
        )
    }

    private fun saveImageThumbnail(context: Context, file: File) {
        val fileName = thumbnailFileName(file.name)

        val thumbnail = ThumbnailUtils.createImageThumbnail(
            file.path,
            MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
        )!!

// val size = context.displayMetrics().widthPixels / 3
// cropCenter(thumbnail, size)

        saveBitmapLocalFiles(
            context,
            fileName,
            thumbnail
        )
    }

    fun saveLocalFiles(
        context: Context,
        file: File
    ): Task<Unit> {
        return execute {
            if (isVideoFile(file.name))
                saveVideoThumbnail(context, file)
            else
                saveImageThumbnail(context, file)

            saveLocalFile(context, file)
        }
    }

    private fun saveLocalFile(context: Context, file: File) {
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

    fun saveBitmapLocalFiles(
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
        filesDirListFiles(context)
            .forEach { it.delete() }
    }

    private fun filesDirListFiles(context: Context): Array<out File> = context
        .filesDir
        .listFiles()
}