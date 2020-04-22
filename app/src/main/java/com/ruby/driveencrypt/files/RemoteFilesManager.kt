package com.ruby.driveencrypt.files

import android.content.Context
import android.util.Log
import com.ruby.driveencrypt.KeyValueStorage
import com.ruby.driveencrypt.drive.DriveService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.File

class RemoteFilesManager(
    private val context: Context,
    private val localFilesManager: LocalFilesManager,
    private val driveService: DriveService
) {

    companion object {
        fun create(context: Context): RemoteFilesManager? {
            val driveService = DriveService.getDriveService(context)
                ?: return null

            return RemoteFilesManager(
                context,
                LocalFilesManager,
                driveService
            )
        }
    }

    fun downloadNotSyncFiles(): Task<List<Task<File>>> =
        filesSyncStatus()
            .onSuccessTask {
                Tasks.forResult(
                    it.orEmpty()
                        .filter { it.syncStatus == SyncStatus.Remote }
                        .map {
                            downloadFile(
                                it.fileId!!, // todo
                                it.fileName
                            )
                        }
                )
            }

    fun uploadNotSyncFiles() =
        filesSyncStatus()
            .onSuccessTask {
                Tasks.forResult(
                    it.orEmpty()
                        .filter { it.syncStatus == SyncStatus.Local }
                        .map {
                            val path = context.filesDir.path + "/" + it.fileName
                            uploadFile(path)
                        }
                )
            }

    fun uploadFile(path: String): Task<com.google.api.services.drive.model.File>? {
        val file = File(path)
        val fileName = file.name

        val folderId =
            KeyValueStorage.getFolderId(context)

        if (folderId == null) {
            Log.e("TAG", "folderId == null")
            return null
        }

        return driveService
            .uploadFile(
                file,
                folderId,
                fileName
            )
    }

    enum class SyncStatus {
        Synced,
        Remote,
        Local
    }

    data class FileSyncStatus(
        val fileName: String,
        val syncStatus: SyncStatus,
        val fileId: String? = null
    )

    fun filesSyncStatus(): Task<Set<FileSyncStatus>> {
        return driveService
            .allImages()
            .onSuccessTask {
                val remoteFileNames: Set<com.google.api.services.drive.model.File> =
                    it.orEmpty().toSet()
                val localFilesNames = localFilesManager.getLocalFilesNames(context).toSet()
                val syncFilesDiff = syncFilesDiff(localFilesNames, remoteFileNames)

                Tasks.forResult(syncFilesDiff)
            }
    }

    private fun syncFilesDiff(
        localFilesNames: Set<String>,
        remoteFiles: Set<com.google.api.services.drive.model.File>
    ): Set<FileSyncStatus> {
        val diffResults = mutableSetOf<FileSyncStatus>()
        val remoteFilesNames = remoteFiles.map { it.name }.toSet()

        val synced = localFilesNames.intersect(remoteFilesNames)
        diffResults.addAll(synced.map { FileSyncStatus(it, SyncStatus.Synced) })

        val onlyLocal = localFilesNames.subtract(remoteFilesNames)
        diffResults.addAll(onlyLocal.map { FileSyncStatus(it, SyncStatus.Local) })

        val onlyRemote = remoteFilesNames.subtract(localFilesNames)
        diffResults.addAll(onlyRemote.map { name ->
            val file = remoteFiles.find { it.name == name }
            FileSyncStatus(name, SyncStatus.Remote, file?.id)
        })

        return diffResults;
    }

    private fun downloadFile(
        fileId: String,
        fileName: String
    ): Task<File> {
        val localFile = File(
            context.filesDir,
            fileName
        )

        return driveService
            .downloadFile(
                localFile,
                fileId
            )
            .onSuccessTask {
                Tasks.forResult(localFile)
            }
    }

    private val folderName = "encrypt"

    fun initFolderId(context: Context) {
        val folderId = KeyValueStorage.getFolderId(context)
        if (folderId == null) {
            driveService
                .files("name = '$folderName'")
                .addOnSuccessListener {
                    if (it.isEmpty()) {
                        driveService
                            .createFolder(folderName)
                            .addOnCompleteListener {
                                val folderId1 = it.result?.id
                                folderId1?.let { it1 ->
                                    KeyValueStorage.putFolderId(
                                        context,
                                        it1
                                    )
                                }
                            }
                    } else {
                        KeyValueStorage.putFolderId(
                            context,
                            folderId = it.first().id
                        )
                    }
                }
        }
    }
}