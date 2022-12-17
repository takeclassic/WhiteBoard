package com.thinkers.whiteboard.search

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentSearchBinding

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
            if (p0.isNullOrEmpty()) {
                return false
            }
            Log.i(TAG, "onQueryTextChange word: $p0")
            viewModel.searchMemos(p0)
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(SearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        recyclerViewAdaper = MemoListAdapter(memoItemOnClick, memoItemLongClick, onMemoItemBind, false)
        binding.searchRecyclerview.recyclerView.adapter = recyclerViewAdaper
        binding.searchSearchText.setOnQueryTextListener(queryTextListener)
        viewModel.searchResults.observe(viewLifecycleOwner) { list ->
            Log.i(TAG, "searched list: $list")
            recyclerViewAdaper.submitList(list)
        }
    }

    private val memoItemOnClick: (View, Memo) -> Unit = { _, memo ->
        val action = SearchFragmentDirections.actionNavSearchToNavMemo(memo.memoId)
        this.findNavController().navigate(action)
    }

    private val memoItemLongClick: (View, Memo) -> Boolean = { _, _ ->
        true
    }

    private val onMemoItemBind:(View, Memo) -> Unit = { view, memo ->
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        val TAG = "SearchFragment"
    }
}
