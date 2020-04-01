package com.example.driveencrypt.lockscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.PinPreferences
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment.OnPFLockScreenCodeCreateListener
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment.OnPFLockScreenLoginListener
import com.example.driveencrypt.R
import com.example.driveencrypt.gallery.view.GalleryActivity

class LockScreenActivity : AppCompatActivity() {
    private val pinPreferences = PinPreferences()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_password)
        val editPassword = intent.getBooleanExtra(EDIT_PASSWORD, false)
        if (editPassword) {
            showLockScreenFragment()
        } else {
            showLockScreenFragment()
        }
    }

    private val mCodeCreateListener: OnPFLockScreenCodeCreateListener =
        object : OnPFLockScreenCodeCreateListener {
            override fun onCodeCreated(encodedCode: String) {
                Toast.makeText(this@LockScreenActivity, "Code created", Toast.LENGTH_SHORT).show()
//                PreferencesSettings.saveToPref(this@MainActivity, encodedCode)
            }

            override fun onNewCodeValidationFailed() {
                Toast.makeText(this@LockScreenActivity, "Code validation error", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val mLoginListener: OnPFLockScreenLoginListener = object : OnPFLockScreenLoginListener {
        override fun onCodeInputSuccessful() {
            Toast.makeText(this@LockScreenActivity, "Code successfull", Toast.LENGTH_SHORT).show()
            showMainFragment()
        }

        override fun onFingerprintSuccessful() {
            Toast.makeText(this@LockScreenActivity, "Fingerprint successfull", Toast.LENGTH_SHORT)
                .show()
            showMainFragment()
        }

        override fun onPinLoginFailed() {
            Toast.makeText(this@LockScreenActivity, "Pin failed", Toast.LENGTH_SHORT).show()
        }

        override fun onFingerprintLoginFailed() {
            Toast.makeText(this@LockScreenActivity, "Fingerprint failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLockScreenFragment() {
        val pinExist = pinPreferences.isPinExist(this)
        showLockScreenFragment(pinExist)
    }

    private fun showLockScreenFragment(isPinExist: Boolean) {
        val builder = PFFLockScreenConfiguration.Builder(this)
            .setTitle(if (isPinExist) "Unlock with your pin code or fingerprint" else "Create Code")
            .setCodeLength(4)
            .setLeftButton("Can't remeber")
            .setNewCodeValidation(true)
            .setNewCodeValidationTitle("Please input code again")
            .setUseFingerprint(true)
        val fragment = PFLockScreenFragment()
//        fragment.setOnLeftButtonClickListener {
//            Toast.makeText(
//                this@MainActivity,
//                "Left button pressed",
//                Toast.LENGTH_LONG
//            ).show()
//        }
        builder.setMode(if (isPinExist) PFFLockScreenConfiguration.MODE_AUTH else PFFLockScreenConfiguration.MODE_CREATE)
        if (isPinExist) {
//            fragment.setEncodedPinCode(PreferencesSettings.getCode(this))
            fragment.setLoginListener(mLoginListener)
        }
        fragment.setConfiguration(builder.build())
        fragment.setCodeCreateListener(mCodeCreateListener)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_view, fragment).commit()
    }

    private fun showMainFragment() {
        val intent = Intent(this, GalleryActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EDIT_PASSWORD = "EDIT_PASSWORD"

        fun editPassword(activity: Activity) {
            val intent = Intent(activity, LockScreenActivity::class.java)
            intent.putExtra(EDIT_PASSWORD, true)
            activity.startActivity(intent)
        }
    }
}