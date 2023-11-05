package com.thinkers.whiteboard.settings

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.thinkers.whiteboard.databinding.HorizontalProgressbarFragmentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HorizontalProgressBarFragment: DialogFragment() {
    companion object {
        const val TAG = "HorizontalProgressBarFragment"
    }

    private var _binding: HorizontalProgressbarFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BackupHomeViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그라 dismiss 되지 않는다.
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HorizontalProgressbarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        Log.i(TAG, "dialog title: ${viewModel.dialogTitle}, viewmodel: ${viewModel.hashCode()}")
        binding.horizontalProgressbarTitle.text = viewModel.dialogTitle

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.backUpDbFiles()
                viewModel.checkUpdates()
                dismiss()
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.i(TAG, "I AM HERE")
                viewModel.uploadedSize.collectLatest {
                    binding.horizontalProgressbarPercentageText.text = "$it%"
                    binding.horizontalProgressbarProgressbar.progress = it.toInt()
                    Log.i(TAG, "DOWN total: ${viewModel.totalSize.value}, current: $it")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
