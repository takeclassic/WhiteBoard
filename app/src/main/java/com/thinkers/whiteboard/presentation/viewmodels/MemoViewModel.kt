package com.thinkers.whiteboard.presentation.viewmodels

import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.domain.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor(private val memoRepository: MemoRepository) : ViewModel() {
    var memo: Memo? = null
    var isFavorite: Boolean = false
    var oldAlarmTime: Long? = null
    var alarmTime: Long? = null
    var beforeTextChangeEditable: Editable? = null
    var afterTextChangeEditable: Editable? = null
    var isDeletion: Boolean = false
    var memoId: Int = -1

    fun setHasUpdate(updatedMemo: Memo, state: MemoUpdateState) {
        Log.i(TAG, "memo fragment, state: $state")
        memoRepository.getDataUpdated(updatedMemo, state)
    }

    fun getMemo(id: Int): LiveData<Memo> {
        return memoRepository.getMemoById(id).asLiveData()
    }

    fun saveMemo(memo: Memo) {
        memoRepository.saveMemo(memo)
    }

    fun updateMemo(memo: Memo) {
        memoRepository.updateMemo(memo)
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.deleteMemo(memo)
        }
    }

    fun getMemoBelongNoteName(): String {
        return memoRepository.noteName
    }

    companion object {
        const val TAG = "MemoViewModel"
    }
}
