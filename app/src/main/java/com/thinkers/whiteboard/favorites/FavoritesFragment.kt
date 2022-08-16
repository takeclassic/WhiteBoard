package com.thinkers.whiteboard.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.MainActivity
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.MemoListAdapter
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var recyclerViewAdaper: MemoListAdapter

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
        recyclerViewAdaper = MemoListAdapter { memo -> adapterOnClick(memo) }
        binding.favoritesRecyclerview.recyclerView.adapter = recyclerViewAdaper

        viewModel.allFavorites.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.favoritesNoteTextView.visibility = View.GONE
                binding.favoritesRecyclerview.recyclerView.visibility = View.VISIBLE
            } else {
                binding.favoritesNoteTextView.visibility = View.VISIBLE
                binding.favoritesRecyclerview.recyclerView.visibility = View.GONE
            }
            recyclerViewAdaper.submitList(it)
        }
    }

    private fun adapterOnClick(memo: Memo) {
        //(requireActivity() as MainActivity).setMemoId(memo.memoId)
        val action = FavoritesFragmentDirections.actionNavFavoritesToMemoFragment(memo.memoId)
        this.findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.favoritesNoteTextView.visibility = View.VISIBLE
        binding.favoritesRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }
}