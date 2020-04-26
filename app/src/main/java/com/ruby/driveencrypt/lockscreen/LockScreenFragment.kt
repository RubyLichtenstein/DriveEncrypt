package com.ruby.driveencrypt.lockscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.ruby.driveencrypt.MainActivity
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.lockscreen.LockScreenViewModel.Mode.*
import com.ruby.driveencrypt.utils.gone
import com.ruby.driveencrypt.utils.visible
import kotlinx.android.synthetic.main.fragment_lock_screen.*
import kotlinx.android.synthetic.main.fragment_lock_screen.view.*


class LockScreenFragment : Fragment() {

    companion object {
        const val ARG_CHANGE = "change"

        fun newInstance(change: Boolean) =
            LockScreenFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_CHANGE, change)
                }
            }
    }

    private lateinit var mFingerprintButton: View
    private lateinit var mDeleteButton: View

    private lateinit var mCodeView: CodeView
    private lateinit var titleView: TextView

    private val viewModel: LockScreenViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val change = it.getBoolean(ARG_CHANGE)
            viewModel.init(change)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_lock_screen, container,
            false
        )

        mFingerprintButton = view.findViewById(R.id.button_finger_print)
        mDeleteButton = view.findViewById(R.id.button_delete)
        mDeleteButton.setOnClickListener(mOnDeleteButtonClickListener)
        mDeleteButton.setOnLongClickListener(mOnDeleteButtonOnLongClickListener)
        mFingerprintButton.setOnClickListener(mOnFingerprintClickListener)
        mCodeView = view.findViewById(R.id.code_view)
        titleView = view.findViewById(R.id.title_text_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initKeyViews(view)

        with(viewModel) {
            nextButtonEnabledLiveData.observe(viewLifecycleOwner) {
                button_next.isEnabled = it
                button_next.isActivated = it
            }

            addOrRemoveDotLiveData.observe(viewLifecycleOwner) { add ->
                if (add) {
                    mCodeView.addDot()
                } else {
                    mCodeView.deleteDot()
                }
            }

            stateLiveData.observe(viewLifecycleOwner) {
                handleState(it)

                titleView.text = it.title

                if (it.code.isEmpty()) {
                    mCodeView.clearCode()
                }

                configureButtons(it.code.length)
            }

            finishLiveData.observe(viewLifecycleOwner) {
                activity?.finish()
            }

            showMainActivityLiveData.observe(viewLifecycleOwner) {
                activity?.let { showMainActivity(it) }
            }

            wrongCodeLiveData.observe(viewLifecycleOwner) {
                showWrongPinMessage()
                errorAction()
            }
        }
    }

    private fun handleState(state: LockScreenViewModel.State) {
        if (state.mode == CREATE) {
            mFingerprintButton.visibility = View.GONE
            button_next.setOnClickListener(mOnNextButtonClickListener)
            button_next.isEnabled = true
            button_next.visible()
        } else {
            button_next.setOnClickListener(null)
            button_next.gone()
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

    private val mOnKeyClickListener = { v: View ->
        if (v is TextView) {
            val string = v.text.toString()
            viewModel.onNumberClick(string)
        }
    }

    private val mOnDeleteButtonClickListener = { v: View ->
        viewModel.codeDelete()
    }

    private val mOnDeleteButtonOnLongClickListener = { v: View ->
        mCodeView.clearCode()
        viewModel.clearCode()
        true
    }

    private val mOnFingerprintClickListener = { v: View ->
//        if (!FingerprintHelper.isFingerprintsExists(requireContext())) {
//            FingerprintHelper.showNoFingerprintDialog(requireContext())
////            return
//        }
//
//        val fragment = PFFingerprintAuthDialogFragment()
//        fragment.show(
//            parentFragmentManager,
//            "FINGERPRINT_DIALOG_FRAGMENT_TAG"
//        )
//
//        fragment.setAuthListener(object :
//            PFFingerprintAuthListener {
//            override fun onAuthenticated() {
//                onFingerprintSuccessful()
//                fragment.dismiss()
//            }
//
//            override fun onError() {
//                onFingerprintLoginFailed()
//            }
//        })
    }

    private fun configureButtons(codeLength: Int) {
        if (codeLength > 0) {
            mFingerprintButton.visibility = View.GONE
            mDeleteButton.visibility = View.VISIBLE
            mDeleteButton.isEnabled = true
        } else {
            mDeleteButton.isEnabled = false
        }
    }

    fun showWrongPinMessage() {
        titleView.text = "Wrong pin, please renter."
    }

    private val mOnNextButtonClickListener = { v: View ->
        viewModel.onNextClick()
    }

    private fun errorAction() {
//        val v =
//            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        v.vibrate(400)

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
}