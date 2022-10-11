package com.thinkers.whiteboard.search

import android.text.Editable
import android.util.Log
import androidx.lifecycle.*
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.launch

class SearchViewModel(private val memoRepository: MemoRepository) : ViewModel() {
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

class SearchViewModelFactory(
    private val memoRepository: MemoRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}
