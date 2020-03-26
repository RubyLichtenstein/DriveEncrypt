package com.example.driveencrypt.files

import android.content.ComponentCallbacks
import android.content.Context
import com.example.driveencrypt.drive.DriveService
import com.example.driveencrypt.drive.log
import java.io.File

class FilesManager(
    private val driveService: DriveService,
    private val localFilesProvider: LocalFilesProvider
) {
    fun onNewFile() {

    }

    enum class DiffResult {
        Synced,
        Remote,
        Local
    }

    data class FileDiffResult(
        val fileName: String,
        val diffResult: DiffResult
    )

    fun syncFiles(context: Context, callback: (Set<FileDiffResult>) -> Unit) {
        driveService
            .allImages()
            .addOnSuccessListener {
                val remoteFileNames = it.map { it.name }.toSet()
                val localFilesNames = localFilesProvider.getLocalFilesNames(context).toSet()
                val syncFilesDiff = syncFilesDiff(localFilesNames, remoteFileNames)
                callback(syncFilesDiff)
            }
    }

    private fun syncFilesDiff(
        localFilesNames: Set<String>,
        remoteFilesNames: Set<String>
    ): Set<FileDiffResult> {
        val diffResults = mutableSetOf<FileDiffResult>()

        val intersect = localFilesNames.intersect(remoteFilesNames)
        diffResults.addAll(intersect.map { FileDiffResult(it, DiffResult.Synced) })

        val onlyLocal = localFilesNames.subtract(remoteFilesNames)
        diffResults.addAll(onlyLocal.map { FileDiffResult(it, DiffResult.Local) })

        val onlyRemote = remoteFilesNames.subtract(localFilesNames)
        diffResults.addAll(onlyRemote.map { FileDiffResult(it, DiffResult.Remote) })

        return diffResults;
    }

    fun downloadFilesFromDriveAndSaveToLocal(
        context: Context,
        onSuccess: (File) -> Unit
    ) {
        driveService
            .allImages()
            .addOnSuccessListener {
                for (file in it) {
                    val localFile = File(
                        context.filesDir,
                        file.name
                    )

                    driveService
                        .downloadFile(
                            localFile,
                            file.id
                        )
                        .addOnSuccessListener {
                            onSuccess(localFile)
                        }.log(
                            "TAG", "downloadFile(${file.id}, ${file.name})"
                        )
                }
            }
    }
}