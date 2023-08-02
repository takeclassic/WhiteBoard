package com.thinkers.whiteboard.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.recyclerview.SettingsListAdapter
import com.thinkers.whiteboard.common.utils.CryptoHelper
import com.thinkers.whiteboard.common.view.CustomDecoration
import com.thinkers.whiteboard.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsListAdapter: SettingsListAdapter

    private var autoRemoveSwitch: Boolean = false
    private var lockSwitch: Boolean = false

    private var fileName: String = ""
    private var autoRemoveKey: String = ""
    //TODO: this value should be in the server not the client
    private var lockKey: String = ""

    private val onBackupButtonClicked: () -> Unit = {
        findNavController().navigate(R.id.nav_backup)
    }

    private val onLockToggleClicked: (SwitchCompat) -> Unit = { switch ->
        viewLifecycleOwner.lifecycleScope.launch {
            val res = CryptoHelper.decryptPassCodeAesGcm()
            if (res == "failed") {
                Toast.makeText(requireContext(), R.string.settings_passcode_is_not_set, Toast.LENGTH_SHORT).show()
                switch.toggle()
                return@launch
            }
            lockSwitch = !lockSwitch
            viewModel.putSwitchStatus(fileName, lockKey, lockSwitch)
        }
    }

    private val onPasscodeSetButtonClicked: () -> Unit = {
        val action = SettingsFragmentDirections.actionNavSettingsToLockFragment()
        findNavController().navigate(action)
    }

    private val onAutoRemoveToggleClicked: () -> Unit = {
        autoRemoveSwitch = !autoRemoveSwitch
        viewModel.putSwitchStatus(fileName, autoRemoveKey, autoRemoveSwitch) { WhiteBoardApplication.instance!!.startAutoRemove() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(SettingsViewModel::class.java)
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
        val settingsArray: Array<String> = requireContext().resources.getStringArray(R.array.settings)
        fileName = requireActivity().getString(R.string.file_name_shared_preference)
        autoRemoveKey = requireActivity().getString(R.string.key_auto_remove)
        lockKey = requireActivity().getString(R.string.key_lock)

        viewLifecycleOwner.lifecycleScope.launch {
            autoRemoveSwitch = viewModel.getSwtichStatus(fileName, autoRemoveKey)
            lockSwitch = viewModel.getSwtichStatus(fileName, lockKey)
            Log.i(TAG, "autoRemoveSwitch: $autoRemoveSwitch, lockSwitch: $lockSwitch")

            settingsListAdapter = SettingsListAdapter(
                onBackupButtonClicked,
                onPasscodeSetButtonClicked,
                onAutoRemoveToggleClicked,
                onLockToggleClicked,
                autoRemoveSwitch,
                lockSwitch
            )
            binding.settingsClose.setOnClickListener { requireActivity().onBackPressed() }
            binding.settingsRecyclerview.recyclerView.adapter = settingsListAdapter
            settingsListAdapter.submitList(settingsArray.toList())
            drawDivider()
        }
    }

    private fun drawDivider() {
        val customDecoration = CustomDecoration(1f, 5f, resources.getColor(R.color.default_icon, null))
        binding.settingsRecyclerview.recyclerView.addItemDecoration(customDecoration)
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
