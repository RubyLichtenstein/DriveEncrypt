package com.ruby.driveencrypt.lockscreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.beautycoder.pflockscreen.PinPreferences
import com.beautycoder.pflockscreen.fragments.PFFingerprintAuthDialogFragment
import com.beautycoder.pflockscreen.fragments.PFFingerprintAuthListener
import com.ruby.driveencrypt.lockscreen.PFCodeView.OnPFCodeListener
import com.ruby.driveencrypt.MainActivity
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.utils.invisible
import kotlinx.android.synthetic.main.fragment_lock_screen_pf.view.*


class LockScreenFragment : Fragment() {
    private val pinPreferences = PinPreferences()

    companion object {
        fun newInstance(mode: Mode) =
            LockScreenFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("MODE", mode)
                }
            }
    }

    private lateinit var mFingerprintButton: View
    private lateinit var mDeleteButton: View

    private lateinit var mNextButton: Button
    private lateinit var mCodeView: PFCodeView
    private lateinit var titleView: TextView
    private var mUseFingerPrint = false
    private var mIsCreateMode = false

    private var mCode = ""
    private var mCodeValidation = ""

    val titleCREATE = "Create Code"
    val titleAUTH = "Unlock with your pin code or fingerprint"
    val titleCHANGE = "Please confirm your pin to continue."

    private var mRootView: View? = null

    enum class Mode {
        CREATE,
        AUTH,
        CHANGE,
        CHANGE_CREATE,
    }

    lateinit var mode: Mode

    val codeLength = 4
    val isNewCodeValidation = true
    val isAutoShowFingerprint = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mode = it.getSerializable("MODE") as Mode
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_lock_screen_pf, container,
            false
        )

        mFingerprintButton = view.findViewById(R.id.button_finger_print)
        mDeleteButton = view.findViewById(R.id.button_delete)
        mNextButton = view.findViewById(R.id.button_next)
        mDeleteButton.setOnClickListener(mOnDeleteButtonClickListener)
        mDeleteButton.setOnLongClickListener(mOnDeleteButtonOnLongClickListener)
        mFingerprintButton.setOnClickListener(mOnFingerprintClickListener)
        mCodeView = view.findViewById(R.id.code_view)
        initKeyViews(view)
        mCodeView.setListener(mCodeListener)

        if (!mUseFingerPrint) {
            mFingerprintButton.visibility = View.GONE
        }

        mRootView = view
        renderByMode(mode)
        return view
    }

    override fun onStart() {
        if (!mIsCreateMode &&
            mUseFingerPrint &&
            isAutoShowFingerprint &&
            FingerprintHelper.isFingerprintAvailable(requireContext())
        ) {
            mOnFingerprintClickListener.onClick(mFingerprintButton)
        }
        super.onStart()
    }

    private fun renderByMode(mode: Mode) {
        titleView = mRootView!!.findViewById(R.id.title_text_view)
        titleView.text = getTitleText()

        if (!mUseFingerPrint) {
            mFingerprintButton.visibility = View.GONE
            mDeleteButton.visibility = View.VISIBLE
        }

        mIsCreateMode = mode == Mode.CREATE ||
                mode == Mode.CHANGE_CREATE

        if (mIsCreateMode) {
            mFingerprintButton.visibility = View.GONE
            mNextButton.setOnClickListener(mOnNextButtonClickListener)
            mNextButton.isEnabled = true
        } else {
            mNextButton.setOnClickListener(null)
            mNextButton.invisible()
        }

        mCodeView.setCodeLength(codeLength)
    }

    private fun getTitleText(): String {
        return when (mode) {
            Mode.CREATE -> titleCREATE
            Mode.AUTH -> titleAUTH
            Mode.CHANGE -> titleCHANGE
            Mode.CHANGE_CREATE -> titleCREATE
        }
    }

    private fun initKeyViews(parent: View) {
        listOf(
            parent.button_0,
            parent.button_1,
            parent.button_2,
            parent.button_3,
            parent.button_4,
            parent.button_5,
            parent.button_6,
            parent.button_7,
            parent.button_8,
            parent.button_9
        ).forEach {
            it.setOnClickListener(mOnKeyClickListener)
        }
    }

    private val mOnKeyClickListener =
        View.OnClickListener { v ->
            if (v is TextView) {
                val string = v.text.toString()
                if (string.length != 1) {
                    return@OnClickListener
                }
                val codeLength = mCodeView.input(string)
                configureButtons(codeLength)
            }
        }

    private val mOnDeleteButtonClickListener =
        View.OnClickListener {
            val codeLength = mCodeView.delete()
            configureButtons(codeLength)
        }

    private val mOnDeleteButtonOnLongClickListener =
        OnLongClickListener {
            mCodeView.clearCode()
            configureButtons(0)
            true
        }

    private val mOnFingerprintClickListener =
        View.OnClickListener {
            if (!FingerprintHelper.isFingerprintsExists(requireContext())) {
                FingerprintHelper.showNoFingerprintDialog(requireContext())
                return@OnClickListener
            }

            val fragment = PFFingerprintAuthDialogFragment()
            fragment.show(
                parentFragmentManager,
                "FINGERPRINT_DIALOG_FRAGMENT_TAG"
            )

            fragment.setAuthListener(object :
                PFFingerprintAuthListener {
                override fun onAuthenticated() {
                    onFingerprintSuccessful()
                    fragment.dismiss()
                }

                override fun onError() {
                    onFingerprintLoginFailed()
                }
            })
        }

    private fun onFingerprintSuccessful() {

    }

    private fun onFingerprintLoginFailed() {

    }

    private fun configureButtons(codeLength: Int) {
        if (codeLength > 0) {
            mFingerprintButton.visibility = View.GONE
            mDeleteButton.visibility = View.VISIBLE
            mDeleteButton.isEnabled = true
            return
        }

        if (mUseFingerPrint &&
            FingerprintHelper.isFingerprintAvailable(requireContext())
        ) {
            mFingerprintButton.visibility = View.VISIBLE
        } else {
            mFingerprintButton.visibility = View.GONE
            mDeleteButton.visibility = View.VISIBLE
        }

        mDeleteButton.isEnabled = false
    }

    private val mCodeListener: OnPFCodeListener = object : OnPFCodeListener {
        override fun onCodeCompleted(code: String) {
            mCode = code

            if (mIsCreateMode) {
                mNextButton.isEnabled = true
                return
            }

            val isCorrect = pinPreferences.checkPin(context!!, mCode)

            if (isCorrect) {
                onCodeInputSuccessful()
            } else {
                onPinLoginFailed()
                showWrongPinMessage()
                mCodeView.clearCode()
                errorAction()
            }
        }

        override fun onCodeNotCompleted(code: String) {
            if (mIsCreateMode) {
                mNextButton.isEnabled = false
                return
            }
        }
    }

    private fun showWrongPinMessage() {
        titleView.text = "Wrong pin, please renter."
    }

    fun onPinLoginFailed() {

    }

    data class State(
        val codeLength: Int
    )

    data class ViewState(
        val deleteEnabled: Boolean,
        val nextEnabled: Boolean
    )

    fun renderState(state: State): ViewState {
        return ViewState(true, true)
    }

    private val mOnNextButtonClickListener =
        View.OnClickListener {
            if (isNewCodeValidation &&
                TextUtils.isEmpty(mCodeValidation)
            ) {
                mCodeValidation = mCode
                cleanCode()
                titleView.text = "Please input code again"
                return@OnClickListener
            }
            if (isNewCodeValidation && !TextUtils.isEmpty(
                    mCodeValidation
                ) && mCode != mCodeValidation
            ) {
                onNewCodeValidationFailed()
                titleView.text = getTitleText()
                cleanCode()
                return@OnClickListener
            }
            mCodeValidation = ""
            pinPreferences.savePin(requireContext(), mCode)
            onCodeCreated(mCode)
        }

    private fun cleanCode() {
        mCode = ""
        mCodeView.clearCode()
    }

    private fun errorAction() {
        val v =
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(400)

//        VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)
        val animShake = AnimationUtils
            .loadAnimation(context, R.anim.shake_pf)

        val set = AnimationSet(false)
        set.addAnimation(animShake)

        mCodeView
            .startAnimation(set)
    }

    private fun showMainActivity(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    private fun onCodeCreated(code: String) {
        Toast.makeText(requireContext(), "Code created", Toast.LENGTH_SHORT).show()
        if (mode == Mode.CHANGE) {
            // mode create
            activity?.finish()
        } else {
            activity?.let { showMainActivity(it) }
        }
    }

    fun onCodeInputSuccessful() {
        when (mode) {
            Mode.CREATE -> {

            }
            Mode.AUTH -> {
                activity?.let { showMainActivity(it) }
            }
            Mode.CHANGE -> {
                renderByMode(Mode.CHANGE_CREATE)
            }
            Mode.CHANGE_CREATE -> {
                activity?.finish()
            }
        }
    }

    fun onNewCodeValidationFailed() {

    }
}