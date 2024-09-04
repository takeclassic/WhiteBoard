package com.thinkers.whiteboard.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
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

@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}
