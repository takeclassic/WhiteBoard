package com.thinkers.whiteboard.presentation.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thinkers.whiteboard.presentation.MainActivity
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.presentation.views.ActionModeHandler
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import com.thinkers.whiteboard.presentation.views.recyclerviews.MemoListAdapter
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentTotalBinding
import com.thinkers.whiteboard.presentation.viewmodels.TotalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
@AndroidEntryPoint
class TotalFragment : Fragment() {
    companion object {
        val TAG = "TotalFragment"
        const val PAGE_SIZE: Int = 30
    }

    private var _binding: FragmentTotalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TotalViewModel by viewModels()
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var totalMemoCount: Int = 0
    private var currentPage: Int = 1

    private var actionMode: ActionMode? = null
    private var actionModeSetMemoList = mutableListOf<Memo>()
    private var actionModeSetViewList = mutableListOf<View>()

    private val onSwipeRefresh = SwipeRefreshLayout.OnRefreshListener {
        binding.totalSwipeLayout.isRefreshing = false
    }

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            Log.i(TAG, "recyclerViewAdaper.itemCount: ${recyclerViewAdaper.itemCount}, currentPage: $currentPage, target: ${currentPage * PAGE_SIZE}")
            if (recyclerViewAdaper.itemCount < totalMemoCount
                && (recyclerViewAdaper.itemCount == currentPage * PAGE_SIZE
                        || recyclerViewAdaper.itemCount - 1 == currentPage * PAGE_SIZE)
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
        _binding = FragmentTotalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).init()
        binding.totalToolBar.noteToolbarCollapsingLayout.setExpandedTitleMargin(50, 0, 0, 60)
        binding.totalToolBar.noteToolbarCollapsingLayout.title = "전체메모"

        recyclerView = binding.totalRecyclerview.recyclerView

        binding.totalSwipeLayout.setOnRefreshListener(onSwipeRefresh)
        recyclerView.addOnScrollListener(onScrollListener)

        recyclerViewAdaper = MemoListAdapter(memoItemOnClick, memoItemLongClick, onMemoItemBind, false)
        binding.totalRecyclerview.recyclerView.adapter = recyclerViewAdaper
        viewModel.init()
        if (viewModel.memoList.isNullOrEmpty()) {
            viewModel.getNextPage(0)
        }
        currentPage = 1

        viewModel.memoListLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "list: ${it.size}, ${viewModel.memoState}")
            recyclerViewAdaper.submitList(it.toList())
            if (viewModel.memoState == MemoUpdateState.INSERT) {
                MainScope().launch { recyclerView.scrollToPosition(0) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalMemoCount(this).collectLatest {
                    totalMemoCount = it
                    if (totalMemoCount > 0) {
                        binding.totalNoteEmptyText.visibility = View.GONE
                    } else {
                        binding.totalNoteEmptyText.visibility = View.VISIBLE
                    }
                    Log.i(TAG, "totalMemoCount: $totalMemoCount")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.totalNoteEmptyText.visibility = View.VISIBLE
        binding.totalRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    private val onDestroyActionMode: () -> Unit = {
        for (actionModeSetView in actionModeSetViewList) {
            actionModeSetView.isSelected = false
        }
        actionModeSetMemoList = mutableListOf()
        actionModeSetViewList = mutableListOf()
        actionMode?.finish()
        actionMode = null
        binding.totalToolBar.noteToolbarCollapsingLayout.visibility = View.VISIBLE
    }

    private val onMemoItemBind:(View, Memo) -> Unit = { view, memo ->
        if (actionModeSetMemoList.contains(memo)) {
            Log.i(TAG, "onMemoItemBind:${memo.text}")
            view.isSelected = true
        } else {
            view.isSelected = false
        }
    }

    private val onActionModeMove: () -> Boolean = {
        val action = TotalFragmentDirections.actionNavTotalToNavEditNote(
            true,
            actionModeSetMemoList.toTypedArray(),
            null
        )
        onDestroyActionMode()
        findNavController().navigate(action)
        true
    }

    private val onActionModeRemove: () -> Unit = {
        showMemoRemoveAlertDialog()
    }

    private val memoItemOnClick: (View, Memo) -> Unit = { view, memo ->
        when(actionMode) {
            null -> {
                val action = TotalFragmentDirections.actionNavTotalToMemoFragment(memo.memoId)
                this.findNavController().navigate(action)
            }
            else -> {
                if (actionModeSetMemoList.contains(memo)) {
                    actionModeSetMemoList.remove(memo)
                    actionModeSetViewList.remove(view)
                    view.isSelected = false
                } else {
                    actionModeSetMemoList.add(memo)
                    actionModeSetViewList.add(view)
                    view.isSelected = true
                }

                if (actionModeSetMemoList.size > 0) {
                    actionMode?.setTitle(Html.fromHtml(
                        "<font color='#66A8FF'>${actionModeSetMemoList.size} </font>",
                        Html.FROM_HTML_OPTION_USE_CSS_COLORS
                    ))

                    actionMode?.menu?.findItem(R.id.action_mode_share)?.isVisible = actionModeSetMemoList.size < 2
                } else {
                    actionMode?.finish()
                }
            }
        }
    }

    private val memoItemLongClick: (View, Memo) -> Boolean = { view, memo ->
        when (actionMode) {
            null -> {
                binding.totalToolBar.noteToolbarCollapsingLayout.visibility = View.GONE
                view.isSelected = true
                Log.i(TAG, "view.drawableState, ${view.drawableState}")

                actionModeSetMemoList = mutableListOf()
                actionModeSetViewList = mutableListOf()
                actionModeSetMemoList.add(memo)
                actionModeSetViewList.add(view)

                actionMode = activity?.startActionMode(
                    ActionModeHandler(
                        actionModeSetMemoList,
                        requireActivity(),
                        onActionModeRemove,
                        onActionModeMove,
                        onDestroyActionMode
                    )
                )
                actionMode?.title = Html.fromHtml(
                    "<font color='#66A8FF'>${actionModeSetMemoList.size} </font>",
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS
                )
                true
            }
            else -> {
                false
            }
        }
    }

    private fun showMemoRemoveAlertDialog() {
        requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("메모 삭제")
                setMessage("선택하신 메모들을 삭제하시겠습니까?")
                setPositiveButton("삭제",
                    DialogInterface.OnClickListener { dialog, id ->
                        viewModel.removeItems(actionModeSetMemoList)
                        onDestroyActionMode()
                    })
                setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            }
            builder.create().show()
        }
    }
}
