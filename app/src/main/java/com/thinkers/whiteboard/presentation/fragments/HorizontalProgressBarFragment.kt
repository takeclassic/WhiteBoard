package com.thinkers.whiteboard.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.HorizontalProgressbarFragmentBinding
import com.thinkers.whiteboard.presentation.viewmodels.BackupHomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@AndroidEntryPoint
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
                when(viewModel.state) {
                    BackupHomeViewModel.Companion.states.BACK_UP -> {
                        launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.uploadedSize.collectLatest {
                                    binding.horizontalProgressbarPercentageText.text = "$it%"
                                    binding.horizontalProgressbarProgressbar.progress = it.toInt()
                                    Log.i(TAG, "UP total: ${viewModel.totalSize.value}, current: $it")
                                }
                            }
                        }
                        viewModel.backUpDbFiles()
                        viewModel.checkUpdates()
                        viewModel.resetState()
                        dismiss()
                    }
                    BackupHomeViewModel.Companion.states.RESTORE -> {
                        launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.downloadSize.collectLatest {
                                    binding.horizontalProgressbarPercentageText.text = "$it%"
                                    binding.horizontalProgressbarProgressbar.progress = it.toInt()
                                    if (it == 100L) {
                                        binding.horizontalProgressbarTitle.text = getString(R.string.horizontal_progress_restore_text)
                                    }
                                    Log.i(TAG, "DOWN total: ${viewModel.currentMetaSize}, current: $it")
                                }
                            }
                        }
                        viewModel.restoreDbFiles(requireContext().filesDir.absolutePath)
                        viewModel.resetState()
                        Toast.makeText(requireContext(), "데이터 복구 완료. 복구된 데이터 사용을 위해 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
                        exitProcess(0)
                    }
                    BackupHomeViewModel.Companion.states.DELETE -> {
                        binding.horizontalProgressbarPercentageText.visibility = View.GONE
                        binding.horizontalProgressbarProgressbar.visibility = View.GONE

                        if (viewModel.deleteFilesOnServer()) {
                            delay(500)
                            viewModel.checkUpdates()
                            viewModel.resetState()
                        } else {
                            Toast.makeText(requireContext(), "파일 삭제에 실패했습니다. 다시 한번 시도해주세요", Toast.LENGTH_SHORT).show()
                        }
                        dismiss()
                    }
                    BackupHomeViewModel.Companion.states.NONE -> {

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
