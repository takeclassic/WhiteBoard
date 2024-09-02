package com.thinkers.whiteboard.utils

import android.content.res.Resources
import android.view.View
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

fun View.convertDpToPixel(dp: Float): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return px.roundToInt()
}

fun <T> Continuation<T>.safeResumeWith(value: Result<T>) {
    if (this is CancellableContinuation) {
        if (isActive)
            resumeWith(value)
    }
}

fun <T> Continuation<T>.safeResumeWithException(exception: Throwable) {
    if (this is CancellableContinuation) {
        if (isActive)
            resumeWithException(exception)
    }
}