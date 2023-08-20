package com.thinkers.whiteboard.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentBackupHomeBinding

class BackupHomeFragment : Fragment() {
    private val viewModel: BackupHomeViewModel by viewModels()
    private var _binding: FragmentBackupHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackupHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = binding.backupHomeToolbar
        toolbar.setNavigationOnClickListener {
            val auth = Firebase.auth
            auth.signOut()
            findNavController().navigate(R.id.action_nav_backup_home_to_nav_settings)
        }
    }

}