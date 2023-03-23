package com.thinkers.whiteboard.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val memoRepository: MemoRepository) : ViewModel() {
    fun putAutoRemoveSwitchStatus(activity: Activity, value: Boolean) {
        viewModelScope.launch {
            memoRepository.writeBooleanPreference(
                activity.getString(R.string.file_shared_preference_auto_remove),
                activity.getString(R.string.key_auto_remove),
                value
            )
        }
    }

    suspend fun getAutoRemoveSwtichStatus(activity: Activity): Boolean =
        withContext(viewModelScope.coroutineContext) {
            memoRepository.readBooleanPreference(
                activity.getString(R.string.file_shared_preference_auto_remove),
                activity.getString(R.string.key_auto_remove),
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
