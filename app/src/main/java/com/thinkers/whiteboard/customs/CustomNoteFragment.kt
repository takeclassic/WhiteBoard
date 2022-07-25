package com.thinkers.whiteboard.customs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.WhiteBoardApplication

import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentCustomNoteBinding
import com.thinkers.whiteboard.favorites.FavoritesFragmentDirections


class CustomNoteFragment : Fragment() {

    private var _binding: FragmentCustomNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomNoteViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            CustomNoteViewModelFactory(WhiteBoardApplication.instance!!.noteRepository)
        ).get(CustomNoteViewModel::class.java)

        _binding = FragmentCustomNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewAdaper = MemoListAdapter { memo -> adapterOnClick(memo) }
        binding.customsRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewModel.allCustomNotes("").observe(viewLifecycleOwner) {
            recyclerViewAdaper.submitList(it?.memos)
        }
    }

    private fun adapterOnClick(memo: Memo) {
        //(requireActivity() as MainActivity).setMemoId(memo.memoId)
        val action = FavoritesFragmentDirections.actionNavFavoritesToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}