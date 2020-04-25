package com.ruby.driveencrypt.lockscreen

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.get
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.utils.animateScale

class CodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(
            context,
            R.layout.view_code_pf_lockscreen,
            this
        )
    }

    private val animDuration = 100L

    fun addDot() {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = createDotView(inflater)
        view.scaleX = 0f
        view.scaleY = 0f
        addView(view)
        animateScale(view, 1.0f, duration = animDuration)
    }

    fun deleteDot() {
        //todo anim bug
        if (childCount > 0) {
            val lastIndex = childCount - 1
            val view = get(lastIndex)
            animateScale(
                view,
                0f,
                duration = animDuration
            ) {
                removeViewAt(lastIndex)
            }
        }
    }

    private fun createDotView(inflater: LayoutInflater) = inflater.inflate(
        R.layout.view_pf_code_checkbox,
        null
    )

    fun clearCode() {
        removeAllViews()
    }
}