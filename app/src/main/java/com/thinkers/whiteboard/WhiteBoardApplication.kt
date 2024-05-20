package com.thinkers.whiteboard

import android.app.Application
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.thinkers.whiteboard.data.database.AppDatabase
import com.thinkers.whiteboard.data.database.DatabaseModule
import com.thinkers.whiteboard.utils.notifications.NotificationHelper
import com.thinkers.whiteboard.domain.MemoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "whiteboard_prefs")

@HiltAndroidApp
class WhiteBoardApplication: Application(), Configuration.Provider {
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

    init {
        instance = this
    }

    @Inject lateinit var memoRepository: MemoRepository
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        startAutoRemove()
        createKey()
    }

    fun startAutoRemove() {
        val isAutoRemoveOn = memoRepository.readBooleanPreference(
            applicationContext.getString(R.string.file_name_shared_preference),
            applicationContext.getString(R.string.key_auto_remove),
            false
        )
        Log.i(TAG, "isAutoRemoveOn: $isAutoRemoveOn")

        if (isAutoRemoveOn) {
            val autoRemoveRequest = PeriodicWorkRequestBuilder<AutoRemoveWorker>(
                4, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            ).build()

            WorkManager
                .getInstance(applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    autoRemoveRequest
                )
        } else {
            WorkManager
                .getInstance(applicationContext)
                .cancelAllWorkByTag(WORK_NAME)
        }
    }

    private fun createKey() {
        runCatching {
            val keyStore: KeyStore = KeyStore.getInstance(AndroidKeyStore)
            keyStore.load(null)
            val secretKey: SecretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
        }.onSuccess {
            Log.i(TAG, "succeed to get the key")
        }.onFailure {
            Log.i(TAG, "failed to create key: ${it.message}")
            it.printStackTrace()

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }
}
