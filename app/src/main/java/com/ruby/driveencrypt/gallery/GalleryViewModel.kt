package com.ruby.driveencrypt.gallery

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.ruby.driveencrypt.drive.log
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.files.LocalFilesManager
import java.io.File

class GalleryViewModel : ViewModel() {
    val localFilesLiveData = MutableLiveData<List<GalleryItem>>()
    val refreshLiveData = MutableLiveData<Boolean>()
    val isInSelectionModeLiveData = MutableLiveData<Boolean>()

    fun showAllLocalFiles(context: Context) {
        val localFilesPaths = LocalFilesManager
            .getLocalFilesPaths(context)
            .map { GalleryItem(it) }

        localFilesLiveData.value = localFilesPaths
    }

    fun refreshSyncedStatusAndEmit(
        remoteFilesManager: RemoteFilesManager
    ) {
        refreshSyncedStatus(remoteFilesManager)
            .addOnSuccessListener {
                if (it != null) {
                    localFilesLiveData.value = it
                }
            }
    }

    private fun refreshSyncedStatus(
        remoteFilesManager: RemoteFilesManager
    ) = remoteFilesManager
        .filesSyncStatus()
        .onSuccessTask { syncStatus ->
            val localFiles = localFilesLiveData.value
            Tasks.call {
                localFiles?.map { path ->
                    galleryItemWithSyncStatus(path, syncStatus)
                }
            }
        }

    private fun galleryItemWithSyncStatus(
        galleryItem: GalleryItem,
        syncStatus: Set<RemoteFilesManager.FileSyncStatus>?
    ): GalleryItem {
        val fileName = File(galleryItem.path).name
        val status = syncStatus
            ?.find { it.fileName == fileName }
            ?.syncStatus

        return galleryItem.copy(synced = status)
    }


    fun refreshFiles(
        remoteFilesManager: RemoteFilesManager,
        context: Context
    ) {
        showAllLocalFiles(context)

        remoteFilesManager
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
                showAllLocalFiles(context)
            }

//        deleteFile(file)
//        filesManager.uploadFile(picturePath)?.addOnSuccessListener { }
    }
}