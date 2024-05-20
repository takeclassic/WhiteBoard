package com.thinkers.whiteboard.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.domain.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val memoRepository: MemoRepository) : ViewModel() {
    fun putSwitchStatus(fileName: String, key: String, value: Boolean, job: (() -> Unit)? = null) {
        viewModelScope.launch {
            memoRepository.writeBooleanPreference(
                fileName,
                key,
                value
            )
            job?.invoke()
        }
    }

    suspend fun getSwtichStatus(fileName: String, key: String): Boolean =
        withContext(Dispatchers.IO) {
            memoRepository.readBooleanPreference(
                fileName,
                key,
                false
            )
        }
    companion object {
        const val TAG = "SettingsViewModel"
    }
}
