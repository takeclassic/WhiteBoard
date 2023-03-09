package com.thinkers.whiteboard.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.common.recyclerview.SettingsListAdapter
import com.thinkers.whiteboard.common.view.CustomDecoration
import com.thinkers.whiteboard.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsListAdapter: SettingsListAdapter

    private val onBackupButtonClicked: () -> Unit = {

    }

    private val onLockButtonClicked: () -> Unit = {

    }

    private val onAutoRemoveToggleClicked: () -> Unit = {

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

        settingsListAdapter = SettingsListAdapter(
            onBackupButtonClicked,
            onLockButtonClicked,
            onAutoRemoveToggleClicked
        )

        binding.settingsRecyclerview.recyclerView.adapter = settingsListAdapter
        settingsListAdapter.submitList(settingsArray.toList())
        drawDivider()
    }

    private fun drawDivider() {
        val customDecoration = CustomDecoration(1f, 30f, resources.getColor(R.color.default_icon, null))
        binding.settingsRecyclerview.recyclerView.addItemDecoration(customDecoration)
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
