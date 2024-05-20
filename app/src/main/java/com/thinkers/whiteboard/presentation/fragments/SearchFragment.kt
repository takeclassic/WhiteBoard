package com.thinkers.whiteboard.presentation.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.presentation.views.recyclerviews.MemoListAdapter
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentSearchBinding
import com.thinkers.whiteboard.presentation.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {
    companion object {
        val TAG = "SearchFragment"
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
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
}
