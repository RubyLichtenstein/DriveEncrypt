package com.beautycoder.pflockscreen.security

import android.content.Context
import com.beautycoder.pflockscreen.security.callbacks.PFPinCodeHelperCallback

interface IPFPinCodeHelper {

    fun encodePin(
        context: Context,
        pin: String,
        callBack: PFPinCodeHelperCallback<String>
    )

    fun checkPin(
        context: Context,
        encodedPin: String,
        pin: String,
        callback: PFPinCodeHelperCallback<Boolean>
    )

    fun delete(context: Context, callback: PFPinCodeHelperCallback<Boolean>)

    fun isPinCodeEncryptionKeyExist(context: Context, callback: PFPinCodeHelperCallback<Boolean>)
}