package com.thinkers.whiteboard.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentLockBinding
import com.thinkers.whiteboard.databinding.FragmentSettingsBinding
import java.lang.StringBuilder

class LockFragment : Fragment() {
    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!

    private val lockViewModel: LockViewModel by viewModels()

    private val numberButtonClickListener = View.OnClickListener {
        Log.i(TAG, "${lockViewModel.passcode}")

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
        changeLockNumberImage()

        if (lockViewModel.passcode.length == 4) {
            lockViewModel.encryptPasscode()
            return@OnClickListener
        }
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
        lockViewModel.initProperties()
        lockViewModel.lockNumbers.add(binding.lockNumber1)
        lockViewModel.lockNumbers.add(binding.lockNumber2)
        lockViewModel.lockNumbers.add(binding.lockNumber3)
        lockViewModel.lockNumbers.add(binding.lockNumber4)

        binding.lockClose.setOnClickListener {
            requireActivity().onBackPressed()
        }
        addNumberButtonListeners(binding)

        binding.lockBackspaceButton.setOnClickListener {
            lockViewModel.passcode.apply {
                if (this.isNotEmpty()) {
                    changeLockNumberImage()
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

    private fun changeLockNumberImage() {
        Log.i(TAG, "passcode: ${lockViewModel.passcode}, length: ${lockViewModel.passcode.length-1}")
        val index = lockViewModel.passcode.length-1
        if (index < 0) return

        if (lockViewModel.lockTinted[index]) {
            lockViewModel.lockNumbers[index].imageTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.default_drawable))
            lockViewModel.lockTinted[index] = false
        } else {
            lockViewModel.lockNumbers[index].imageTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.black))
            lockViewModel.lockTinted[index] = true
        }
    }

    companion object {
        const val TAG = "LockFragment"
    }
}
