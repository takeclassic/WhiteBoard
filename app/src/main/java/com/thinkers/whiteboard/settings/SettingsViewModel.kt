package com.thinkers.whiteboard.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel : ViewModel() {
    fun putAutoRemoveSwitchStatus(activity: Activity, value: Boolean) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            viewModelScope.launch {
                Log.i(TAG, "saving value: $value")
                putBoolean(activity.getString(R.string.key_auto_remove), value)
                apply()
            }
        }
    }

    suspend fun getAutoRemoveSwtichStatus(activity: Activity): Boolean =
        withContext(viewModelScope.coroutineContext) {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: false
            with(sharedPref as SharedPreferences) {
                val value = this.getBoolean(activity.getString(R.string.key_auto_remove), false)
                value
            }
        }

    companion object {
        const val TAG = "SettingsViewModel"
    }
}

class SettingsViewModelFactory
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
