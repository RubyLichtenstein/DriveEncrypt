package com.example.driveencrypt

import android.app.Activity
import android.content.Context

object KeyValueStorage {

    private const val FOLDER_ID_KEY = "folder_id"
    private const val FILES_ID_KEY = "files_id"

    fun putFolderId(activity: Activity, folderId: String) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(FOLDER_ID_KEY, folderId)
            commit()
        }
    }

    fun getFolderId(activity: Activity): String? {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref) {
            return getString(FOLDER_ID_KEY, null)
        }
    }

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