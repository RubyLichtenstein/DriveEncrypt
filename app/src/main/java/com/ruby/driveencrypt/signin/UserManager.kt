package com.ruby.driveencrypt.signin

import android.content.Context
import com.ruby.driveencrypt.files.LocalFilesManager
import com.google.android.gms.tasks.Task

class UserManager(
    private val localFilesManager: LocalFilesManager,
    private val googleSignInHelper: GoogleSignInHelper
) {
    fun signOut(context: Context): Task<Void> {
        localFilesManager.deleteAllLocalFiles(context)
        return googleSignInHelper.signOut()
    }
}