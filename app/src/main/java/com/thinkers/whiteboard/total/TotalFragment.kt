package com.thinkers.whiteboard.total

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
import com.thinkers.whiteboard.databinding.FragmentTotalBinding
import com.thinkers.whiteboard.favorites.FavoritesFragmentDirections
import com.thinkers.whiteboard.favorites.FavoritesViewModel
import com.thinkers.whiteboard.favorites.FavoritesViewModelFactory

class TotalFragment : Fragment() {

    private var _binding: FragmentTotalBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TotalViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            TotalViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(TotalViewModel::class.java)
        _binding = FragmentTotalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewAdaper = MemoListAdapter { memo -> adapterOnClick(memo) }
        binding.totalRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewModel.allMemos.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.totalNoteTextView.visibility = View.GONE
                binding.totalRecyclerview.recyclerView.visibility = View.VISIBLE
            } else {
                binding.totalNoteTextView.visibility = View.VISIBLE
                binding.totalRecyclerview.recyclerView.visibility = View.GONE
            }
            recyclerViewAdaper.submitList(it)
        }
    }

    private fun adapterOnClick(memo: Memo) {
        val action = TotalFragmentDirections.actionNavTotalToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.totalNoteTextView.visibility = View.VISIBLE
        binding.totalRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }
}