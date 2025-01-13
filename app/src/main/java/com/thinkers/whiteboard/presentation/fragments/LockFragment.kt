package com.thinkers.whiteboard.presentation.fragments

import android.app.KeyguardManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentLockBinding
import com.thinkers.whiteboard.presentation.viewmodels.LockViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

@AndroidEntryPoint
class LockFragment : Fragment() {
    companion object {
        const val TAG = "LockFragment"
    }

    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!

    private val lockViewModel: LockViewModel by viewModels()
    private var isResume: Boolean = false

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lockCautionText.visibility = View.VISIBLE
        binding.lockClose.visibility = View.VISIBLE
        this.isResume = false
        val isResume: Boolean? = arguments?.getBoolean("isResume")
        Log.i(TAG, "isResume: $isResume")
        isResume?.let {
            if(it) {
                this.isResume = true
                binding.lockCautionText.visibility = View.GONE
                binding.lockClose.visibility = View.INVISIBLE
            }
        }

        lockViewModel.initProperties()

        binding.lockClose.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val biometricManager = BiometricManager.from(requireContext())
        if (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast
                .makeText(
                    requireContext(),
                    R.string.error_no_device_authentication,
                    Toast.LENGTH_SHORT
                ).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(),
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    findNavController().popBackStack()
                    findNavController().popBackStack()
                    //findNavController().navigate(R.id.nav_total)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(requireContext().getString(R.string.lock_authentication_title))
            .setSubtitle(requireContext().getString(R.string.lock_authentication_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
