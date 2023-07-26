package com.thinkers.whiteboard.common.utils

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.thinkers.whiteboard.WhiteBoardApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

object CryptoHelper {
    fun encryptStringAesGcm(plainString: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val keyStore: KeyStore = KeyStore.getInstance(WhiteBoardApplication.AndroidKeyStore)
            runCatching {
                keyStore.load(null)
            }.onFailure {
                Log.e("CryptoHelper", "loading keystore had problem: ${it.stackTrace}")
                return@launch
            }
            val secretKey: SecretKey =
                keyStore.getKey(WhiteBoardApplication.KEY_NAME, null) as SecretKey
            val transformation = KeyProperties.KEY_ALGORITHM_AES
                .plus("/")
                .plus(KeyProperties.BLOCK_MODE_GCM)
                .plus("/")
                .plus(KeyProperties.ENCRYPTION_PADDING_NONE)

            val cipher: Cipher = Cipher.getInstance(transformation)

            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv.copyOf()
            val aad = SecureRandom().generateSeed(CryptoConstants.gcmSeedSize)
            cipher.updateAAD(aad)

            val encodedIv: String = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encodedAad: String = Base64.encodeToString(aad, Base64.NO_WRAP)
            WhiteBoardApplication.instance!!.dataStoreHelper.storeStringValue(
                DataStoreKeys.STRING_KEY_IV,
                encodedIv
            )
            WhiteBoardApplication.instance!!.dataStoreHelper.storeStringValue(
                DataStoreKeys.STRING_KEY_AAD,
                encodedAad
            )
            var encrypted = byteArrayOf()
            runCatching {
                encrypted = cipher.doFinal(plainString.toByteArray())
            }.onSuccess {
                Log.i("CryptoHelper", "encryption was successful")
                writeToFile(encrypted)
            }.onFailure {
                Log.e("CryptoHelper", "encrypting the word had problem: ${it.stackTrace}")
            }
        }
    }

    suspend fun decryptPassCodeAesGcm(): String = withContext(Dispatchers.Default) {
            val encrypted = readFile()

            val keyStore: KeyStore = KeyStore.getInstance(WhiteBoardApplication.AndroidKeyStore)
            runCatching {
                keyStore.load(null)
            }.onFailure {
                Log.e("CryptoHelper", "loading keystore had problem: ${it.stackTrace}")
            }

            val secretKey: SecretKey = keyStore.getKey(WhiteBoardApplication.KEY_NAME, null) as SecretKey
            val transformation = KeyProperties.KEY_ALGORITHM_AES
                .plus("/")
                .plus(KeyProperties.BLOCK_MODE_GCM)
                .plus("/")
                .plus(KeyProperties.ENCRYPTION_PADDING_NONE)

            val cipher = Cipher.getInstance(transformation)
            val ivStr: String = WhiteBoardApplication.instance!!.dataStoreHelper.getStringValue(DataStoreKeys.STRING_KEY_IV).first()
            val iv = Base64.decode(ivStr, Base64.NO_WRAP)
            val ivSpec = GCMParameterSpec(CryptoConstants.gcmSpecSize, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val aadStr: String = WhiteBoardApplication.instance!!.dataStoreHelper.getStringValue(DataStoreKeys.STRING_KEY_AAD).first()
            val aad = Base64.decode(aadStr, Base64.NO_WRAP)
            cipher.updateAAD(aad)
            var decrypted = ""
            kotlin.runCatching {
                decrypted = cipher.doFinal(encrypted).toString(Charsets.UTF_8)
            }.onFailure {
                Log.e("CryptoHelper", "decryption had problem: ${it.stackTrace}")
            }
           decrypted
    }

    private fun writeToFile(encrypted: ByteArray) {
        val context = WhiteBoardApplication.context()
        val output = context.openFileOutput(CryptoConstants.fileName, Context.MODE_PRIVATE)
        val dos = DataOutputStream(output)
        dos.write(encrypted)
        dos.flush()
        dos.close()
    }

    private fun readFile(): ByteArray {
        val context = WhiteBoardApplication.context()
        val input = context.openFileInput(CryptoConstants.fileName)
        val dis = DataInputStream(input)
        val byteArray = ByteArray(CryptoConstants.byteSize)

        dis.read(byteArray)
        dis.close()

        return byteArray
    }
}

object CryptoConstants {
    val fileName = "crypt.dat"
    val byteSize = 20
    val gcmSeedSize = 16
    val gcmSpecSize = 128
}
