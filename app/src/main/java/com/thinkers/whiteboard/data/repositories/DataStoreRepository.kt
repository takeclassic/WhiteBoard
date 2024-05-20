package com.thinkers.whiteboard.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepository @Inject constructor(@ApplicationContext var context: Context) {
    companion object {
        var instance: DataStoreRepository? = null
    }
    init {
        instance = this
    }
    suspend fun storeStringValue(key: DataStoreKeys, value: String) {
        context.dataStore.edit {
            it[stringPreferencesKey(key.value)] = value
        }
    }

    fun getStringValue(key: DataStoreKeys): Flow<String> {
        return context.dataStore.data.map { p ->
            p[stringPreferencesKey(key.value)] ?: ""
        }
    }
}

enum class DataStoreKeys(val value: String) {
    STRING_KEY_IV("string_key_iv"), STRING_KEY_AAD("string_key_aad")
}
