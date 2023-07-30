package com.thinkers.whiteboard.common.recyclerview

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.databinding.ItemSettingsBinding

class SettingsListAdapter(
    val onBackupButtonClicked: () -> Unit,
    val onPasscodeSetButtonClicked:() -> Unit,
    val onAutoRemoveToggleClicked: () -> Unit,
    val onLockToggleClicked: (SwitchCompat) -> Unit,
    val autoRemoveStatus: Boolean,
    val lockStatus: Boolean
): ListAdapter<String, SettingsListAdapter.SettingsViewHolder>(SettingsAdapterDiffCallback) {
    class SettingsViewHolder(
        val binding: ItemSettingsBinding,
        val onBackupButtonClicked: () -> Unit,
        val onPasscodeSetButtonClicked:() -> Unit,
        val onAutoRemoveToggleClicked: () -> Unit,
        val onLockToggleClicked: (SwitchCompat) -> Unit,
        val autoRemoveStatus: Boolean,
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
                "비밀번호 설정" -> {
                    itemView.setOnClickListener{ onPasscodeSetButtonClicked() }
                }
                "자동삭제" -> {
                    binding.itemSettingsArrow.visibility = View.GONE

                    binding.itemSettingsSwitch.visibility = View.VISIBLE
                    binding.itemSettingsSwitch.isChecked = autoRemoveStatus
                    Log.i(TAG, "autoRemoveStatus: $autoRemoveStatus")
                    binding.itemSettingsSwitch.setOnClickListener{ onAutoRemoveToggleClicked() }

                    binding.itemSettingsText.visibility = View.VISIBLE
                    val applicationContext = WhiteBoardApplication.context()
                    binding.itemSettingsText.text = applicationContext.getText(R.string.item_settings_text)
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
            onPasscodeSetButtonClicked,
            onAutoRemoveToggleClicked,
            onLockToggleClicked,
            autoRemoveStatus,
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
