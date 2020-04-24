package com.ruby.driveencrypt.utils

import android.view.View

fun animateAlpha(view: View, value: Float, endAction: Runnable) {
    view
        .animate()
        .alpha(value)
        .setDuration(200)
        .withEndAction(endAction)
        .start()
}

fun animateScale(
    view: View,
    scale: Float,
    endAction: (() -> Unit)? = null
) {
    view
        .animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(200)
        .withEndAction {
            view.scaleX = scale
            view.scaleY = scale
            endAction?.invoke()
        }
        .start()
}
