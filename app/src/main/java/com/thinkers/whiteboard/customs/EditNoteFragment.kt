package com.thinkers.whiteboard.customs

import android.content.DialogInterface
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.ColorFilter
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.NoteListAdapter
import com.thinkers.whiteboard.common.memo.MemoFragmentArgs
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.databinding.FragmentEditNoteBinding


class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditNoteViewModel
    private lateinit var recyclerViewAdaper: NoteListAdapter

    private var memoList: List<Memo>? = null
    private var isActionMode = false

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
        viewModel.moveMemos(note.noteName, memoList!!)
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
                        viewModel.deleteNote(note)
                    })
                setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            }
            builder.create().show()
        }
    }

    private fun drawDivider() {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val a = requireContext().obtainStyledAttributes(attrs)
        val divider = a.getDrawable(0)
        divider?.colorFilter = BlendModeColorFilter(R.color.default_icon, BlendMode.DST)
        val insetDivider = InsetDrawable(divider, 0, 30, 0, 0)
        a.recycle()

        val decor = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        decor.setDrawable(insetDivider)
        binding.editNoteRecyclerview.recyclerView.addItemDecoration(decor)
    }

    private fun handleActionMode() {
        binding.editNoteTitle.text = "메모이동"

        recyclerViewAdaper = NoteListAdapter(this::onDelete, this::onEdit, this::onMove, isActionMode)
        binding.editNoteRecyclerview.recyclerView.adapter = recyclerViewAdaper

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
        binding.editNoteTitle.text = "메모 수정"

        recyclerViewAdaper = NoteListAdapter(this::onDelete, this::onEdit, this::onMove, isActionMode)
        binding.editNoteRecyclerview.recyclerView.adapter = recyclerViewAdaper
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

    companion object {
        val TAG = "EditNoteFragment"
    }
}
