package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.domain.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val memoRepository: MemoRepository) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Memo>>()
    val searchResults: LiveData<List<Memo>> = _searchResults

    private fun sanitizeSearchQuery(query: String): String {
        val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
        return "*${queryWithEscapedQuotes}*"
    }

    fun searchMemos(query: String) {
        viewModelScope.launch {
            _searchResults.value = memoRepository.getSearchingMemos(sanitizeSearchQuery(query))
            Log.i(TAG, "_searchResults.value : ${_searchResults.value }")
        }
    }

    companion object {
        const val TAG = "SearchViewModel"
    }
}
