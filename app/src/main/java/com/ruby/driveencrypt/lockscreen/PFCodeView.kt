package com.ruby.driveencrypt.lockscreen

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.view.get
import com.beautycoder.pflockscreen.R
import com.ruby.driveencrypt.utils.animateScale
import java.util.*

/**
 * Created by Aleksandr Nikiforov on 2018/02/07.
 */
class PFCodeView : LinearLayout {
    private var mCodeViews: MutableList<CheckBox> = ArrayList()
    var code = ""
        private set

    private var mCodeLength = DEFAULT_CODE_LENGTH
    private var mListener: OnPFCodeListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(
            context,
            R.layout.view_code_pf_lockscreen,
            this
        )
        setUpCodeViews()
    }

    fun setCodeLength(codeLength: Int) {
        mCodeLength = codeLength
        setUpCodeViews()
    }

    private fun setUpCodeViews() {
        removeAllViews()
        mCodeViews.clear()
        code = ""
        for (i in 0 until mCodeLength) {
//            addDot()
        }
        if (mListener != null) {
            mListener!!.onCodeNotCompleted("")
        }
    }

    private fun addDot() {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = dotView(inflater)
        view.scaleX = 0f
        view.scaleY = 0f
        addView(view)
        animateScale(view, 1.0f)
    }

    private fun removeDot() {
        if (childCount > 0) {
            val lastIndex = childCount - 1
            val view = get(lastIndex)
            animateScale(
                view,
                0f
            ) {
                removeViewAt(lastIndex)
            }
        }
    }

    private fun dotView(inflater: LayoutInflater): View {
        val view = inflater.inflate(
            R.layout.view_pf_code_checkbox,
            null
        )
        val layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        val margin =
            resources.getDimensionPixelSize(R.dimen.code_fp_margin)
        layoutParams.setMargins(margin, margin, margin, margin)
        view.layoutParams = layoutParams
        return view
    }

    fun input(number: String): Int {
        if (code.length == mCodeLength) {
            return code.length
        }

        addDot()

        code += number
        if (code.length == mCodeLength && mListener != null) {
            mListener!!.onCodeCompleted(code)
        }
        return code.length
    }

    fun delete(): Int {
        removeDot()
        if (mListener != null) {
            mListener!!.onCodeNotCompleted(code)
        }
        if (code.length == 0) {
            return code.length
        }
        code = code.substring(0, code.length - 1)
//        mCodeViews[code.length].toggle() //.setChecked(false);
        return code.length
    }

    fun clearCode() {
        removeAllViews()
        if (mListener != null) {
            mListener!!.onCodeNotCompleted(code)
        }
        code = ""
        for (codeView in mCodeViews) {
            codeView.isChecked = false
        }
    }

    val inputCodeLength: Int
        get() = code.length

    fun setListener(listener: OnPFCodeListener?) {
        mListener = listener
    }

    interface OnPFCodeListener {
        fun onCodeCompleted(code: String)
        fun onCodeNotCompleted(code: String)
    }

    companion object {
        private const val DEFAULT_CODE_LENGTH = 4
    }
}