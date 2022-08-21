package com.thinkers.whiteboard.customs

import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
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
        binding.editNoteClose.setOnClickListener {
            Log.i(TAG, "clicked close button")
            requireActivity().onBackPressed()
        }

        recyclerViewAdaper = NoteListAdapter(this::onDelete, this::onEdit)
        binding.editNoteRecyclerview.recyclerView.adapter = recyclerViewAdaper
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.editNoteRecyclerview.recyclerView.addItemDecoration(decoration)

        viewModel.allEditableNotes.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.editNoteTextView.visibility = View.GONE
                binding.editNoteRecyclerview.recyclerView.visibility = View.VISIBLE
            } else {
                binding.editNoteTextView.visibility = View.VISIBLE
                binding.editNoteRecyclerview.recyclerView.visibility = View.GONE
            }
            recyclerViewAdaper.submitList(it)
        }
    }

    private fun onDelete(note: Note) {
        onDeleteAlertDialog(note)
    }

    private fun onEdit(note: Note) {
        val bundle = bundleOf("note" to note)
        findNavController().navigate(R.id.nav_add_note, bundle)
    }

    private fun onDeleteAlertDialog(note: Note) {
        requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("노트 삭제")
                setMessage("삭제하실 경우 노트안의 모든 메모가 함께 삭제됩니다")
                setPositiveButton("삭제",
                    DialogInterface.OnClickListener { dialog, id ->
                        viewModel.deleteNote(note)
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
        binding.editNoteTextView.visibility = View.VISIBLE
        binding.editNoteRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    companion object {
        val TAG = "EditNoteFragment"
    }
}
