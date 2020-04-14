package com.ruby.driveencrypt.gallery

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.ruby.driveencrypt.drive.log
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.files.LocalFilesManager

class GalleryViewModel : ViewModel() {
    val localFilesLiveData = MutableLiveData<List<String>>()
    val fileAddedLiveData = MutableLiveData<Unit>()
    val refreshLiveData = MutableLiveData<Boolean>()
    val isInSelectionModeLiveData = MutableLiveData<Boolean>()

    fun showAllLocalFiles(context: Context) {
        val localFilesPaths = LocalFilesManager.getLocalFilesPaths(context).sorted()
        localFilesLiveData.value = localFilesPaths
    }

    fun refreshFiles(
        filesManager: FilesManager,
        context: Context
    ) {
        showAllLocalFiles(context)

        filesManager
            .downloadNotSyncFiles()
            .addOnSuccessListener {
                var counter = it.size
                it.forEach { task ->
                    task
                        .addOnSuccessListener {
//                            remoteFiles.add(it)
//                            fileAddedLiveData.value = it.path
                        }
                        .addOnCompleteListener {
                            counter--
                            if (counter == 0) {
                                refreshLiveData.value = false
                            }
                        }
                }
            }
    }

    fun handleImagePath(context: Context, paths: List<String>) {
        val tasks = paths.map {
            val file = java.io.File(it)
            LocalFilesManager.saveLocalFiles(context, file)
                .log(
                    "save_image",
                    file.name
                )
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                fileAddedLiveData.value = Unit
            }

//        deleteFile(file)
//        filesManager.uploadFile(picturePath)?.addOnSuccessListener { }
    }
}