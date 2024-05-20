package com.thinkers.whiteboard.presentation.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.enums.AuthErrorCodes
import com.thinkers.whiteboard.data.enums.AuthInfo
import com.thinkers.whiteboard.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentBackupSendPasswordResetEmailBinding
import com.thinkers.whiteboard.presentation.viewmodels.BackupSendPasswordResetEmailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BackupSendPasswordResetEmailFragment : Fragment() {
    companion object {
        const val TAG = "BackupSendPasswordResetEmailFragment"
    }

    private val viewModel: BackupSendPasswordResetEmailViewModel by viewModels()
    private var _binding: FragmentBackupSendPasswordResetEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackupSendPasswordResetEmailBinding.inflate(
            inflater,
            container,
            false)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sendPasswordResetEmailEdittext.isFocusableInTouchMode = true;
        binding.sendPasswordResetEmailEdittext.requestFocus();
        Utils.showKeyboard(requireContext(), binding.sendPasswordResetEmailEdittext)

        binding.sendPasswordResetEmailLayout.setOnTouchListener { v, event ->
            Utils.hideKeyboard(requireContext(), v)
            v.clearFocus()
            false
        }

        binding.sendPasswordResetEmailBackButton.setOnClickListener {
            Utils.hideKeyboard(requireContext(), binding.sendPasswordResetEmailEdittext)
            findNavController().popBackStack()
        }

        binding.sendPasswordResetEmailEdittext.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.passwordResetConfirmButton.callOnClick()
                true
            }
            false
        }

        binding.passwordResetConfirmButton.setOnClickListener {
            binding.sendPasswordResetEmailProgressbar.visibility = View.VISIBLE
            Utils.hideKeyboard(requireContext(), it)
            it.clearFocus()
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.emailAddress = binding.sendPasswordResetEmailEdittext.text.toString()
                viewModel.sendPasswordResetEmail().collect { res ->
                    when (res) {
                        is AuthInfo.Success -> {
                            binding.sendPasswordResetEmailProgressbar.visibility = View.GONE
                            binding.sendPasswordResetEmailSuccessCheck.visibility = View.VISIBLE
                            showToast(R.string.send_email_completed)
                            delay(1800)
                            findNavController().popBackStack()
                        }
                        is AuthInfo.Failure -> {
                            binding.sendPasswordResetEmailProgressbar.visibility = View.GONE
                            if (res.errorCode == AuthErrorCodes.NOT_EMAIL_FORM) {
                                showToast(R.string.backup_id_info_text)
                            } else if (res.errorCode == AuthErrorCodes.NOT_EXIST) {
                                showToast(R.string.email_not_exist)
                            } else if (res.errorCode == AuthErrorCodes.NETWORK) {
                                showToast(R.string.backup_login_network_problem)
                            } else if (res.errorCode == AuthErrorCodes.TOO_MANY_REQUEST) {
                                showToast(R.string.max_retry_count)
                            } else {
                                showToast(R.string.backup_login_default_problem)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(id: Int) {
        Toast.makeText(
            requireContext(),
            id,
            Toast.LENGTH_SHORT
        ).show()
    }
}
