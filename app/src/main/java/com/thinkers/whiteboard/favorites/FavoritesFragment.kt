package com.thinkers.whiteboard.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.common.interfaces.PagingMemoUpdateListener
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentFavoritesBinding
import com.thinkers.whiteboard.total.TotalFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(), PagingMemoUpdateListener {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var favoritesMemoCount: Int = 0
    private var currentPage: Int = 1

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        binding.favoritesSwipeLayout.isRefreshing = false
    }

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (recyclerViewAdaper.itemCount < favoritesMemoCount
                && (recyclerViewAdaper.itemCount == currentPage * TotalFragment.PAGE_SIZE
                        || recyclerViewAdaper.itemCount - 1 == currentPage * TotalFragment.PAGE_SIZE)
            ) {
                viewModel.getNextPage(currentPage)
                currentPage++
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
                this,
                FavoritesViewModelFactory(WhiteBoardApplication.instance!!.memoRepository, this)
            ).get(FavoritesViewModel::class.java)

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.favoritesRecyclerview.recyclerView
        binding.favoritesSwipeLayout.setOnRefreshListener(onSwipeRefresh)
        recyclerView.addOnScrollListener(onScrollListener)

        recyclerViewAdaper = MemoListAdapter(adapterOnClick, memoItemLongClick)
        binding.favoritesRecyclerview.recyclerView.adapter = recyclerViewAdaper
        viewModel.initKeepUpdated()
        viewModel.getNextPage(0)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.FavoriteMemoCount(this).collectLatest {
                favoritesMemoCount = it
                if (favoritesMemoCount > 0) {
                    binding.favoritesNoteTextView.visibility = View.GONE
                }
                Log.i(TAG, "favoritesMemoCount: $favoritesMemoCount")
            }
        }
    }

    private val adapterOnClick: (View, Memo) -> Unit = { _ , memo ->
        val action = FavoritesFragmentDirections.actionNavFavoritesToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    private val memoItemLongClick: (View, Memo) -> Boolean = { _, _ ->
        true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.favoritesNoteTextView.visibility = View.VISIBLE
        binding.favoritesRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    override fun onMemoListUpdated(memoList: List<Memo>) {
        Log.i(TAG, "data: $memoList")
        recyclerViewAdaper.submitList(memoList.toList())
    }

    companion object {
        val TAG = "FavoritesFragment"
        val PAGE_SIZE = 30
    }
}
