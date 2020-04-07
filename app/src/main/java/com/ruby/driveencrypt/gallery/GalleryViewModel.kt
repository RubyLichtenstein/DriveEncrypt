package com.ruby.driveencrypt.gallery

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.files.LocalFilesManager
import com.google.api.services.drive.model.File

class GalleryViewModel : ViewModel() {
    val localFilesLiveData = MutableLiveData<List<String>>()
    val addFileLiveData = MutableLiveData<String>()
    val refreshLiveData = MutableLiveData<Boolean>()

    val remoteFiles = mutableListOf<File>()

    fun showAllLocalFiles(context: Context) {
        val localFilesPaths = LocalFilesManager.getLocalFilesPaths(context)
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
                            addFileLiveData.value = it.path
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
}