package com.thinkers.whiteboard.settings

import android.widget.ImageView
import androidx.lifecycle.ViewModel

class LockViewModel: ViewModel() {
    var passcode = StringBuilder()
    val lockNumbers = arrayListOf<ImageView>()
    var lockTinted: Array<Boolean> = Array(4) { false }

    fun initProperties() {
        passcode.clear()
        lockNumbers.clear()
        lockTinted = Array(4) { false }
    }
}