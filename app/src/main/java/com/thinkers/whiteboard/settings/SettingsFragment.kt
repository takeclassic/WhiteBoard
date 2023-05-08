package com.thinkers.whiteboard.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.recyclerview.SettingsListAdapter
import com.thinkers.whiteboard.common.view.CustomDecoration
import com.thinkers.whiteboard.databinding.FragmentSettingsBinding
import com.thinkers.whiteboard.search.SearchViewModel
import com.thinkers.whiteboard.search.SearchViewModelFactory
import com.thinkers.whiteboard.total.TotalFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsListAdapter: SettingsListAdapter

    private var autoRemoveSwitch: Boolean = false

    private val onBackupButtonClicked: () -> Unit = {

    }

    private val onLockButtonClicked: () -> Unit = {
        val action = SettingsFragmentDirections.actionNavSettingsToLockFragment()
        findNavController().navigate(action)
    }

    private val onAutoRemoveToggleClicked: () -> Unit = {
        viewModel.putAutoRemoveSwitchStatus(requireActivity(), !autoRemoveSwitch)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(SettingsViewModel::class.java)

        lifecycleScope.launchWhenCreated {
            autoRemoveSwitch = viewModel.getAutoRemoveSwtichStatus(requireActivity())
            Log.i(TAG, "autoRemoveSwitch: $autoRemoveSwitch")
        }
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
            onAutoRemoveToggleClicked,
            autoRemoveSwitch
        )

        binding.settingsClose.setOnClickListener { requireActivity().onBackPressed() }
        binding.settingsRecyclerview.recyclerView.adapter = settingsListAdapter
        settingsListAdapter.submitList(settingsArray.toList())
        drawDivider()
    }

    private fun drawDivider() {
        val customDecoration = CustomDecoration(1f, 5f, resources.getColor(R.color.default_icon, null))
        binding.settingsRecyclerview.recyclerView.addItemDecoration(customDecoration)
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
