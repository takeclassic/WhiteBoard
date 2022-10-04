package com.thinkers.whiteboard.customs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.thinkers.whiteboard.databinding.FragmentCustomNoteBinding
import com.thinkers.whiteboard.favorites.FavoritesFragment
import com.thinkers.whiteboard.total.TotalFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class CustomNoteFragment : Fragment(), PagingMemoUpdateListener {

    private var _binding: FragmentCustomNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomNoteViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var memoCount: Int = 0
    private var currentPage: Int = 1
    private var noteName: String = ""

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        binding.customSwipeLayout.isRefreshing = false
    }

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (recyclerViewAdaper.itemCount < memoCount
                && (recyclerViewAdaper.itemCount == currentPage * TotalFragment.PAGE_SIZE
                        || recyclerViewAdaper.itemCount - 1 == currentPage * TotalFragment.PAGE_SIZE)
            ) {
                viewModel.getNextPage(currentPage, noteName)
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
            CustomNoteViewModelFactory(WhiteBoardApplication.instance!!.memoRepository, this)
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
        this.noteName = noteName
        Log.i(TAG, "noteName: $noteName")
        recyclerView = binding.customsRecyclerview.recyclerView
        recyclerView.addOnScrollListener(onScrollListener)
        binding.customSwipeLayout.setOnRefreshListener(onSwipeRefresh)

        recyclerViewAdaper = MemoListAdapter(adapterOnClick, memoItemLongClick)
        binding.customsRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewModel.initKeepUpdated()
        viewModel.getNextPage(0, noteName)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customNoteMemoCount(this, noteName).collectLatest {
                memoCount = it
                if (memoCount > 0) {
                    binding.customNoteTextView.visibility = View.GONE
                }
                Log.i(TAG, "customMemoCount: $memoCount")
            }
        }
    }

    private val adapterOnClick: (Memo) -> Unit = { memo ->
        val action = CustomNoteFragmentDirections.actionNavCustomNoteToNavMemo(memo.memoId)
        this.findNavController().navigate(action)
    }
    private val memoItemLongClick: (View, Memo) -> Boolean = { _, _ ->
        true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customNoteTextView.visibility = View.VISIBLE
        binding.customsRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    override fun onMemoListUpdated(memoList: List<Memo>) {
        Log.i(FavoritesFragment.TAG, "data: $memoList")
        recyclerViewAdaper.submitList(memoList.toList())
    }

    companion object {
        const val TAG = "CustomNoteFragment"
        const val PAGE_SIZE = 30
    }
}