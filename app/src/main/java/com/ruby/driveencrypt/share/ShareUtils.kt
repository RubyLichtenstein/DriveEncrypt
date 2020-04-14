package com.ruby.driveencrypt.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import java.io.File


fun getUriForFile(
    context: Context,
    fileToShare: File
): Uri? {
    return try {
        FileProvider.getUriForFile(
            context,
            "com.ruby.driveencrypt.fileprovider",
            fileToShare
        )
    } catch (e: IllegalArgumentException) {
        Log.e(
            "getUriForFile",
            "The selected file can't be getUriForFile: $fileToShare",
            e
        )
        null
    }
}

fun shareImage(
    context: Context,
    filePath: String
) {
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(
            Intent.EXTRA_STREAM,
            getUriForFile(context, File(filePath))
        )
    }
    context.startActivity(Intent.createChooser(share, "Share Image"))
}

fun shareMultipleImage(
    context: Context,
    filePaths: List<String>
) {
    val uris = filePaths.mapNotNullTo(ArrayList()) {
        getUriForFile(context, File(it))
    }

    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
    }

    context.startActivity(Intent.createChooser(intent, "Share Images"))
}