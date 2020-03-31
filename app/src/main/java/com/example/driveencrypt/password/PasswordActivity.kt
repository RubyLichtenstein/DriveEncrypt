package com.example.driveencrypt.password

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.example.driveencrypt.R


class PasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        val fragment = PFLockScreenFragment()

        val builder = PFFLockScreenConfiguration
            .Builder(this)
            .setMode(PFFLockScreenConfiguration.MODE_CREATE)

        fragment.setConfiguration(builder.build())
        fragment.setCodeCreateListener(object : PFLockScreenFragment.OnPFLockScreenCodeCreateListener {
            override fun onNewCodeValidationFailed() {

            }

            override fun onCodeCreated(encodedCode: String) {

            }
        })

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_view, fragment)
            .commit()
    }
}
