package com.thinkers.whiteboard.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentBackupLogInBinding
import com.thinkers.whiteboard.databinding.FragmentBackupVerifyBinding

class BackupVerifyFragment : Fragment() {
    private val viewModel: BackupVerifyViewModel by viewModels()
    private var _binding: FragmentBackupVerifyBinding? = null
    private val binding get() = _binding!!
    private val auth = Firebase.auth
    private val user = auth.currentUser!!

    private val authStateListener = FirebaseAuth.AuthStateListener {
        Log.i(TAG, "something has changed!")
        user.reload()
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
        val text = getString(R.string.backup_verify_address, user.email)
        binding.backupVerifyAddress.text = text
        binding.backupVerifyCloseButton.setOnClickListener {
            findNavController().popBackStack()
        }

        auth.addAuthStateListener(authStateListener)

        binding.backupVerifyRefresh.setOnClickListener {
            user.reload()
            if (user.isEmailVerified) {
                Log.i(TAG, "verified")
                auth.removeAuthStateListener(authStateListener)
                findNavController().navigate(R.id.action_nav_backup_verify_to_nav_backup_home)
            } else {
                Log.i(TAG, "NOT verified")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        user.reload()
        if (user.isEmailVerified) {
            Log.i(TAG, "verified")
            auth.removeAuthStateListener(authStateListener)
            findNavController().navigate(R.id.action_nav_backup_verify_to_nav_backup_home)
        } else {
            Log.i(TAG, "NOT verified")
        }
    }

    companion object {
        const val TAG = "BackupVerifyFragment"
    }

}