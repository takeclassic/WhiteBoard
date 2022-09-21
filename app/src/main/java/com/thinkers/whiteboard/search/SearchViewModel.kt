package com.thinkers.whiteboard.search

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository

class SearchViewModel(private val memoRepository: MemoRepository) : ViewModel() {
    private fun sanitizeSearchQuery(query: String): String {
        val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
        return "*${queryWithEscapedQuotes}*"
    }

    fun searchMemos(query: String): LiveData<List<Memo>> {
        return memoRepository.getSearchingMemos(sanitizeSearchQuery(query)).asLiveData()
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
