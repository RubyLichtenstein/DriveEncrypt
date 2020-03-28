package com.example.driveencrypt.signin

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope

class GoogleSignInHelper(private val context: Activity) {
    val RC_SIGN_IN = 100

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }


    fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        context.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun isUserSignedIn(context: Context): Boolean =
        GoogleSignIn.getLastSignedInAccount(context) != null

    fun signOut() = googleSignInClient.signOut()
}