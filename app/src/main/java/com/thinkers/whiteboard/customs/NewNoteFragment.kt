package com.thinkers.whiteboard.customs

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.databinding.FragmentCustomNoteBinding
import com.thinkers.whiteboard.databinding.FragmentNewNoteBinding
import kotlinx.coroutines.launch

class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

   private val radioGroup1Listener: RadioGroup.OnCheckedChangeListener =
       RadioGroup.OnCheckedChangeListener { group, checkedId ->
           val selected = requireActivity().findViewById<RadioButton>(checkedId)
           noteColor = selected.buttonTintList!!.defaultColor

           with(binding) {
               newNoteRadioGroup2.setOnCheckedChangeListener(null)
               newNoteRadioGroup3.setOnCheckedChangeListener(null)

               newNoteRadioGroup2.clearCheck()
               newNoteRadioGroup3.clearCheck()

               newNoteRadioGroup2.setOnCheckedChangeListener(radioGroup2Listener)
               newNoteRadioGroup3.setOnCheckedChangeListener(radioGroup3Listener)
           }
       }

    private val radioGroup2Listener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val selected = requireActivity().findViewById<RadioButton>(checkedId)
            noteColor = selected.buttonTintList!!.defaultColor
            with(binding) {
                newNoteRadioGroup1.setOnCheckedChangeListener(null)
                newNoteRadioGroup3.setOnCheckedChangeListener(null)

                newNoteRadioGroup1.clearCheck()
                newNoteRadioGroup3.clearCheck()

                newNoteRadioGroup1.setOnCheckedChangeListener(radioGroup1Listener)
                newNoteRadioGroup3.setOnCheckedChangeListener(radioGroup3Listener)
            }
        }

    private val radioGroup3Listener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val selected = requireActivity().findViewById<RadioButton>(checkedId)
            noteColor = selected.buttonTintList!!.defaultColor
            with(binding) {
                newNoteRadioGroup1.setOnCheckedChangeListener(null)
                newNoteRadioGroup2.setOnCheckedChangeListener(null)

                newNoteRadioGroup1.clearCheck()
                newNoteRadioGroup2.clearCheck()

                newNoteRadioGroup1.setOnCheckedChangeListener(radioGroup1Listener)
                newNoteRadioGroup2.setOnCheckedChangeListener(radioGroup2Listener)
            }
        }

    private lateinit var viewModel: NewNoteViewModel
    private lateinit var note: Note
    private var noteColor: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            NewNoteViewModelFactory(WhiteBoardApplication.instance!!.noteRepository)
        ).get(NewNoteViewModel::class.java)

        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.newNoteSaveButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val noteName = binding.newNoteNoteName.text.toString()
                note = Note(noteName, System.currentTimeMillis())
                val res = viewModel.saveNote(note)
                if (res == -1L) {
                    Toast.makeText(
                        requireContext(),
                        "이미 같은 이름의 노트가 존재합니다",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
    }

}