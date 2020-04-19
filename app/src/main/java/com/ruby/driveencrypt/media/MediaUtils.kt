package com.ruby.driveencrypt.media

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File

private fun deleteFile(context: Context, file: File) {
    val delete = file.delete()
    MediaScannerConnection.scanFile(
        context,
        arrayOf(Environment.getExternalStorageDirectory().toString()),
        null
    ) { path, uri ->
        //            Log.i("ExternalStorage", "Scanned $path:")
        //            Log.i("ExternalStorage", "-> uri=$uri")
    }
}