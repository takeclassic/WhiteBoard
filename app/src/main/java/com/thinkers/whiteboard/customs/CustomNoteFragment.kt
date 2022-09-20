package com.thinkers.whiteboard.customs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thinkers.whiteboard.WhiteBoardApplication

import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.common.MemoPagingAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentCustomNoteBinding
import com.thinkers.whiteboard.favorites.FavoritesFragmentDirections
import com.thinkers.whiteboard.total.TotalFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class CustomNoteFragment : Fragment() {

    private var _binding: FragmentCustomNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomNoteViewModel
    private lateinit var recyclerViewAdaper: MemoPagingAdapter

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        recyclerViewAdaper.refresh()
        binding.customSwipeLayout.isRefreshing = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            CustomNoteViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(CustomNoteViewModel::class.java)

        _binding = FragmentCustomNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val noteName = requireArguments().get("noteName") as String
        if (noteName.isNullOrBlank()) {
            Toast.makeText(requireContext(), "노트 이름이 명확하지 않습니다", Toast.LENGTH_SHORT).show()
            return
        }
        Log.i(TAG, "noteName: $noteName")

        binding.customSwipeLayout.setOnRefreshListener(onSwipeRefresh)

        recyclerViewAdaper = MemoPagingAdapter { memo -> adapterOnClick(memo) }
        binding.customsRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewLifecycleOwner.lifecycleScope.launch {
            recyclerViewAdaper.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.refresh is LoadState.NotLoading) {
                    binding.customNoteTextView.isVisible = recyclerViewAdaper.itemCount < 1
                    binding.customsRecyclerview.recyclerView.isVisible = recyclerViewAdaper.itemCount >= 1
                }
            }
        }

        viewModel.allPagingCustomNotes(noteName).observe(viewLifecycleOwner) {
            recyclerViewAdaper.submitData(this.lifecycle, it)
            Log.i(TAG, "data: ${recyclerViewAdaper.snapshot()}")
        }
    }

    private fun adapterOnClick(memo: Memo) {
        val action = CustomNoteFragmentDirections.actionNavCustomNoteToNavMemo(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customNoteTextView.visibility = View.VISIBLE
        binding.customsRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    companion object {
        val TAG = "CustomNoteFragment"
    }
}