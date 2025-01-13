package com.thinkers.whiteboard.presentation.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.repositories.DataStoreKeys
import com.thinkers.whiteboard.presentation.views.recyclerviews.SettingsListAdapter
import com.thinkers.whiteboard.presentation.views.CustomDecoration
import com.thinkers.whiteboard.databinding.FragmentSettingsBinding
import com.thinkers.whiteboard.presentation.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    companion object {
        const val TAG = "SettingsFragment"
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var settingsListAdapter: SettingsListAdapter

    private var autoRemoveSwitch: Boolean = false
    private var lockSwitch: Boolean = false

    private val onBackupButtonClicked: () -> Unit = {
        findNavController().navigate(R.id.action_nav_settings_to_nav_backup)
    }

    private val onLockToggleClicked: (SwitchCompat) -> Unit = { switch ->
        lockSwitch = !lockSwitch
        viewModel.putSwitchStatus(DataStoreKeys.BOOLEAN_KEY_LOCK_MODE, lockSwitch)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isResume = arguments?.getBoolean("isResume")
        isResume?.let {
            if (it) {
                val bundle = bundleOf("isResume" to true)
                findNavController().navigate(R.id.nav_lock, bundle)
            }
        }

        val settingsArray: Array<String> =
            requireContext().resources.getStringArray(R.array.settings)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                autoRemoveSwitch =
                    viewModel.getSwtichStatus(DataStoreKeys.BOOLEAN_KEY_AUTO_REMOVE, false)
                lockSwitch = viewModel.getSwtichStatus(DataStoreKeys.BOOLEAN_KEY_LOCK_MODE, false)

                Log.i(TAG, "autoRemoveSwitch: $autoRemoveSwitch, lockSwitch: $lockSwitch")
                settingsListAdapter = SettingsListAdapter(
                    onBackupButtonClicked,
                    onLockToggleClicked,
                    lockSwitch
                )
                binding.settingsClose.setOnClickListener { requireActivity().onBackPressed() }
                binding.settingsRecyclerview.recyclerView.adapter = settingsListAdapter
                settingsListAdapter.submitList(settingsArray.toList())
                drawDivider()
            }
        }
    }

    private fun drawDivider() {
        val customDecoration =
            CustomDecoration(1f, 5f, resources.getColor(R.color.default_icon, null))
        binding.settingsRecyclerview.recyclerView.addItemDecoration(customDecoration)
    }
}
