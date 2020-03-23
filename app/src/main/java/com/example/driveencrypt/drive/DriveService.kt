package com.example.driveencrypt.drive

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.driveencrypt.crypto.CryptoUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DriveService(private val drive: Drive) {
    private val mExecutor: Executor =
        Executors.newSingleThreadExecutor()

    fun createFolder(name: String) = execute {
        val fileMetadata =
            File()
        fileMetadata.name = name
        fileMetadata.mimeType = "application/vnd.google-apps.folder"

        drive
            .files()
            .create(fileMetadata)
            .setFields("id")
            .execute()
    }

    private fun <V> execute(call: () -> V): Task<V> {
        return Tasks.call(mExecutor, Callable(call))
    }

    fun uploadFile(
        toUploadFile: java.io.File,
        folderId: String,
        fileName: String
    ) = execute {
        val mediaContent = FileContent("image/jpeg", toUploadFile)

        val fileMetadata = File()
        fileMetadata.name = fileName
        fileMetadata.parents = listOf(folderId)

        drive
            .files()
            .create(fileMetadata, mediaContent)
            .setFields("id")
            .execute()
    }

    fun downloadAndDecrypt(
        activity: Activity,
        fileId: String,
        callback: (java.io.File) -> Unit
    ) {
        val file = java.io.File(
            activity.filesDir,
            System.currentTimeMillis().toString() + "_downloaded_encrypted.jpg"
        )

        downloadFile(file, fileId)
            .addOnCompleteListener {
                Log.d("TAG", file.toString())

                if (file.exists()) {
                    Log.d("TAG", "file downloaded")
                    callback(file)
//                    val fileDecrypted = java.io.File(
//                        Environment.getExternalStorageDirectory(),
//                        System.currentTimeMillis().toString() + "_downloaded_decrypted.jpg"
//                    )

                    try {
//                        CryptoUtils.decrypt(CryptoUtils.key, file, fileDecrypted)
                    } catch (e: Exception) {
                        Log.e("TAG", e.message + " id: " + fileId, e)
                    }
                } else {
                    Log.d("TAG", "file downloaded not exist")
                }
            }
    }

    fun files(
        query: String
    ) = execute {
        var pageToken: String? = null
        val allFiles = mutableListOf<File>()

        do {
            val result: FileList = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)
                .execute()

            allFiles.addAll(result.files)

            pageToken = result.nextPageToken
        } while (pageToken != null)

        allFiles
    }

    private fun downloadFile(
        fileSaveLocation: java.io.File?,
        fileId: String?
    ) = execute {
        val outputStream: OutputStream = FileOutputStream(fileSaveLocation)

        drive
            .files()[fileId]
            .executeMediaAndDownloadTo(outputStream)
    }


    companion object {
        private fun getGoogleDriveService(
            context: Context?,
            account: GoogleSignInAccount,
            appName: String?
        ): Drive {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            return Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                )
                .setApplicationName(appName)
                .build()
        }

        fun getDriveService(context: Context): DriveService? {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            return if (account != null) {
                val googleDriveService = getGoogleDriveService(
                    context,
                    account,
                    "Android client 1"
                )

                DriveService(googleDriveService)
            } else {
                null
            }
        }

        private const val TAG = "DriveServiceHelper"
    }

}

fun <V> Task<V>.log(tag: String, msg: String) =
    addOnSuccessListener {
        Log.d(tag, "Success $msg, $it")
    }
        .addOnFailureListener {
            Log.d(tag, "Failure $msg, e: $it")
        }
        .addOnCompleteListener {
            Log.d(tag, "Complete $msg, task: $it")
        }
        .addOnCanceledListener {
            Log.d(tag, "Canceled $msg")
        }