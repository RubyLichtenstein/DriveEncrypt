package com.ruby.driveencrypt.files

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.util.IOUtils
import com.ruby.driveencrypt.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object LocalFilesManager {
    private val mExecutor: Executor =
        Executors.newCachedThreadPool()

    private const val THUMBNAIL_PREFIX = "thumbnail_"

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

    suspend fun saveLocalFiles(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            with(context.contentResolver) {
                openFileDescriptor(
                    uri,
                    "r",
                    null
                )?.let {
                    val inputStream = FileInputStream(it.fileDescriptor)

                    val file = File(
                        context.filesDir,
                        getFileName(uri)
                    )

                    val outputStream = FileOutputStream(file)
                    IOUtils.copy(inputStream, outputStream)
                }
            }
        }
    }

    // todo move out
    fun ContentResolver.getFileName(fileUri: Uri): String {

        var name = ""
        val returnCursor = this.query(
            fileUri,
            null,
            null,
            null,
            null
        )

        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
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

    fun deleteLocal(path: String) = File(path).delete()
}