package com.thinkers.whiteboard.search

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.common.MemoPagingAdapter
import com.thinkers.whiteboard.common.memo.MemoViewModel
import com.thinkers.whiteboard.common.memo.MemoViewModelFactory
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentMemoBinding
import com.thinkers.whiteboard.databinding.FragmentSearchBinding
import com.thinkers.whiteboard.total.TotalFragmentDirections
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter

    private val queryTextListener = object: SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(p0: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            if (p0 == null) {
                return false
            }
            Log.i(TAG, "onQueryTextChange word: $p0")
            viewModel.searchMemos(p0).observe(viewLifecycleOwner) { list ->
                Log.i(TAG, "onQueryTextChange list: $list")
                recyclerViewAdaper.submitList(list)

            }
            return true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        recyclerViewAdaper = MemoListAdapter { memo -> adapterOnClick(memo) }
        binding.searchRecyclerview.recyclerView.adapter = recyclerViewAdaper
        binding.searchSearchText.setOnQueryTextListener(queryTextListener)
    }

    private fun adapterOnClick(memo: Memo) {
        val action = SearchFragmentDirections.actionNavSearchToNavMemo(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        val TAG = "SearchFragment"
    }
}