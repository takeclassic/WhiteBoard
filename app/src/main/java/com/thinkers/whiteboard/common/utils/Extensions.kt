package com.thinkers.whiteboard.common.utils

import android.content.res.Resources
import android.view.View
import kotlin.math.roundToInt

fun View.convertDpToPixel(dp: Float): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return px.roundToInt()
}