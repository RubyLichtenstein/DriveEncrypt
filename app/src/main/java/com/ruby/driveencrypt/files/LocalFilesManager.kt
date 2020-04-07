package com.ruby.driveencrypt.files

import android.content.Context
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

    fun deleteAllLocalFiles(context: Context) {
        context
            .filesDir
            .listFiles()
            .forEach { it.delete() }
    }
}