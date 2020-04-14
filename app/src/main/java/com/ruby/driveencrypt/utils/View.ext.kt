package com.ruby.driveencrypt.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup


fun View.displayMetrics(): DisplayMetrics = context.displayMetrics()

fun Context.displayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (this as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun View.window() = (context as Activity).window

fun View.setMargins(all: Int) = setMargins(all, all, all, all)
fun View.setMargins(l: Int, t: Int, r: Int, b: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p: ViewGroup.MarginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(l, t, r, b)
        requestLayout()
    }
}