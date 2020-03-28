package com.example.driveencrypt.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun shareImage(
    context: Context,
    filePath: String
) {
    val fileToShare = File(filePath)

    val fileUri: Uri? = try {
        FileProvider.getUriForFile(
            context,
            "com.example.driveencrypt.fileprovider",
            fileToShare
        )
    } catch (e: IllegalArgumentException) {
        Log.e(
            "File Selector",
            "The selected file can't be shared: $fileToShare",
            e
        )
        null
    }

    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/*"
    share.putExtra(
        Intent.EXTRA_STREAM,
        fileUri
    )
    context.startActivity(Intent.createChooser(share, "Share Image"))
}