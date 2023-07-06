package com.thinkers.whiteboard.settings

import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.utils.DataStoreKeys
import com.thinkers.whiteboard.database.entities.Setting
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey


class LockViewModel: ViewModel() {
    var passcode = StringBuilder()
    val lockNumbers = arrayListOf<ImageView>()
    var lockTinted: Array<Boolean> = Array(4) { false }

    fun initProperties() {
        passcode.clear()
        lockNumbers.clear()
        lockTinted = Array(4) { false }

        encryptPasscode()
    }

    fun encryptPasscode() {
        val keyStore: KeyStore = KeyStore.getInstance(WhiteBoardApplication.AndroidKeyStore)
        keyStore.load(null)
        val secretKey: SecretKey = keyStore.getKey(WhiteBoardApplication.KEY_NAME, null) as SecretKey
        val transformation = KeyProperties.KEY_ALGORITHM_AES
            .plus("/")
            .plus(KeyProperties.BLOCK_MODE_CBC)
            .plus("/")
            .plus(KeyProperties.ENCRYPTION_PADDING_PKCS7)

        val cipher: Cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encodedIv: String = Base64.encodeToString(iv, Base64.NO_WRAP)
        viewModelScope.launch {
            WhiteBoardApplication.instance!!.dataStoreHelper.storeStringValue(DataStoreKeys.STRING_KEY_IV, encodedIv)
        }

        val encrypted = cipher.doFinal(passcode.toString().toByteArray())
        val setting = Setting("passcode", encrypted.toString(Charsets.UTF_8))
        WhiteBoardApplication.instance!!.settingRepository.setSetting(setting)
    }

    fun decryptPasscode() {

    }
}
