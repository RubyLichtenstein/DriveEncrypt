package com.example.driveencrypt.files

import android.content.Context
import android.os.FileObserver
import java.io.File

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
                it.write(file.readBytes())
            }
    }

    fun deleteAllLocalFiles(context: Context) {
        context
            .filesDir
            .listFiles()
            .forEach { it.delete() }
    }
}