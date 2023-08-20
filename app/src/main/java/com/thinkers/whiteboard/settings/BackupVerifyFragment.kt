package com.thinkers.whiteboard.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentBackupLogInBinding
import com.thinkers.whiteboard.databinding.FragmentBackupVerifyBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BackupVerifyFragment : Fragment() {
    private var _binding: FragmentBackupVerifyBinding? = null
    private val binding get() = _binding!!
    private val auth = Firebase.auth
    private val user = auth.currentUser!!

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

    companion object {
        const val TAG = "BackupVerifyFragment"
    }

}