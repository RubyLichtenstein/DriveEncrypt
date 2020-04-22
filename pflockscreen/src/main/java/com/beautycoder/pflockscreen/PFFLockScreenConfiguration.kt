package com.beautycoder.pflockscreen

import android.content.Context
import androidx.annotation.IntDef
import java.io.Serializable
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by Aleksandr Nikiforov on 2018/02/14.
 */
class PFFLockScreenConfiguration private constructor(builder: Builder) :
    Serializable {
    var nextButton = ""
    var isUseFingerprint = false
    var isAutoShowFingerprint = false
    var title = ""

    @get:PFLockScreenMode
    var mode = MODE_AUTH
    var codeLength = 4
    var isClearCodeOnError = false
    var isErrorVibration = true
    var isErrorAnimation = true
    var isNewCodeValidation = false
    var newCodeValidationTitle = ""

    class Builder(context: Context) {
        var mLeftButton = ""
        var mNextButton = ""
        var mUseFingerprint = false
        var mAutoShowFingerprint = false
        var mTitle = ""
        var mMode = 0
        var mCodeLength = 4
        var mClearCodeOnError = false
        var mErrorVibration = true
        var mErrorAnimation = true
        var mNewCodeValidation = false
        var mNewCodeValidationTitle = ""
        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setLeftButton(leftButton: String): Builder {
            mLeftButton = leftButton
            return this
        }

        fun setNextButton(nextButton: String): Builder {
            mNextButton = nextButton
            return this
        }

        fun setUseFingerprint(useFingerprint: Boolean): Builder {
            mUseFingerprint = useFingerprint
            return this
        }

        fun setAutoShowFingerprint(autoShowFingerprint: Boolean): Builder {
            mAutoShowFingerprint = autoShowFingerprint
            return this
        }

        fun setMode(@PFLockScreenMode mode: Int): Builder {
            mMode = mode
            return this
        }

        fun setCodeLength(codeLength: Int): Builder {
            mCodeLength = codeLength
            return this
        }

        fun setClearCodeOnError(clearCodeOnError: Boolean): Builder {
            mClearCodeOnError = clearCodeOnError
            return this
        }

        fun setErrorVibration(errorVibration: Boolean): Builder {
            mErrorVibration = errorVibration
            return this
        }

        fun setErrorAnimation(errorAnimation: Boolean): Builder {
            mErrorAnimation = errorAnimation
            return this
        }

        fun setNewCodeValidation(newCodeValidation: Boolean): Builder {
            mNewCodeValidation = newCodeValidation
            return this
        }

        fun setNewCodeValidationTitle(newCodeValidationTitle: String): Builder {
            mNewCodeValidationTitle = newCodeValidationTitle
            return this
        }

        fun build(): PFFLockScreenConfiguration {
            return PFFLockScreenConfiguration(
                this
            )
        }

        init {
            mTitle = context.resources
                .getString(R.string.lock_screen_title_pf)
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
        MODE_CREATE,
        MODE_AUTH
    )
    annotation class PFLockScreenMode
    companion object {
        const val MODE_CREATE = 0
        const val MODE_AUTH = 1
    }

    init {
        nextButton = builder.mNextButton
        isUseFingerprint = builder.mUseFingerprint
        isAutoShowFingerprint = builder.mAutoShowFingerprint
        title = builder.mTitle
        mode = builder.mMode
        codeLength = builder.mCodeLength
        isClearCodeOnError = builder.mClearCodeOnError
        isErrorVibration = builder.mErrorVibration
        isErrorAnimation = builder.mErrorAnimation
        isNewCodeValidation = builder.mNewCodeValidation
        newCodeValidationTitle = builder.mNewCodeValidationTitle
    }
}