package com.thinkers.whiteboard.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.FragmentBackupBinding
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
    }

}