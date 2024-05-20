package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.utils.CryptoHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor() : ViewModel() {
    companion object {
        const val TAG = "LockViewModel"
    }

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
}
