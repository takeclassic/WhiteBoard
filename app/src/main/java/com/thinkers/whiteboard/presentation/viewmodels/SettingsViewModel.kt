package com.thinkers.whiteboard.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.data.repositories.DataStoreKeys
import com.thinkers.whiteboard.data.repositories.DataStoreRepository
import com.thinkers.whiteboard.domain.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    companion object {
        const val TAG = "SettingsViewModel"
    }

    fun putSwitchStatus(key: DataStoreKeys, value: Boolean, job: (() -> Unit)? = null) {
        viewModelScope.launch {
            DataStoreRepository.storeBooleanValue(key, value)
            job?.invoke()
        }
    }

    suspend fun getSwtichStatus(key: DataStoreKeys, defaultValue: Boolean): Boolean =
        withContext(Dispatchers.Main) {
            DataStoreRepository.getBooleanValue(key, defaultValue).first()
        }
}
