package com.example.driveencrypt.gallery.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.driveencrypt.R
import com.example.driveencrypt.signin.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.dialog_signin.*
import kotlinx.android.synthetic.main.dialog_signin.view.*
import kotlinx.android.synthetic.main.dialog_signin.view.googleSignInButton
import kotlinx.android.synthetic.main.dialog_signin.view.lockscreen

class UserDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            val googleSignInHelper = GoogleSignInHelper(it)
            val view = inflater.inflate(R.layout.dialog_signin, null)
            val account = GoogleSignIn.getLastSignedInAccount(it)

            if (account != null) {
                displayAccount(view, account)
                view.logout.visibility = View.VISIBLE
                view.googleSignInButton.visibility = View.GONE

                view.logout.setOnClickListener {
                    googleSignInHelper.signOut()
                }
            } else {
                view.logout.visibility = View.GONE
                view.googleSignInButton.visibility = View.VISIBLE
                view.googleSignInButton.setOnClickListener {
                    googleSignInHelper.signIn()
                }
            }

            lockscreen.setOnClickListener {

            }

            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun displayAccount(view: View, account: GoogleSignInAccount) {
        view.user_name.text = account?.displayName
        view.email.text = account?.email
    }
}