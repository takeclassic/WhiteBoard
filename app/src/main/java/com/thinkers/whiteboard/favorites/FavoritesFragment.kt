package com.thinkers.whiteboard.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thinkers.whiteboard.MainActivity
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.common.MemoPagingAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentFavoritesBinding
import com.thinkers.whiteboard.total.TotalFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var recyclerViewAdaper: MemoPagingAdapter

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        recyclerViewAdaper.refresh()
        binding.favoritesSwipeLayout.isRefreshing = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
                this,
                FavoritesViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
            ).get(FavoritesViewModel::class.java)

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.favoritesSwipeLayout.setOnRefreshListener(onSwipeRefresh)

        recyclerViewAdaper = MemoPagingAdapter { memo -> adapterOnClick(memo) }
        binding.favoritesRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewLifecycleOwner.lifecycleScope.launch {
            recyclerViewAdaper.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.refresh is LoadState.NotLoading) {
                    binding.favoritesNoteTextView.isVisible = recyclerViewAdaper.itemCount < 1
                    binding.favoritesRecyclerview.recyclerView.isVisible = recyclerViewAdaper.itemCount >= 1
                }
            }
        }

        viewModel.allPagingFavoriteNotes().observe(viewLifecycleOwner) {
            recyclerViewAdaper.submitData(this.lifecycle, it)
            Log.i(TAG, "data: ${recyclerViewAdaper.snapshot()}")
        }
    }

    private fun adapterOnClick(memo: Memo) {
        //(requireActivity() as MainActivity).setMemoId(memo.memoId)
        val action = FavoritesFragmentDirections.actionNavFavoritesToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.favoritesNoteTextView.visibility = View.VISIBLE
        binding.favoritesRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    companion object {
        val TAG = "FavoritesFragment"
    }
}
