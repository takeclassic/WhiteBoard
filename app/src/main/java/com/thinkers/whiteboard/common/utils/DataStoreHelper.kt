package com.thinkers.whiteboard.common.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreHelper(private val dataStore: DataStore<Preferences>) {
    suspend fun storeStringValue(key: DataStoreKeys, value: String) {
        dataStore.edit {
            it[stringPreferencesKey(key.value)] = value
        }
    }

    fun getStringValue(key: DataStoreKeys): Flow<String> {
        return dataStore.data.map { p ->
            p[stringPreferencesKey(key.value)] ?: ""
        }
    }
}

enum class DataStoreKeys(val value: String) {
    STRING_KEY_IV("string_key_iv")
}
