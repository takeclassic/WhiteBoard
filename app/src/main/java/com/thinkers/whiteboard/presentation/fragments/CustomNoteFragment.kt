package com.thinkers.whiteboard.presentation.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import com.thinkers.whiteboard.databinding.FragmentCustomNoteBinding
import com.thinkers.whiteboard.presentation.MainActivity
import com.thinkers.whiteboard.presentation.viewmodels.CustomNoteViewModel
import com.thinkers.whiteboard.presentation.views.ActionModeHandler
import com.thinkers.whiteboard.presentation.views.recyclerviews.MemoListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class CustomNoteFragment : Fragment() {
    companion object {
        const val TAG = "CustomNoteFragment"
        const val PAGE_SIZE = 30
    }

    private var _binding: FragmentCustomNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomNoteViewModel by viewModels()
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var memoCount: Int = 0
    private var currentPage: Int = 1
    private var noteName: String = ""
    private var noteNumber: Int = -1

    private var actionMode: ActionMode? = null

    private lateinit var note: Note

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
        val isMoved = (requireActivity() as MainActivity).isMoved
        if (isMoved) {
            viewModel.removeMovedItems()
        }
        (requireActivity() as MainActivity).init()
        var receivedNoteName = viewModel.getCustomNoteName()

        Log.i(TAG, "noteNameBefore: ${noteName}, noteName: $receivedNoteName, isDelete: ${viewModel.isDeletion()}")
        if (receivedNoteName.isNullOrBlank() ||
            (receivedNoteName == noteName && viewModel.isDeletion())) {
            viewModel.setDeletion(false)
            this.findNavController().navigate(R.id.nav_total)
            return
        }

        Log.i(TAG, "noteNumberBefore: $noteNumber, noteNumberAfter: ${viewModel.getChangedNoteNumber()}")
        if (noteNumber == -1 || viewModel.getChangedNoteNumber() == noteNumber) {
            noteName = receivedNoteName
            viewModel.setChangedNoteNumber(-1)
        }

//        if (!this.noteName.isNullOrBlank() &&
//            noteName != this.noteName &&
//            (viewModel.checkNameChanged()
//                    || viewModel.checkColorChanged())
//        ) {
//            viewModel.setColorChangedFlag(false)
//            viewModel.setNameChangedFlag(false)
//            if ()
//            noteName = this.noteName
//        } else {
//            this.noteName = noteName
//        }

        binding.customToolBar.noteToolbarCollapsingLayout.setExpandedTitleMargin(50, 0, 0, 60)
        binding.customToolBar.noteToolbarCollapsingLayout.title = noteName
        Log.i(TAG, "noteName: $noteName")
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getNote(noteName).collect {
                note = it
                noteNumber = note.noteNumber
                if (!isNightMode(requireContext())) {
                    binding.customNoteMainLayout.setBackgroundColor(note.noteColor)
                }
                Log.i(TAG, "note: $note")
            }
        }
        recyclerView = binding.customsRecyclerview.recyclerView
        recyclerView.addOnScrollListener(onScrollListener)

        recyclerViewAdaper = MemoListAdapter(memoItemOnClick, memoItemLongClick, onMemoItemBind, true)
        binding.customsRecyclerview.recyclerView.adapter = recyclerViewAdaper
        currentPage = 1
        viewModel.init()
        if(viewModel.memoList.isNullOrEmpty() && !isMoved) {
            viewModel.getNextPage(0, noteName)
        }

        viewModel.memoListLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "list: ${it.size}")
            recyclerViewAdaper.submitList(it.toList())
            if (viewModel.memoState == MemoUpdateState.INSERT) {
                recyclerView.post{ recyclerView.scrollToPosition(0) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customNoteMemoCount(this, noteName).collectLatest {
                Log.i(TAG, "customMemoCount: $it")
                memoCount = it
                if (memoCount > 0) {
                    binding.customNoteTextView.visibility = View.GONE
                    binding.customNestedScrollView.isNestedScrollingEnabled = true
                } else {
                    binding.customNoteTextView.visibility = View.VISIBLE
                    binding.customNestedScrollView.isNestedScrollingEnabled = false
                }
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
                view.isSelected = true

                if (viewModel.actionModeSetMemoList.contains(memo)) {
                    viewModel.actionModeSetMemoList.remove(memo)
                    viewModel.actionModeSetViewList.remove(view)
                    view.isSelected = false
                } else {
                    viewModel.actionModeSetMemoList.add(memo)
                    viewModel.actionModeSetViewList.add(view)
                    view.isSelected = true
                }

                if (viewModel.actionModeSetMemoList.size > 0) {
                    actionMode?.setTitle(
                        Html.fromHtml(
                        "<font color='#66A8FF'>${viewModel.actionModeSetMemoList.size} </font>",
                        Html.FROM_HTML_OPTION_USE_CSS_COLORS
                    ))

                    actionMode?.menu?.findItem(R.id.action_mode_share)?.isVisible = viewModel.actionModeSetMemoList.size < 2
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
                view.isSelected = true

                viewModel.actionModeSetMemoList = mutableListOf()
                viewModel.actionModeSetViewList = mutableListOf()
                viewModel.actionModeSetMemoList.add(memo)
                viewModel.actionModeSetViewList.add(view)

                actionMode = activity?.startActionMode(
                    ActionModeHandler(
                        viewModel.actionModeSetMemoList,
                        requireActivity(),
                        onActionModeRemove,
                        onActionModeMove,
                        onDestroyActionMode
                    )
                )
                actionMode?.title = Html.fromHtml(
                    "<font color='#66A8FF'>${viewModel.actionModeSetMemoList.size} </font>",
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
        if (viewModel.actionModeSetMemoList.contains(memo)) {
            Log.i(TAG, "onMemoItemBind:${memo.text}")
            view.isSelected = true
        } else {
            view.isSelected = false
        }
    }

    private val onDestroyActionMode: () -> Unit = {
        for (actionModeSetView in viewModel.actionModeSetViewList) {
            actionModeSetView.isSelected = false
        }
        viewModel.clearActionModeList()
        actionMode?.finish()
        actionMode = null
        binding.customToolBar.noteToolbarCollapsingLayout.visibility = View.VISIBLE
    }

    private val onActionModeMove: () -> Boolean = {
        val action = CustomNoteFragmentDirections.actionNavCustomNoteToNavEditNote(
            true,
            viewModel.actionModeSetMemoList.toTypedArray(),
            noteName
        )
        onDestroyActionMode()
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
                        viewModel.removeItems(viewModel.actionModeSetMemoList)
                        onDestroyActionMode()
                    })
                setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            }
            builder.create().show()
        }
    }

    private fun isNightMode(context: Context): Boolean {
        val nightModeFlags: Int =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customNoteTextView.visibility = View.VISIBLE
        binding.customsRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }
}
