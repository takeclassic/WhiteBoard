package com.thinkers.whiteboard.data.repositories

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreRepository {
    suspend fun storeStringValue(key: DataStoreKeys, value: String) {
        WhiteBoardApplication.context().dataStore.edit {
            it[stringPreferencesKey(key.value)] = value
        }
    }

    fun getStringValue(key: DataStoreKeys): Flow<String> {
        return WhiteBoardApplication.context().dataStore.data.map { p ->
            p[stringPreferencesKey(key.value)] ?: ""
        }
    }

    suspend fun storeBooleanValue(key: DataStoreKeys, value: Boolean) {
        WhiteBoardApplication.context().dataStore.edit {
            it[booleanPreferencesKey(key.value)] = value
        }
    }

    fun getBooleanValue(key: DataStoreKeys, defaultValue: Boolean): Flow<Boolean> {
        return WhiteBoardApplication.context().dataStore.data.map { p ->
            p[booleanPreferencesKey(key.value)] ?: defaultValue
        }
    }
}

enum class DataStoreKeys(val value: String) {
    STRING_KEY_IV("string_key_iv"),
    STRING_KEY_AAD("string_key_aad"),
    BOOLEAN_KEY_LOCK_MODE("boolean_key_lock_mode"),
    BOOLEAN_KEY_AUTO_REMOVE("boolean_key_auto_remove")
}
