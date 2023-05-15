package com.thinkers.whiteboard.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentLockBinding
import com.thinkers.whiteboard.databinding.FragmentSettingsBinding

class LockFragment : Fragment() {
    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!

    private val lockViewModel: LockViewModel by viewModels()

    private val numberButtonClickListener = View.OnClickListener {
        Log.i(TAG, "${lockViewModel.passcode}")

        if (lockViewModel.passcode.length == 4) return@OnClickListener

        when(it.id) {
            R.id.lock_button_1 -> {
                lockViewModel.passcode.append("1")
            }
            R.id.lock_button_2 -> {
                lockViewModel.passcode.append("2")
            }
            R.id.lock_button_3 -> {
                lockViewModel.passcode.append("3")
            }
            R.id.lock_button_4 -> {
                lockViewModel.passcode.append("4")
            }
            R.id.lock_button_5 -> {
                lockViewModel.passcode.append("5")
            }
            R.id.lock_button_6 -> {
                lockViewModel.passcode.append("6")
            }
            R.id.lock_button_7 -> {
                lockViewModel.passcode.append("7")
            }
            R.id.lock_button_8 -> {
                lockViewModel.passcode.append("8")
            }
            R.id.lock_button_9 -> {
                lockViewModel.passcode.append("9")
            }
            R.id.lock_button_10 -> {
                lockViewModel.passcode.append("0")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lockClose.setOnClickListener {
            requireActivity().onBackPressed()
        }
        addNumberButtonListeners(binding)

        binding.lockBackspaceButton.setOnClickListener {
            lockViewModel.passcode.apply {
                if (this.isNotEmpty()) {
                    this.deleteAt(this.lastIndex)
                    Log.i(TAG, "after remove passcode: ${lockViewModel.passcode}")
                }
            }
        }
    }

    private fun addNumberButtonListeners(binding: FragmentLockBinding) {
        binding.lockButton1.setOnClickListener(numberButtonClickListener)
        binding.lockButton2.setOnClickListener(numberButtonClickListener)
        binding.lockButton3.setOnClickListener(numberButtonClickListener)
        binding.lockButton4.setOnClickListener(numberButtonClickListener)
        binding.lockButton5.setOnClickListener(numberButtonClickListener)
        binding.lockButton6.setOnClickListener(numberButtonClickListener)
        binding.lockButton7.setOnClickListener(numberButtonClickListener)
        binding.lockButton8.setOnClickListener(numberButtonClickListener)
        binding.lockButton9.setOnClickListener(numberButtonClickListener)
        binding.lockButton10.setOnClickListener(numberButtonClickListener)
    }

    companion object {
        const val TAG = "LockFragment"
    }
}