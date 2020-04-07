package com.ruby.driveencrypt.utils

import android.app.Activity
import android.util.DisplayMetrics
import android.view.View


fun View.displayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun View.window() = (context as Activity).window
