package com.thinkers.whiteboard.common.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R

class SettingsListAdapter(
   val onBackupButtonClicked: () -> Unit,
   val onLockButtonClicked:() -> Unit,
   val onAutoRemoveToggleClicked: () -> Unit,
): ListAdapter<String, SettingsListAdapter.SettingsViewHolder>(SettingsAdapterDiffCallback) {
    class SettingsViewHolder(
        itemView: View,
        val onBackupButtonClicked: () -> Unit,
        val onLockButtonClicked:() -> Unit,
        val onAutoRemoveToggleClicked: () -> Unit,
    ): RecyclerView.ViewHolder(itemView) {
        private val settingName: TextView = itemView.findViewById(R.id.item_settings_name)

        fun bind(settingName: String) {
            this.settingName.text = settingName

            when(settingName) {
                "백업하기" -> {
                    this.settingName.setOnClickListener{ onBackupButtonClicked() }
                }
                "잠금설정" -> {
                    this.settingName.setOnClickListener{ onLockButtonClicked() }
                }
                "자동삭제" -> {
                    this.settingName.setOnClickListener{ onAutoRemoveToggleClicked() }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingsViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_settings, parent, false)

        return SettingsViewHolder(
            view,
            onBackupButtonClicked,
            onLockButtonClicked,
            onAutoRemoveToggleClicked
        )
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val settingName = getItem(position)
        holder.bind(settingName)
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
