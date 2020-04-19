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