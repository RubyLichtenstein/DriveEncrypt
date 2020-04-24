package com.ruby.driveencrypt.utils

import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.hypot

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
fun createCircularReveal(
    myView: View,
    anchor: View
) {
    // previously invisible view
    // get the center for the clipping circle
    val anchorCx = anchor.x + (anchor.width / 2)
    val anchorCy = anchor.y + (anchor.height / 2)

    val cx = myView.width// / 2
    val cy = myView.height// / 2

    // get the final radius for the clipping circle
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

    // create the animator for this view (the start radius is zero)
    val anim = ViewAnimationUtils.createCircularReveal(
        myView,
        anchorCx.toInt(),
        anchorCy.toInt(),
        0f,
        finalRadius
    )
    // make the view visible and start the animation
    myView.visibility = View.VISIBLE
    anim.start()
}