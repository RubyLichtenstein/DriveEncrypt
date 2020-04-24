package com.ruby.driveencrypt.gallery

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.files.RemoteFilesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    val localFilesLiveData = MutableLiveData<List<GalleryItem>>()
    val refreshLiveData = MutableLiveData<Boolean>()
    val isInSelectionModeLiveData = MutableLiveData<Boolean>()

    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete
    private var pendingDeleteImage: MediaStoreImage? = null

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

    fun handleImagePath(
        context: Context,
        paths: List<Uri>
    ) {
        viewModelScope.launch {
            paths.map {
                LocalFilesManager.saveLocalFiles(context, it)
                showAllLocalFiles(context)
            }
        }
    }

    suspend fun performDeleteImage(
        contentResolver: ContentResolver,
        image: MediaStoreImage
    ) {
        withContext(Dispatchers.IO) {
            try {
                contentResolver.delete(
                    image.contentUri,
                    "${MediaStore.Images.Media._ID} = ?",
                    arrayOf(image.id.toString())
                )
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException

                    // Signal to the Activity that it needs to request permission and
                    // try the delete again if it succeeds.
                    pendingDeleteImage = image
                    _permissionNeededForDelete.postValue(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    )
                } else {
                    throw securityException
                }
            }
        }
    }
}