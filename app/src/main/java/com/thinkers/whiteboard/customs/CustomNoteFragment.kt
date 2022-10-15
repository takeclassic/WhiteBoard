package com.thinkers.whiteboard.customs

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication

import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.common.actionmode.ActionModeHandler
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentCustomNoteBinding
import com.thinkers.whiteboard.total.TotalFragment
import com.thinkers.whiteboard.total.TotalFragmentDirections
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class CustomNoteFragment : Fragment() {

    private var _binding: FragmentCustomNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomNoteViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var memoCount: Int = 0
    private var currentPage: Int = 1
    private var noteName: String = ""

    private var actionMode: ActionMode? = null
    private var actionModeSetMemoList = mutableListOf<Memo>()
    private var actionModeSetViewList = mutableListOf<View>()
    private var job: Job? = null
    private val mutex = Mutex()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            CustomNoteViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(CustomNoteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        viewModel.noteName = noteName
        Log.i(TAG, "noteName: $noteName")
        recyclerView = binding.customsRecyclerview.recyclerView
        recyclerView.addOnScrollListener(onScrollListener)
        binding.customSwipeLayout.setOnRefreshListener(onSwipeRefresh)

        recyclerViewAdaper = MemoListAdapter(memoItemOnClick, memoItemLongClick, onMemoItemBind)
        binding.customsRecyclerview.recyclerView.adapter = recyclerViewAdaper
        currentPage = 1
        viewModel.initKeepUpdated()
        if(viewModel.memoList.isNullOrEmpty()) {
            viewModel.getNextPage(0, noteName)
        }

        viewModel.memoListLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "list: ${it.size}")
            recyclerViewAdaper.submitList(it.toList())
        }

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

    private val memoItemOnClick: (View, Memo) -> Unit = { view, memo ->
        when(actionMode) {
            null -> {
                val action = CustomNoteFragmentDirections.actionNavCustomNoteToNavMemo(memo.memoId)
                this.findNavController().navigate(action)
            }
            else -> {
                view.background =
                    requireContext().getDrawable(R.drawable.colored_rounder_corner_view)

                if (actionModeSetMemoList.contains(memo)) {
                    actionModeSetMemoList.remove(memo)
                    actionModeSetViewList.remove(view)
                    view.background =
                        requireContext().getDrawable(R.drawable.rounder_corner_view)
                } else {
                    actionModeSetMemoList.add(memo)
                    actionModeSetViewList.add(view)
                    view.background =
                        requireContext().getDrawable(R.drawable.colored_rounder_corner_view)
                }

                if (actionModeSetMemoList.size > 0) {
                    actionMode?.setTitle(
                        Html.fromHtml(
                        "<font color='#f5fffa'>${actionModeSetMemoList.size} </font>",
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
                binding.customToolBar.noteToolbarCollapsingLayout.visibility = View.GONE
                view.background =
                    requireContext().getDrawable(R.drawable.colored_rounder_corner_view)

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
                    "<font color='#f5fffa'>${actionModeSetMemoList.size} </font>",
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS
                )
                true
            }
            else -> {
                false
            }
        }
    }

    private val onMemoItemBind: (View, Memo) -> Unit = { view, memo ->
        if (actionModeSetMemoList.contains(memo)) {
            Log.i(TAG, "onMemoItemBind:${memo.text}")
            view.background = requireContext().getDrawable(R.drawable.colored_rounder_corner_view)
        } else {
            view.background = requireContext().getDrawable(R.drawable.rounder_corner_view)
        }
    }

    private val onDestroyActionMode: () -> Unit = {
        for (actionModeSetView in actionModeSetViewList) {
            actionModeSetView.background =
                requireContext().getDrawable(R.drawable.rounder_corner_view)
        }
        actionModeSetMemoList = mutableListOf()
        actionModeSetViewList = mutableListOf()
        actionMode?.finish()
        actionMode = null
        binding.customToolBar.noteToolbarCollapsingLayout.visibility = View.VISIBLE
    }

    private val onActionModeMove: () -> Boolean = {
        onDestroyActionMode()
        val action = CustomNoteFragmentDirections.actionNavCustomNoteToNavEditNote(
            true,
            actionModeSetMemoList.toTypedArray()
        )
        findNavController().navigate(action)
        true
    }

    private val onActionModeRemove: () -> Unit = {
        showMemoRemoveAlertDialog()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customNoteTextView.visibility = View.VISIBLE
        binding.customsRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    companion object {
        const val TAG = "CustomNoteFragment"
        const val PAGE_SIZE = 30
    }
}