package com.ruby.driveencrypt.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View


fun View.displayMetrics(): DisplayMetrics = context.displayMetrics()

fun Context.displayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (this as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun View.window() = (context as Activity).window
