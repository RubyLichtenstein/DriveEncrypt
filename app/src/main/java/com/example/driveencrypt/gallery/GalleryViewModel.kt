package com.example.driveencrypt.gallery

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driveencrypt.files.FilesManager
import com.example.driveencrypt.files.LocalFilesManager

class GalleryViewModel : ViewModel() {
    val localFilesLiveData = MutableLiveData<List<String>>()
    val addFileLiveData = MutableLiveData<String>()
    val refreshLiveData = MutableLiveData<Boolean>()

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