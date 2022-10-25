package com.thinkers.whiteboard.common.memo

import androidx.lifecycle.*
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoViewModel(private val memoRepository: MemoRepository) : ViewModel() {
    fun setHasUpdate(updatedMemo: Memo, state: MemoUpdateState) {
        memoRepository.getDataUpdated(updatedMemo, state)
    }

    fun getMemo(id: Int): LiveData<Memo> {
        return memoRepository.getMemoById(id).asLiveData()
    }

    fun saveMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.saveMemo(memo)
        }
    }

    fun updateMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.updateMemo(memo)
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.deleteMemo(memo)
        }
    }

    fun getMemoBelongNoteName(): String {
        return memoRepository.noteName
    }
}

class MemoViewModelFactory(private val memoRepository: MemoRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
