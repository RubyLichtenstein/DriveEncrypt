package com.ruby.driveencrypt.lockscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.beautycoder.pflockscreen.PinPreferences
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.MainActivity

class LockScreenActivity : AppCompatActivity() {
    private val pinPreferences = PinPreferences()

    var editPassword: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        editPassword = intent.getBooleanExtra(EDIT_PASSWORD, false)

        if (editPassword) {
            val fragment = LockScreenFragment.newInstance(LockScreenFragment.Mode.CHANGE)
            replaceFragment(fragment)
        } else {
            showLockScreenFragment()
        }
    }

//    private val mCodeCreateListener: OnPFLockScreenCodeCreateListener =
//        object : OnPFLockScreenCodeCreateListener {
//            override fun onCodeCreated(encodedCode: String) {
//                Toast.makeText(this@LockScreenActivity, "Code created", Toast.LENGTH_SHORT).show()
//                if (editPassword) {
//                    // mode create
//                    finish()
//                } else {
//                    showMainActivity()
//                }
//            }
//
//            override fun onNewCodeValidationFailed() {
//                Toast.makeText(this@LockScreenActivity, "Code validation error", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//
//    private val mLoginListener: OnPFLockScreenLoginListener = object : OnPFLockScreenLoginListener {
//        override fun onCodeInputSuccessful() {
//            if (editPassword) {
//                showLockScreenFragment(isPinExist = false)
//            } else {
//                showMainActivity()
//            }
//        }
//
//        override fun onFingerprintSuccessful() {
//            showMainActivity()
//        }
//
//        override fun onPinLoginFailed() {
//            Toast.makeText(this@LockScreenActivity, "Pin failed", Toast.LENGTH_SHORT).show()
//        }
//
//        override fun onFingerprintLoginFailed() {
//            Toast.makeText(this@LockScreenActivity, "Fingerprint failed", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun showLockScreenFragment() {
        val pinExist = pinPreferences.isPinExist(this)
        showLockScreenFragment(pinExist)
    }

    private fun showLockScreenFragment(
        isPinExist: Boolean
    ) {
        val fragment = LockScreenFragment.newInstance(
            if (isPinExist)
                LockScreenFragment.Mode.AUTH
            else
                LockScreenFragment.Mode.CREATE
        )
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_view, fragment)
            .commit()
    }

//    private fun showMainActivity() {
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish()
//    }

    companion object {
        const val EDIT_PASSWORD = "EDIT_PASSWORD"

        fun editPassword(activity: Activity) {
            val intent = Intent(activity, LockScreenActivity::class.java)
            intent.putExtra(EDIT_PASSWORD, true)
            activity.startActivity(intent)
        }
    }
}