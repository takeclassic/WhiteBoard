package com.thinkers.whiteboard.presentation.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.presentation.MainActivity
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.presentation.views.recyclerviews.NoteListAdapter
import com.thinkers.whiteboard.presentation.views.CustomDecoration
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.databinding.FragmentEditNoteBinding
import com.thinkers.whiteboard.presentation.viewmodels.EditNoteViewModel
import com.thinkers.whiteboard.presentation.viewmodels.EditNoteViewModelFactory


class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditNoteViewModel
    private lateinit var recyclerViewAdaper: NoteListAdapter

    private var memoList: List<Memo>? = null
    private var isActionMode = false
    private var noteName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            EditNoteViewModelFactory(
                WhiteBoardApplication.instance!!.noteRepository,
                WhiteBoardApplication.instance!!.memoRepository
            )
        ).get(EditNoteViewModel::class.java)

        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.editNoteClose.setOnClickListener {
            Log.i(TAG, "clicked close button")
            requireActivity().onBackPressed()
        }
        val bundle = requireArguments()
        val args = EditNoteFragmentArgs.fromBundle(bundle)
        isActionMode = args.isActionMode
        memoList = args.memoList?.toList()
        noteName = args.noteName

        if (isActionMode) {
            handleActionMode()
        } else {
            handleEditNote()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.editNoteTextView.visibility = View.VISIBLE
        binding.editNoteRecyclerview.recyclerView.visibility = View.GONE
        _binding = null
    }

    private fun onDelete(note: Note) {
        onDeleteAlertDialog(note)
    }

    private fun onEdit(note: Note) {
        val bundle = bundleOf("note" to note)
        findNavController().navigate(R.id.nav_add_note, bundle)
    }

    private fun onMove(note: Note) {
        if (!noteName.isNullOrBlank()
            && noteName == note.noteName) {
            Toast.makeText(requireContext(), "이미 사용중인 노트입니다", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.moveMemos(note.noteName, memoList!!)
        (requireActivity() as MainActivity).isMoved = true
        requireActivity().onBackPressed()
    }

    private fun onDeleteAlertDialog(note: Note) {
        requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("노트 삭제")
                setMessage("삭제하실 경우 노트안의 모든 메모가 함께 삭제됩니다")
                setPositiveButton("삭제",
                    DialogInterface.OnClickListener { dialog, id ->
                        viewModel.setDeletion(true)
                        viewModel.deleteNote(note)
                        binding.editNoteRecyclerview.recyclerView.invalidateItemDecorations()
                    })
                setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            }
            builder.create().show()
        }
    }

    private fun drawDivider() {
        val customDecoration = CustomDecoration(1f, 30f, resources.getColor(R.color.default_icon, null))
        binding.editNoteRecyclerview.recyclerView.addItemDecoration(customDecoration)
    }

    private fun handleActionMode() {
        binding.editNoteTitle.text = "메모이동"

        setRecyclerViewAdapters()
        viewModel.allMoveableNotes.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.editNoteTextView.visibility = View.GONE
                binding.editNoteRecyclerview.recyclerView.visibility = View.VISIBLE
            } else {
                binding.editNoteTextView.visibility = View.VISIBLE
                binding.editNoteRecyclerview.recyclerView.visibility = View.GONE
            }
            recyclerViewAdaper.submitList(it)
            drawDivider()
        }
    }

    private fun handleEditNote() {
        binding.editNoteTitle.text = "노트편집"

        setRecyclerViewAdapters()
        viewModel.allEditableNotes.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.editNoteTextView.visibility = View.GONE
                binding.editNoteRecyclerview.recyclerView.visibility = View.VISIBLE
            } else {
                binding.editNoteTextView.visibility = View.VISIBLE
                binding.editNoteRecyclerview.recyclerView.visibility = View.GONE
            }
            recyclerViewAdaper.submitList(it)
            drawDivider()
        }
    }

    private fun setRecyclerViewAdapters() {
        recyclerViewAdaper = NoteListAdapter(this::onDelete, this::onEdit, this::onMove, isActionMode, noteName)
        binding.editNoteRecyclerview.recyclerView.adapter = recyclerViewAdaper
    }

    companion object {
        val TAG = "EditNoteFragment"
    }
}
