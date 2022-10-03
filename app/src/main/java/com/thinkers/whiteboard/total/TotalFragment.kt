package com.thinkers.whiteboard.total

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
import com.thinkers.whiteboard.databinding.FragmentTotalBinding
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TotalFragment : Fragment(), PagingMemoUpdateListener {

    private var _binding: FragmentTotalBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TotalViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var totalMemoCount: Int = 0
    private var currentPage: Int = 1

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        binding.totalSwipeLayout.isRefreshing = false
    }
    //TODO: RecyclerView Header
    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (recyclerViewAdaper.itemCount < totalMemoCount
                && (recyclerViewAdaper.itemCount == currentPage * PAGE_SIZE
                        || recyclerViewAdaper.itemCount - 1 == currentPage * PAGE_SIZE)
            ) {
                viewModel.getNextPage(currentPage)
                currentPage++
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            TotalViewModelFactory(WhiteBoardApplication.instance!!.memoRepository, this)
        ).get(TotalViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.totalRecyclerview.recyclerView

        binding.totalSwipeLayout.setOnRefreshListener(onSwipeRefresh)
        //TODO: RecyclerView Header
        recyclerView.addOnScrollListener(onScrollListener)

        recyclerViewAdaper = MemoListAdapter { memo -> adapterOnClick(memo) }
        binding.totalRecyclerview.recyclerView.adapter = recyclerViewAdaper
        viewModel.initKeepUpdated()
        viewModel.getNextPage(0)

        viewLifecycleOwner.lifecycleScope.launch {
             viewModel.totalMemoCount(this).collectLatest {
                 totalMemoCount = it
                 if (totalMemoCount > 0) {
                     binding.totalNoteEmptyText.visibility = View.GONE
                 }
                 Log.i(TAG, "totalMemoCount: $totalMemoCount")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.totalNoteEmptyText.visibility = View.VISIBLE
        binding.totalRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    private fun adapterOnClick(memo: Memo) {
        val action = TotalFragmentDirections.actionNavTotalToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onMemoListUpdated(memoList: List<Memo>) {
        Log.i(TAG, "data: $memoList")
        recyclerViewAdaper.submitList(memoList.toList())
    }

    companion object {
        val TAG = "TotalFragment"
        const val PAGE_SIZE: Int = 30
    }
}
