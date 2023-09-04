package com.thinkers.whiteboard.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentBackupSendPasswordResetEmailBinding

class BackupSendPasswordResetEmailFragment : Fragment() {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}