package com.ruby.driveencrypt.lockscreen

import android.content.Context

class PinPreferences {

    private val pinCodePrefKey = "code_pref_key"

    private fun sharedPreferences(context: Context) =
        context.getSharedPreferences(pinCodePrefKey, Context.MODE_PRIVATE)

    private val PIN_KEY = "pin_key"

    fun savePin(
        context: Context,
        pin: String
    ) {
        sharedPreferences(context).edit().putString(PIN_KEY, pin).apply()
    }

    fun checkPin(
        context: Context,
        pin: String
    ): Boolean {
        val storedPin = sharedPreferences(context).getString(PIN_KEY, null)
        return storedPin == pin
    }

    fun delete(
        context: Context
    ) {
        sharedPreferences(context).edit().remove(PIN_KEY).apply()
    }

    fun isPinExist(
        context: Context
    ): Boolean {
        return sharedPreferences(context).getString(PIN_KEY, null) != null;
    }
}