package com.example.driveencrypt.files

import android.content.Context
import com.example.driveencrypt.drive.DriveService
import com.example.driveencrypt.drive.log
import java.io.File

class FilesManager(
    private val driveService: DriveService
) {
    fun onNewFile() {

    }

    fun downloadFilesFromDriveAndSaveToLocal(
        context: Context,
        onSuccess: (File) -> Unit
    ) {
        driveService
            .files("mimeType='image/jpeg'")
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