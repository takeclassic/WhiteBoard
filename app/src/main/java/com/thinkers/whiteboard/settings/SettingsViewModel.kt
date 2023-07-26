package com.thinkers.whiteboard.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val memoRepository: MemoRepository) : ViewModel() {
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

class SettingsViewModelFactory(private val memoRepository: MemoRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
