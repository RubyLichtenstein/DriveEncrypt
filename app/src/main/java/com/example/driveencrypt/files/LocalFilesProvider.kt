package com.example.driveencrypt.files

import android.content.Context
import java.io.File

class LocalFilesProvider {
    fun getLocalFilesPaths(context: Context): List<String> {
        return context
            .filesDir
            .listFiles()
            .map { it.path }
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

    fun deleteAllFiles(context: Context) {
        context
            .filesDir
            .listFiles()
            .forEach { it.delete() }
    }
}