package com.thinkers.whiteboard.total

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.common.MemoPagingAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentTotalBinding
import com.thinkers.whiteboard.favorites.FavoritesFragmentDirections
import com.thinkers.whiteboard.favorites.FavoritesViewModel
import com.thinkers.whiteboard.favorites.FavoritesViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TotalFragment : Fragment() {

    private var _binding: FragmentTotalBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TotalViewModel
    private lateinit var recyclerViewAdaper: MemoPagingAdapter

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        recyclerViewAdaper.refresh()
        binding.totalSwipeLayout.isRefreshing = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            TotalViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(TotalViewModel::class.java)
        _binding = FragmentTotalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.totalSwipeLayout.setOnRefreshListener(onSwipeRefresh)

        recyclerViewAdaper = MemoPagingAdapter { memo -> adapterOnClick(memo) }
        binding.totalRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewLifecycleOwner.lifecycleScope.launch {
            recyclerViewAdaper.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.refresh is LoadState.NotLoading) {
                    binding.totalNoteTextView.isVisible = recyclerViewAdaper.itemCount < 1
                    binding.totalRecyclerview.recyclerView.isVisible = recyclerViewAdaper.itemCount >= 1
                }
            }
        }

        viewModel.pagingMemos.observe(viewLifecycleOwner) {
            recyclerViewAdaper.submitData(this.lifecycle, it)
            Log.i(TAG, "data: ${recyclerViewAdaper.snapshot()}")
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerViewAdaper.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.totalNoteTextView.visibility = View.VISIBLE
        binding.totalRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    private fun adapterOnClick(memo: Memo) {
        val action = TotalFragmentDirections.actionNavTotalToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    companion object {
        val TAG = "TotalFragment"
    }
}
