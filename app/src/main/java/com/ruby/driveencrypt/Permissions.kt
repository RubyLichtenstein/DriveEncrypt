package com.ruby.driveencrypt

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** The request code for requesting [Manifest.permission.READ_EXTERNAL_STORAGE] permission. */
const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

/**
 * Code used with [IntentSender] to request user permission to delete an image with scoped storage.
 */
const val DELETE_PERMISSION_REQUEST = 0x1033

/**
 * Convenience method to check if [Manifest.permission.READ_EXTERNAL_STORAGE] permission
 * has been granted to the app.
 */
object Permissions {
    fun haveStoragePermission(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Convenience method to request [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    fun requestPermission(activity: Activity) {
        if (!haveStoragePermission(activity)) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(activity, permissions, READ_EXTERNAL_STORAGE_REQUEST)
        }
    }
}

