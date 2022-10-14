package com.thinkers.whiteboard.favorites

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.thinkers.whiteboard.customs.CustomNoteFragment
import com.thinkers.whiteboard.customs.CustomNoteFragmentDirections
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentFavoritesBinding
import com.thinkers.whiteboard.total.TotalFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter
    private lateinit var recyclerView: RecyclerView

    private var favoritesMemoCount: Int = 0
    private var currentPage: Int = 1

    private var actionMode: ActionMode? = null
    private lateinit var actionModeSetMemoList: MutableList<Memo>
    private lateinit var actionModeSetViewList: MutableList<View>

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
                FavoritesViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
            ).get(FavoritesViewModel::class.java)

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.favoritesRecyclerview.recyclerView
        binding.favoritesSwipeLayout.setOnRefreshListener(onSwipeRefresh)
        recyclerView.addOnScrollListener(onScrollListener)

        recyclerViewAdaper = MemoListAdapter(memoItemOnClick, memoItemLongClick, onMemoItemBind)
        binding.favoritesRecyclerview.recyclerView.adapter = recyclerViewAdaper
        viewModel.initKeepUpdated()
        if (viewModel.memoList.isNullOrEmpty()) {
            viewModel.getNextPage(0)
        }
        currentPage = 1

        viewModel.memoListLiveData.observe(viewLifecycleOwner) {
            Log.i(TAG, "list: ${it.size}")
            recyclerViewAdaper.submitList(it.toList())
        }

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

    private val memoItemOnClick: (View, Memo) -> Unit = { view, memo ->
        when(actionMode) {
            null -> {
                val action = FavoritesFragmentDirections.actionNavFavoritesToMemoFragment(memo.memoId)
                this.findNavController().navigate(action)
            }
            else -> {
                view.background =
                    requireContext().getDrawable(R.drawable.colored_rounder_corner_view)

                if (actionModeSetMemoList.contains(memo)) {
                    actionModeSetMemoList.remove(memo)
                    actionModeSetViewList.remove(view)
                    view.isSelected = false
                    view.background =
                        requireContext().getDrawable(R.drawable.rounder_corner_view)
                } else {
                    actionModeSetMemoList.add(memo)
                    actionModeSetViewList.add(view)
                    view.isSelected = true
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
                //binding.totalNoteTitle.visibility = View.GONE
                view.isSelected = true
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

    private val onMemoItemBind:(View, Memo) -> Unit = { view, memo ->
    }

    private val onDestroyActionMode: () -> Unit = {
        for (actionModeSetView in actionModeSetViewList) {
            Log.i(CustomNoteFragment.TAG, "actionModeSetView isSelected: ${actionModeSetView.isSelected}")
            actionModeSetView.isSelected = false
            actionModeSetView.background =
                requireContext().getDrawable(R.drawable.rounder_corner_view)
        }
        actionMode?.finish()
        actionMode = null
        //binding.customNoteTextView.visibility = View.VISIBLE
    }

    private val onActionModeMove: () -> Boolean = {
        onDestroyActionMode()
        val action = FavoritesFragmentDirections.actionNavFavoritesToNavEditNote(
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
        binding.favoritesNoteTextView.visibility = View.VISIBLE
        binding.favoritesRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    companion object {
        val TAG = "FavoritesFragment"
        val PAGE_SIZE = 30
    }
}
