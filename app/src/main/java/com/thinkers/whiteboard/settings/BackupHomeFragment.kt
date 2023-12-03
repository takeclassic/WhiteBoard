package com.thinkers.whiteboard.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentBackupHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class BackupHomeFragment : Fragment() {
    private val viewModel: BackupHomeViewModel by viewModels()
    private var _binding: FragmentBackupHomeBinding? = null
    private val binding get() = _binding!!

    private val backPressListener = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backPressListenerImpl()
        }
    }

    private val backPressListenerImpl: () -> Unit = {
        val auth = Firebase.auth
        auth.signOut()
        findNavController().navigate(R.id.action_nav_backup_home_to_nav_settings)
    }

    private val deleteButtonListener = OnClickListener {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dialogTitle = getString(R.string.horizontal_progress_delete_text)
            viewModel.state = BackupHomeViewModel.Companion.states.DELETE
            val instance = HorizontalProgressBarFragment()
            instance.show(childFragmentManager, HorizontalProgressBarFragment.TAG)
        }
    }

    private val restoreButtonListener = OnClickListener {
        showRestoreWarningAlert()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackupHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backupHomeProgressbar.visibility = View.VISIBLE
        val toolbar = binding.backupHomeToolbar
        toolbar.setNavigationOnClickListener {
            backPressListenerImpl()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.checkUpdates()
                launch {
                    viewModel.metaSize.collectLatest {
                        Log.i(TAG, "metasize: $it")
                        binding.backupHomeProgressbar.visibility = View.GONE
                        if (it == 0L) {
                            binding.backupHomeEmptyText.visibility = View.VISIBLE
                            binding.backupHomeHistorySizeTitle.visibility = View.GONE
                            binding.backupHomeHistorySizeContent.visibility = View.GONE
                            binding.backupHomeHistoryLayout.setOnClickListener(null)
                        } else {
                            binding.backupHomeEmptyText.visibility = View.GONE
                            binding.backupHomeHistorySizeTitle.visibility = View.VISIBLE
                            binding.backupHomeHistorySizeContent.visibility = View.VISIBLE
                            binding.backupHomeHistorySizeContent.text =
                                "${(it / 1000).toString()} KB"
                            binding.backupHomeHistoryLayout.setOnClickListener(restoreButtonListener)
                        }
                    }
                }
                launch {
                    viewModel.uploadDate.collectLatest {
                        Log.i(TAG, "uploadDate: $it")
                        binding.backupHomeProgressbar.visibility = View.GONE
                        if (it == 0L) {
                            binding.backupHomeHistoryLayout.setBackgroundColor(resources.getColor(R.color.light_grey, null))
                            binding.backupHomeEmptyText.visibility = View.VISIBLE
                            binding.backupHomeHistoryDateTitle.visibility = View.GONE
                            binding.backupHomeHistoryDateContent.visibility = View.GONE
                            binding.backupHomeHistoryDeadlineTitle.visibility = View.GONE
                            binding.backupHomeHistoryDeadlineContent.visibility = View.GONE
                            binding.backupHomeRestoreText.visibility = View.GONE
                        } else {
                            val c = Calendar.getInstance()
                            c.timeInMillis = it
                            c.add(Calendar.MONTH, 1)
                            val dueDate = c.timeInMillis
                            Log.i(TAG, "date: $it, before: ${Utils.showDate(it)}, after: ${Utils.showDate(dueDate)}")
                            binding.backupHomeHistoryLayout.setBackgroundColor(resources.getColor(R.color.app_main_color, null))
                            binding.backupHomeEmptyText.visibility = View.GONE
                            binding.backupHomeHistoryDateTitle.visibility = View.VISIBLE
                            binding.backupHomeHistoryDateContent.visibility = View.VISIBLE
                            binding.backupHomeHistoryDateContent.text = Utils.showDate(it)
                            binding.backupHomeHistoryDeadlineTitle.visibility = View.VISIBLE
                            binding.backupHomeHistoryDeadlineContent.visibility = View.VISIBLE
                            binding.backupHomeRestoreText.visibility = View.VISIBLE
                            binding.backupHomeHistoryDeadlineContent.text = Utils.showDate(dueDate)
                        }
                    }
                }
            }
        }

        binding.backupHomeBackupButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.dialogTitle = getString(R.string.horizontal_progress_upload_text)
                viewModel.state = BackupHomeViewModel.Companion.states.BACK_UP
                Log.i(TAG, "set title: ${viewModel.dialogTitle}, viewmodel: ${viewModel.hashCode()}")
                val instance = HorizontalProgressBarFragment()
                instance.show(childFragmentManager, HorizontalProgressBarFragment.TAG)
            }
        }
        binding.backupHomeRemoveButton.setOnClickListener(deleteButtonListener)
    }

    private fun showRestoreWarningAlert() {
        Utils.showAlertDialog(
            requireContext(),
            "데이터 복구를 진행하시겠습니까?",
            "현재 폰에 저장된 메모는 모두 삭제되고 백업된 데이터로 대체됩니다.",
            "예",
            "아니오",
            restore,
            null
        )
    }

    private val restore: () -> Unit = {
        viewModel.state = BackupHomeViewModel.Companion.states.RESTORE
        viewModel.dialogTitle = getString(R.string.horizontal_progress_download_text)
        val instance = HorizontalProgressBarFragment()
        instance.show(childFragmentManager, HorizontalProgressBarFragment.TAG)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressListener)
    }

    override fun onDetach() {
        super.onDetach()
        backPressListener.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BackupHomeFragment"
    }
}
