package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.data.utils.CryptoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LockViewModel: ViewModel() {
    var passcode = StringBuilder()
    val lockNumbers = arrayListOf<ImageView>()
    var lockTinted: Array<Boolean> = Array(4) { false }

    fun initProperties() {
        passcode.clear()
        lockNumbers.clear()
        lockTinted = Array(4) { false }
    }

    fun encryptPasscode() {
        viewModelScope.launch {
            Log.i(TAG, "to encrypt: $passcode")
            CryptoHelper.encryptStringAesGcm(passcode.toString())
            initProperties()
        }
    }

    suspend fun comparePasscode(): Boolean = withContext(Dispatchers.Default) {
        val decrypted = CryptoHelper.decryptPassCodeAesGcm()
        Log.i(TAG, "decrypted: $decrypted")
        passcode.toString() == decrypted
    }

    companion object {
        const val TAG = "LockViewModel"
    }
}
