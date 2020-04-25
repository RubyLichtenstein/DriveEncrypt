package com.ruby.driveencrypt.lockscreen

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.ruby.driveencrypt.R

object FingerprintHelper {
    fun isFingerprintAvailable(context: Context) =
        isFingerprintApiAvailable(context) && isFingerprintsExists(context)

    fun isFingerprintApiAvailable(context: Context): Boolean {
        return FingerprintManagerCompat.from(context).isHardwareDetected
    }

    fun isFingerprintsExists(context: Context): Boolean {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints()
    }

    fun showNoFingerprintDialog(context: Context) {
//        AlertDialog.Builder(context)
//            .setTitle(R.string.no_fingerprints_title_pf)
//            .setMessage(R.string.no_fingerprints_message_pf)
//            .setCancelable(true)
//            .setNegativeButton(R.string.cancel_pf, null)
//            .setPositiveButton(R.string.settings_pf) { dialog, which ->
//                context.startActivity(
//                    Intent(Settings.ACTION_SECURITY_SETTINGS)
//                )
//            }.create().show()
    }
}