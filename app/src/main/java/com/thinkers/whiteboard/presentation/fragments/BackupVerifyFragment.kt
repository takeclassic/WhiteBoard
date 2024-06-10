package com.thinkers.whiteboard.presentation.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.enums.AuthAddress
import com.thinkers.whiteboard.databinding.FragmentBackupVerifyBinding
import com.thinkers.whiteboard.domain.SendVerifyEmailUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BackupVerifyFragment : Fragment() {
    companion object {
        const val TAG = "BackupVerifyFragment"
    }

    private var _binding: FragmentBackupVerifyBinding? = null
    private val binding get() = _binding!!
    private val auth = Firebase.auth
    private val user = auth.currentUser!!
    @Inject lateinit var sendVerifyEmailUseCase: SendVerifyEmailUseCase

    private val authStateListener = FirebaseAuth.AuthStateListener {
        Log.i(TAG, "auth state changed")
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            checkVerified()
        }
    }

    private val checkVerifiedListener = View.OnClickListener { checkVerified() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackupVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated()")

        val text = getString(R.string.backup_verify_address, user.email)
        binding.backupVerifyAddress.text = text
        checkVerified()

        binding.backupVerifyCloseButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.backupVerifyRefresh.setOnClickListener(checkVerifiedListener)
        binding.backupVerifyRetry.setOnClickListener {
            Log.i(TAG, "retry touched!")
            sendVerifyEmailUseCase(AuthAddress.URL_VERIFY.str, AuthAddress.REDIRECT_PACKAGE_NAME.str) {
                Log.i(TAG, "send completed")
                Toast.makeText(requireContext(), "이메일 재전송 완료 되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
        checkVerified()
    }

    private fun checkVerified() {
        user.reload()
        if (user.isEmailVerified) {
            Log.i(TAG, "verified")
            auth.removeAuthStateListener(authStateListener)
            binding.backupVerifyRefresh.setOnClickListener(null)
            findNavController().navigate(R.id.action_nav_backup_verify_to_nav_backup_home)
        } else {
            Log.i(TAG, "NOT verified")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestory()")
        _binding = null
        auth.removeAuthStateListener(authStateListener)
    }
}