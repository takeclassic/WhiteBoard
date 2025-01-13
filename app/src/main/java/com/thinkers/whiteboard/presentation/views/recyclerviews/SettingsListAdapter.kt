package com.thinkers.whiteboard.presentation.views.recyclerviews

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.databinding.ItemSettingsBinding

class SettingsListAdapter(
    val onBackupButtonClicked: () -> Unit,
    val onLockToggleClicked: (SwitchCompat) -> Unit,
    val lockStatus: Boolean
): ListAdapter<String, SettingsListAdapter.SettingsViewHolder>(SettingsAdapterDiffCallback) {
    class SettingsViewHolder(
        val binding: ItemSettingsBinding,
        val onBackupButtonClicked: () -> Unit,
        val onLockToggleClicked: (SwitchCompat) -> Unit,
        val lockStatus: Boolean
    ): RecyclerView.ViewHolder(binding.root) {
        private val settingName: TextView = binding.itemSettingsName
        fun bind(settingName: String) {
            this.settingName.text = settingName

            when(settingName) {
                "백업하기" -> {
                    itemView.setOnClickListener{ onBackupButtonClicked() }
                }
                "잠금설정" -> {
                    binding.itemSettingsArrow.visibility = View.GONE

                    binding.itemSettingsSwitch.visibility = View.VISIBLE
                    binding.itemSettingsSwitch.isChecked = lockStatus
                    Log.i(TAG, "lockStatus: $lockStatus")
                    binding.itemSettingsSwitch.setOnClickListener{ onLockToggleClicked(binding.itemSettingsSwitch) }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingsViewHolder {
        val binding = ItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SettingsViewHolder(
            binding,
            onBackupButtonClicked,
            onLockToggleClicked,
            lockStatus
        )
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val settingName = getItem(position)
        holder.bind(settingName)
    }

    companion object {
        const val TAG = "SettingsListAdapter"
    }
}

object SettingsAdapterDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
