package com.ruby.driveencrypt.lockscreen

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.PinPreferences
import com.beautycoder.pflockscreen.fragments.PFFingerprintAuthDialogFragment
import com.beautycoder.pflockscreen.fragments.PFFingerprintAuthListener
import com.beautycoder.pflockscreen.views.PFCodeView
import com.beautycoder.pflockscreen.views.PFCodeView.OnPFCodeListener
import com.ruby.driveencrypt.R

class PFLockScreenFragment : Fragment() {
    val pinPreferences = PinPreferences()

    private lateinit var mFingerprintButton: View
    private lateinit var mDeleteButton: View

    //    private lateinit var mLeftButton: TextView
    private lateinit var mNextButton: Button
    private lateinit var mCodeView: PFCodeView
    private lateinit var titleView: TextView
    private var mUseFingerPrint = true
    private var mFingerprintHardwareDetected = false
    private var mIsCreateMode = false
    private var mCodeCreateListener: OnPFLockScreenCodeCreateListener? = null
    private var mLoginListener: OnPFLockScreenLoginListener? = null
    private var mCode = ""
    private var mCodeValidation = ""
    private var mEncodedPinCode = ""
    private var mConfiguration: PFFLockScreenConfiguration? = null
    private var mRootView: View? = null

    private var mOnLeftButtonClickListener: View.OnClickListener? = null
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(
            INSTANCE_STATE_CONFIG,
            mConfiguration
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_lock_screen_pf, container,
            false
        )
        if (mConfiguration == null) {
            mConfiguration = savedInstanceState!!.getSerializable(
                INSTANCE_STATE_CONFIG
            ) as PFFLockScreenConfiguration
        }
        mFingerprintButton = view.findViewById(R.id.button_finger_print)
        mDeleteButton = view.findViewById(R.id.button_delete)
//        mLeftButton = view.findViewById(R.id.button_left)
        mNextButton = view.findViewById(R.id.button_next)
        mDeleteButton.setOnClickListener(mOnDeleteButtonClickListener)
        mDeleteButton.setOnLongClickListener(mOnDeleteButtonOnLongClickListener)
        mFingerprintButton.setOnClickListener(mOnFingerprintClickListener)
        mCodeView = view.findViewById(R.id.code_view)
        initKeyViews(view)
        mCodeView.setListener(mCodeListener)

        if (!mUseFingerPrint) {
            mFingerprintButton.setVisibility(View.GONE)
        }

        mFingerprintHardwareDetected = isFingerprintApiAvailable(context)
        mRootView = view
        applyConfiguration(mConfiguration)
        return view
    }

    override fun onStart() {
        if (!mIsCreateMode && mUseFingerPrint && mConfiguration!!.isAutoShowFingerprint &&
            isFingerprintApiAvailable(activity) && isFingerprintsExists(activity)
        ) {
            mOnFingerprintClickListener.onClick(mFingerprintButton)
        }
        super.onStart()
    }

    fun setConfiguration(configuration: PFFLockScreenConfiguration?) {
        mConfiguration = configuration
        applyConfiguration(configuration)
    }

    private fun applyConfiguration(configuration: PFFLockScreenConfiguration?) {
        if (mRootView == null || configuration == null) {
            return
        }
        titleView = mRootView!!.findViewById(R.id.title_text_view)
        titleView.setText(configuration.title)
        if (TextUtils.isEmpty(configuration.leftButton)) {
//            mLeftButton!!.visibility = View.GONE
        } else {
//            mLeftButton!!.text = configuration.leftButton
//            mLeftButton!!.setOnClickListener(mOnLeftButtonClickListener)
        }
        if (!TextUtils.isEmpty(configuration.nextButton)) {
            mNextButton.text = configuration.nextButton
        }
        mUseFingerPrint = configuration.isUseFingerprint
        if (!mUseFingerPrint) {
            mFingerprintButton.visibility = View.GONE
            mDeleteButton.visibility = View.VISIBLE
        }
        mIsCreateMode = mConfiguration!!.mode == PFFLockScreenConfiguration.MODE_CREATE
        if (mIsCreateMode) {
//            mLeftButton!!.visibility = View.GONE
            mFingerprintButton.visibility = View.GONE
        }
        if (mIsCreateMode) {
            mNextButton.setOnClickListener(mOnNextButtonClickListener)
        } else {
            mNextButton.setOnClickListener(null)
        }
        mNextButton.visibility = View.GONE
        mCodeView.setCodeLength(mConfiguration!!.codeLength)
    }

    private fun initKeyViews(parent: View) {
        parent.findViewById<View>(R.id.button_0)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_1)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_2)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_3)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_4)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_5)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_6)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_7)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_8)
            .setOnClickListener(mOnKeyClickListener)
        parent.findViewById<View>(R.id.button_9)
            .setOnClickListener(mOnKeyClickListener)
    }

    private val mOnKeyClickListener =
        View.OnClickListener { v ->
            if (v is TextView) {
                val string = v.text.toString()
                if (string.length != 1) {
                    return@OnClickListener
                }
                val codeLength = mCodeView!!.input(string)
                configureRightButton(codeLength)
            }
        }
    private val mOnDeleteButtonClickListener =
        View.OnClickListener {
            val codeLength = mCodeView!!.delete()
            configureRightButton(codeLength)
        }
    private val mOnDeleteButtonOnLongClickListener =
        OnLongClickListener {
            mCodeView!!.clearCode()
            configureRightButton(0)
            true
        }
    private val mOnFingerprintClickListener =
        View.OnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                !isFingerprintApiAvailable(activity)
            ) {
                return@OnClickListener
            }
            if (!isFingerprintsExists(activity)) {
                showNoFingerprintDialog()
                return@OnClickListener
            }
            val fragment =
                PFFingerprintAuthDialogFragment()
            fragment.show(
                fragmentManager!!,
                FINGERPRINT_DIALOG_FRAGMENT_TAG
            )
            fragment.setAuthListener(object :
                PFFingerprintAuthListener {
                override fun onAuthenticated() {
                    if (mLoginListener != null) {
                        mLoginListener!!.onFingerprintSuccessful()
                    }
                    fragment.dismiss()
                }

                override fun onError() {
                    if (mLoginListener != null) {
                        mLoginListener!!.onFingerprintLoginFailed()
                    }
                }
            })
        }

    private fun configureRightButton(codeLength: Int) {
        if (mIsCreateMode) {
            if (codeLength > 0) {
                mDeleteButton.visibility = View.VISIBLE
            } else {
//                mDeleteButton.visibility = View.GONE
            }
            return
        }

        if (codeLength > 0) {
            mFingerprintButton.visibility = View.GONE
            mDeleteButton.visibility = View.VISIBLE
            mDeleteButton!!.isEnabled = true
            return
        }

        if (mUseFingerPrint && mFingerprintHardwareDetected) {
            mFingerprintButton!!.visibility = View.VISIBLE
//            mDeleteButton!!.visibility = View.GONE
        } else {
            mFingerprintButton!!.visibility = View.GONE
            mDeleteButton!!.visibility = View.VISIBLE
        }
        mDeleteButton!!.isEnabled = false
    }

    private fun isFingerprintApiAvailable(context: Context?): Boolean {
        return FingerprintManagerCompat.from(context!!).isHardwareDetected
    }

    private fun isFingerprintsExists(context: Context?): Boolean {
        return FingerprintManagerCompat.from(context!!).hasEnrolledFingerprints()
    }

    private fun showNoFingerprintDialog() {
        AlertDialog.Builder(context)
            .setTitle(R.string.no_fingerprints_title_pf)
            .setMessage(R.string.no_fingerprints_message_pf)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel_pf, null)
            .setPositiveButton(R.string.settings_pf) { dialog, which ->
                startActivity(
                    Intent(Settings.ACTION_SECURITY_SETTINGS)
                )
            }.create().show()
    }

    private val mCodeListener: OnPFCodeListener = object : OnPFCodeListener {
        override fun onCodeCompleted(code: String) {
            if (mIsCreateMode) {
                mNextButton!!.visibility = View.VISIBLE
                mCode = code
                return
            }
            mCode = code

            val isCorrect = pinPreferences.checkPin(context!!, mCode)
            if (mLoginListener != null) {
                if (isCorrect) {
                    mLoginListener!!.onCodeInputSuccessful()
                } else {
                    // clean
                    mLoginListener!!.onPinLoginFailed()
                    mCodeView.clearCode()
                    errorAction()
                }
            }
            if (!isCorrect && mConfiguration!!.isClearCodeOnError) {
                mCodeView!!.clearCode()
            }
        }

        override fun onCodeNotCompleted(code: String) {
            if (mIsCreateMode) {
                mNextButton!!.visibility = View.INVISIBLE
                return
            }
        }
    }
    private val mOnNextButtonClickListener =
        View.OnClickListener {
            if (mConfiguration!!.isNewCodeValidation && TextUtils.isEmpty(
                    mCodeValidation
                )
            ) {
                mCodeValidation = mCode
                cleanCode()
                titleView!!.text = mConfiguration!!.newCodeValidationTitle
                return@OnClickListener
            }
            if (mConfiguration!!.isNewCodeValidation && !TextUtils.isEmpty(
                    mCodeValidation
                ) && mCode != mCodeValidation
            ) {
                mCodeCreateListener!!.onNewCodeValidationFailed()
                titleView!!.text = mConfiguration!!.title
                cleanCode()
                return@OnClickListener
            }
            mCodeValidation = ""
            pinPreferences.savePin(context!!, mCode)
            mCodeCreateListener!!.onCodeCreated(mCode)
        }

    private fun cleanCode() {
        mCode = ""
        mCodeView!!.clearCode()
    }

    private fun deleteEncodeKey() {
        pinPreferences.delete(context!!)
    }

    private fun errorAction() {
        if (mConfiguration!!.isErrorVibration) {
            val v =
                context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v?.vibrate(400)
        }
        if (mConfiguration!!.isErrorAnimation) {
            val animShake =
                AnimationUtils.loadAnimation(context, R.anim.shake_pf)
            mCodeView!!.startAnimation(animShake)
        }
    }

    fun setOnLeftButtonClickListener(onLeftButtonClickListener: View.OnClickListener) {
        mOnLeftButtonClickListener = onLeftButtonClickListener
    }
    /*private void showFingerprintAlertDialog(Context context) {
        new AlertDialog.Builder(context).setTitle("Fingerprint").setMessage(
                "Would you like to use fingerprint for future login?")
                .setPositiveButton("Use fingerprint", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if (isFingerprintsExists(getContext())) {
                    //PFFingerprintPinCodeHelper.getInstance().encodePin()
                //}
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }*/
    /**
     * Set OnPFLockScreenCodeCreateListener.
     *
     * @param listener OnPFLockScreenCodeCreateListener object.
     */
    fun setCodeCreateListener(listener: OnPFLockScreenCodeCreateListener?) {
        mCodeCreateListener = listener
    }

    /**
     * Set OnPFLockScreenLoginListener.
     *
     * @param listener OnPFLockScreenLoginListener object.
     */
    fun setLoginListener(listener: OnPFLockScreenLoginListener?) {
        mLoginListener = listener
    }

    /**
     * Set Encoded pin code.
     *
     * @param encodedPinCode encoded pin code string, that was created before.
     */
    fun setEncodedPinCode(encodedPinCode: String) {
        mEncodedPinCode = encodedPinCode
    }

    /**
     * Pin Code create callback interface.
     */
    interface OnPFLockScreenCodeCreateListener {
        /**
         * Callback method for pin code creation.
         *
         * @param encodedCode encoded pin code string.
         */
        fun onCodeCreated(encodedCode: String)

        /**
         * This will be called if PFFLockScreenConfiguration#newCodeValidation is true.
         * User need to input new code twice. This method will be called when second code isn't
         * the same as first.
         */
        fun onNewCodeValidationFailed()
    }

    /**
     * Login callback interface.
     */
    interface OnPFLockScreenLoginListener {
        /**
         * Callback method for successful login attempt with pin code.
         */
        fun onCodeInputSuccessful()

        /**
         * Callback method for successful login attempt with fingerprint.
         */
        fun onFingerprintSuccessful()

        /**
         * Callback method for unsuccessful login attempt with pin code.
         */
        fun onPinLoginFailed()

        /**
         * Callback method for unsuccessful login attempt with fingerprint.
         */
        fun onFingerprintLoginFailed()
    }

    companion object {
        private val TAG = PFLockScreenFragment::class.java.name
        private const val FINGERPRINT_DIALOG_FRAGMENT_TAG = "FingerprintDialogFragment"
        private const val INSTANCE_STATE_CONFIG =
            "com.beautycoder.pflockscreen.instance_state_config"
    }
}