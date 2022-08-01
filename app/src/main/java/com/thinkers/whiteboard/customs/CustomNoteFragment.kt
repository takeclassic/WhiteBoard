package com.thinkers.whiteboard.customs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        val noteName = requireArguments().get("noteName") as String
        if (noteName.isNullOrBlank()) {
            Toast.makeText(requireContext(), "노트 이름이 명확하지 않습니다", Toast.LENGTH_SHORT).show()
            return
        }
        Log.i(TAG, "noteName: $noteName")

        recyclerViewAdaper = MemoListAdapter { memo -> adapterOnClick(memo) }
        binding.customsRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewModel.allCustomNotes(noteName).observe(viewLifecycleOwner) {
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

    companion object {
        val TAG = "CustomNoteFragment"
    }
}