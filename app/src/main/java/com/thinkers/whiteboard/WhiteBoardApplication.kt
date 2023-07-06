package com.thinkers.whiteboard

import android.app.Application
import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.thinkers.whiteboard.common.notifications.NotificationHelper
import com.thinkers.whiteboard.common.utils.DataStoreHelper
import com.thinkers.whiteboard.common.utils.DispatcherProviderUtil
import com.thinkers.whiteboard.database.AppDatabase
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import com.thinkers.whiteboard.database.repositories.SettingRepository
import com.thinkers.whiteboard.settings.AutoRemoveWorker
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class WhiteBoardApplication: Application() {
    init {
        instance = this
    }

    val database by lazy { AppDatabase.getDatabase(this) }
    val memoRepository by lazy {
        MemoRepository(
            database.memoDao(),
            DispatcherProviderUtil()
        )
    }
    val noteRepository by lazy {
        NoteRepository(
            database.noteDao(),
            DispatcherProviderUtil()
        )
    }
    val settingRepository by lazy {
        SettingRepository(
            database.settingDao(),
            DispatcherProviderUtil()
        )
    }

    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "whiteboard_prefs")
    val dataStoreHelper by lazy {
        DataStoreHelper(dataStore)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        startAutoRemove()
        createKey()
    }

    fun startAutoRemove() {
        val isAutoRemoveOn = memoRepository.readBooleanPreference(
            context().getString(R.string.file_shared_preference_auto_remove),
            context().getString(R.string.key_auto_remove),
            false
        )
        Log.i(TAG, "isAutoRemoveOn: $isAutoRemoveOn")

        if (isAutoRemoveOn) {
            val autoRemoveRequest = PeriodicWorkRequestBuilder<AutoRemoveWorker>(
                4, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            ).build()

            WorkManager
                .getInstance(context())
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    autoRemoveRequest
                )
        }
    }

    private fun createKey() {
        runCatching {
            val keyStore: KeyStore = KeyStore.getInstance(AndroidKeyStore)
            keyStore.load(null)
            val secretKey: SecretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
            Log.i(TAG, "secretKey :$secretKey")
        }.onSuccess {
            Log.i(TAG, "succeed to get the key")
        }.onFailure {
            Log.i(TAG, "failed to create key: ${it.message}")
            it.printStackTrace()

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    fun getIv() {

    }

    companion object {
        var instance: WhiteBoardApplication? = null
        fun context(): Context {
            return instance!!.applicationContext
        }

        const val TAG = "WhiteBoardApplication"
        const val WORK_NAME = "auto_remove"
        const val KEY_NAME = "whiteboard_key"
        const val AndroidKeyStore = "AndroidKeyStore"
    }
}
