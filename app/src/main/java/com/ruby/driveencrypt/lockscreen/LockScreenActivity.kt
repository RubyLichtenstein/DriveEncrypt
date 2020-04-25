package com.ruby.driveencrypt.lockscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ruby.driveencrypt.R

class LockScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        val editPassword = intent.getBooleanExtra(EDIT_PASSWORD, false)
        val fragment = LockScreenFragment.newInstance(
            change = editPassword
        )
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_view, fragment)
            .commit()
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