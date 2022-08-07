package com.thinkers.whiteboard.customs

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.iterator
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.databinding.FragmentNewNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

   private val radioGroup1Listener: RadioGroup.OnCheckedChangeListener =
       RadioGroup.OnCheckedChangeListener { group, checkedId ->
           val selected = requireActivity().findViewById<RadioButton>(checkedId)
           Log.i(TAG, "button tint color: ${selected.buttonTintList!!.defaultColor}")
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
            Log.i(TAG, "button tint color: ${selected.buttonTintList!!.defaultColor}")
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
            Log.i(TAG, "button tint color: ${selected.buttonTintList!!.defaultColor}")
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
    private var noteColor: Int = -769226
    private var isNew = true

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
        binding.newNoteClose.setOnClickListener {
            Log.i(TAG, "clicked close button")
            requireActivity().onBackPressed()
        }

        binding.newNoteRadioGroup1.setOnCheckedChangeListener(radioGroup1Listener)
        binding.newNoteRadioGroup2.setOnCheckedChangeListener(radioGroup2Listener)
        binding.newNoteRadioGroup3.setOnCheckedChangeListener(radioGroup3Listener)

        val bundle = requireArguments()
        val args = NewNoteFragmentArgs.fromBundle(bundle)
        args.note?.let {
            binding.newNoteNoteName.text =
                Editable.Factory().newEditable(it.noteName)
            binding.newNoteSaveButton.text = "수정하기"
            noteColor = it.noteColor
            checkSavedNoteColor(it.noteColor)
            isNew = false
        }

        binding.newNoteSaveButton.setOnClickListener {
            if (binding.newNoteNoteName.text.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "노트의 이름을 지정하세요",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val noteName = binding.newNoteNoteName.text.toString()
            note = Note(noteName, System.currentTimeMillis(), noteColor)
            Log.i(TAG, "saving note info: $note")

            if (isNew) {
                val res = viewModel.saveNote(note)
                Log.i(TAG, "save result: $res")
                if (res == -1L) {
                    Toast.makeText(
                        requireContext(),
                        "이미 같은 이름의 노트가 존재합니다",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    requireActivity().onBackPressed()
                }
            } else {
                val res = viewModel.updateNote(note)
                Log.i(TAG, "update result: $res")
                if (res == 0) {
                    Toast.makeText(
                        requireContext(),
                        "수정을 원하시면 노트 이름을 변경하세요",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkSavedNoteColor(color: Int) {
        val iter = binding.newNoteRadioGroup1.iterator()
        iter.forEach {
            val button = it as RadioButton
            if (button.buttonTintList?.defaultColor == color) {
                button.isChecked = true
                Log.i(TAG, "button.buttonTintList?.defaultColor: ${button.buttonTintList?.defaultColor}, color: $color")
                return
            }
        }

        val iter2 = binding.newNoteRadioGroup2.iterator()
        iter2.forEach {
            val button = it as RadioButton
            if (button.buttonTintList?.defaultColor == color) {
                button.isChecked = true
                Log.i(TAG, "button.buttonTintList?.defaultColor: ${button.buttonTintList?.defaultColor}, color: $color")
                return
            }
        }

        val iter3 = binding.newNoteRadioGroup3.iterator()
        iter3.forEach {
            val button = it as RadioButton
            if (button.buttonTintList?.defaultColor == color) {
                button.isChecked = true
                Log.i(TAG, "button.buttonTintList?.defaultColor: ${button.buttonTintList?.defaultColor}, color: $color")
                return
            }
        }
    }

    companion object {
        val TAG = "NewNoteFragment"
    }
}
