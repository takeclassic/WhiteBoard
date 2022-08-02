package com.thinkers.whiteboard.customs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.NoteListAdapter
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.databinding.FragmentEditNoteBinding

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditNoteViewModel
    private lateinit var recyclerViewAdaper: NoteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            EditNoteViewModelFactory(WhiteBoardApplication.instance!!.noteRepository)
        ).get(EditNoteViewModel::class.java)

        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewAdaper = NoteListAdapter(this::onDelete, this::onEdit)
        binding.editNoteRecyclerview.recyclerView.adapter = recyclerViewAdaper
        viewModel.allEditableNotes.observe(viewLifecycleOwner) {
            recyclerViewAdaper.submitList(it)
        }
    }

    private fun onDelete(note: Note) {
        viewModel.deleteNote(note)
    }

    private fun onEdit(note: Note) {
        val bundle = bundleOf("note" to note)
        findNavController().navigate(R.id.nav_add_note, bundle)
    //val result = viewModel.updateNote(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}