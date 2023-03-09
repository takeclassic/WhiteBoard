package com.thinkers.whiteboard.wastebin

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.ActionMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.MainActivity
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.actionmode.ActionModeHandler
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.common.recyclerview.MemoListAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.Note

import com.thinkers.whiteboard.databinding.FragmentWasteBinBinding
import com.thinkers.whiteboard.total.TotalFragment

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WasteBinFragment : Fragment() {

    private var _binding: FragmentWasteBinBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WasteBinViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var memoCount: Int = 0
    private var currentPage: Int = 1

    private var actionMode: ActionMode? = null

    private lateinit var note: Note

    private val noteName = "waste_bin"

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (recyclerViewAdaper.itemCount < memoCount
                && (recyclerViewAdaper.itemCount == currentPage * TotalFragment.PAGE_SIZE
                        || recyclerViewAdaper.itemCount - 1 == currentPage * TotalFragment.PAGE_SIZE)
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
            WasteBinViewModelFactory(
                WhiteBoardApplication.instance!!.noteRepository,
                WhiteBoardApplication.instance!!.memoRepository
            )
        ).get(WasteBinViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWasteBinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isMoved = (requireActivity() as MainActivity).isMoved
        if (isMoved) {
            viewModel.removeMovedItems()
        }
        (requireActivity() as MainActivity).init()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getNote(noteName).collect {
                note = it
                Log.i(TAG, "note: $note")
            }
        }
        recyclerView = binding.wasteBinRecyclerview.recyclerView
        recyclerView.addOnScrollListener(onScrollListener)

        recyclerViewAdaper = MemoListAdapter(memoItemOnClick, memoItemLongClick, onMemoItemBind, false)
        binding.wasteBinRecyclerview.recyclerView.adapter = recyclerViewAdaper
        currentPage = 1
        viewModel.init()
        if(viewModel.memoList.isNullOrEmpty() && !isMoved) {
            viewModel.getNextPage(0)
        }

        viewModel.memoListLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "list: ${it.size}")
            recyclerViewAdaper.submitList(it.toList())
            if (viewModel.memoState == MemoUpdateState.INSERT) {
                recyclerView.post{ recyclerView.scrollToPosition(0) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.wasteBinMemoCount(this, noteName).collectLatest {
                Log.i(TAG, "customMemoCount: $it")
                memoCount = it
                if (memoCount > 0) {
                    binding.wasteBinNoteEmptyText.visibility = View.GONE
                    binding.wasteBinClearAllImageview.visibility = View.VISIBLE
                } else {
                    binding.wasteBinNoteEmptyText.visibility = View.VISIBLE
                    binding.wasteBinClearAllImageview.visibility = View.GONE
                }
            }
        }

        binding.wasteBinClearAllImageview.setOnClickListener {
            onActionModeRemoveAll()
        }
    }

    private val memoItemOnClick: (View, Memo) -> Unit = { view, memo ->
        when(actionMode) {
            null -> {
                val action = WasteBinFragmentDirections.actionNavWasteBinToMemoFragment(memo.memoId)
                this.findNavController().navigate(action)
            }
            else -> {
                view.background =
                    requireContext().getDrawable(R.drawable.colored_rounder_corner_view)

                if (viewModel.actionModeSetMemoList.contains(memo)) {
                    viewModel.actionModeSetMemoList.remove(memo)
                    viewModel.actionModeSetViewList.remove(view)
                    view.background =
                        requireContext().getDrawable(R.drawable.rounder_corner_view)
                } else {
                    viewModel.actionModeSetMemoList.add(memo)
                    viewModel.actionModeSetViewList.add(view)
                    view.background =
                        requireContext().getDrawable(R.drawable.colored_rounder_corner_view)
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
                toggleTileAndDeleteAllButton(false)

                view.background =
                    requireContext().getDrawable(R.drawable.colored_rounder_corner_view)

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
                        onDestroyActionMode,
                        true,
                        onActionModeRemoveAll
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
            view.background = requireContext().getDrawable(R.drawable.colored_rounder_corner_view)
        } else {
            view.background = requireContext().getDrawable(R.drawable.rounder_corner_view)
        }
    }

    private val onDestroyActionMode: () -> Unit = {
        for (actionModeSetView in viewModel.actionModeSetViewList) {
            actionModeSetView.background =
                requireContext().getDrawable(R.drawable.rounder_corner_view)
        }
        viewModel.clearActionModeList()
        actionMode?.finish()
        actionMode = null
        toggleTileAndDeleteAllButton(true)
    }

    private val onActionModeMove: () -> Boolean = {
        val action = WasteBinFragmentDirections.actionNavWasteBinToNavEditNote(
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

    private val onActionModeRemoveAll: () -> Unit = {
        showMemoRemoveAllAlertDialog()
    }

    private fun showMemoRemoveAlertDialog() {
        requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("메모 삭제")
                setMessage("선택하신 메모들을 영구 삭제하시겠습니까?")
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

    private fun showMemoRemoveAllAlertDialog() {
        requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("모든 메모 삭제")
                setMessage("모든 메모를 영구 삭제하시겠습니까?")
                setPositiveButton("삭제",
                    DialogInterface.OnClickListener { dialog, id ->
                        viewModel.removeAllItems()
                        onDestroyActionMode()
                    })
                setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            }
            builder.create().show()
        }
    }

    private fun toggleTileAndDeleteAllButton(flag: Boolean) {
        if (flag) {
            binding.wasteBinTitle.visibility = View.VISIBLE
            binding.wasteBinClearAllImageview.visibility = View.VISIBLE
        } else {
            binding.wasteBinTitle.visibility = View.GONE
            binding.wasteBinClearAllImageview.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.wasteBinNoteEmptyText.visibility = View.VISIBLE
        binding.wasteBinClearAllImageview.visibility = View.GONE
        binding.wasteBinRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    companion object {
        const val TAG = "WasteBinFragment"
        const val PAGE_SIZE = 30
    }
}