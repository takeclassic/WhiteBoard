package com.thinkers.whiteboard.settings

import android.content.Context
import android.os.Build
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.utils.CryptoHelper
import com.thinkers.whiteboard.common.utils.DataStoreKeys
import com.thinkers.whiteboard.database.entities.Setting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec


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

    fun decryptPasscode() {
        viewModelScope.launch {
            val decrypted = CryptoHelper.decryptPassCodeAesGcm()
            Log.i(TAG, "decrypted: $decrypted")
        }
    }

    companion object {
        const val TAG = "LockViewModel"
    }
}
