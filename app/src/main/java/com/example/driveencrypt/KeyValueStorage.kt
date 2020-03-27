package com.example.driveencrypt

import android.app.Activity
import android.content.Context

object KeyValueStorage {

    private const val SP_NAME = "drive_enc_pref_name"
    private const val FOLDER_ID_KEY = "folder_id"
    private const val FILES_ID_KEY = "files_id"

    fun putFolderId(context: Context, folderId: String) {
        with(sharedPreferences(context).edit()) {
            putString(FOLDER_ID_KEY, folderId)
            commit()
        }
    }

    fun getFolderId(context: Context): String? {
        with(sharedPreferences(context)) {
            return getString(FOLDER_ID_KEY, null)
        }
    }

    private fun sharedPreferences(context: Context) =
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    fun putFilesId(activity: Activity, filesId: Set<String>) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet(FILES_ID_KEY, filesId)
            commit()
        }
    }

    fun getFilesId(activity: Activity): Set<String>? {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref) {
            return getStringSet(FILES_ID_KEY, null)
        }
    }
}