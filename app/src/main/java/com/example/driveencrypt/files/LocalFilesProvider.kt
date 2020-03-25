package com.example.driveencrypt.files

import android.content.Context
import android.os.FileObserver
import java.io.File

class LocalFilesProvider {
    fun getLocalFilesPaths(context: Context): List<String> {
        return context
            .filesDir
            .listFiles()
            .map { it.path }
    }

    fun observeLocal(context: Context): FileObserver {
        return object : FileObserver(context.filesDir.path) {
            override fun onEvent(event: Int, path: String?) {
//                Log.d("TAG", "event: $event, path: $path")
            }
        }
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