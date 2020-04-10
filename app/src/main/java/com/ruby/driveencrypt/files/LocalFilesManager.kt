package com.ruby.driveencrypt.files

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.IOException

object LocalFilesManager {

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

    fun saveToLocalFiles(
        context: Context,
        file: File
    ) {
        context.openFileOutput(
            file.name,
            Context.MODE_PRIVATE
        )
            .use {
//                try {
                it.write(file.readBytes())
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