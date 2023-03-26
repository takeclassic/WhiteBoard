package com.thinkers.whiteboard.common.recyclerview

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication

class SettingsListAdapter(
   val onBackupButtonClicked: () -> Unit,
   val onLockButtonClicked:() -> Unit,
   val onAutoRemoveToggleClicked: () -> Unit,
   val autoRemoveStatus: Boolean
): ListAdapter<String, SettingsListAdapter.SettingsViewHolder>(SettingsAdapterDiffCallback) {
    class SettingsViewHolder(
        itemView: View,
        val onBackupButtonClicked: () -> Unit,
        val onLockButtonClicked:() -> Unit,
        val onAutoRemoveToggleClicked: () -> Unit,
        val autoRemoveStatus: Boolean
    ): RecyclerView.ViewHolder(itemView) {
        private val settingName: TextView = itemView.findViewById(R.id.item_settings_name)

        fun bind(settingName: String) {
            this.settingName.text = settingName

            when(settingName) {
                "백업하기" -> {
                    itemView.setOnClickListener{ onBackupButtonClicked() }
                }
                "잠금설정" -> {
                    itemView.setOnClickListener{ onLockButtonClicked() }
                }
                "자동삭제" -> {
                    Log.i(TAG, "before marginTop: ${itemView.marginTop}, paddingTop: ${itemView.paddingTop}")

                    itemView.findViewById<ImageView>(R.id.item_settings_arrow).visibility = View.GONE

                    itemView.findViewById<SwitchCompat>(R.id.item_settings_switch).visibility = View.VISIBLE
                    itemView.findViewById<SwitchCompat>(R.id.item_settings_switch).isChecked = autoRemoveStatus
                    itemView.findViewById<SwitchCompat>(R.id.item_settings_switch)
                        .setOnClickListener{ onAutoRemoveToggleClicked() }

                    itemView.findViewById<TextView>(R.id.item_settings_text).visibility = View.VISIBLE
                    val applicationContext = WhiteBoardApplication.context()
                    itemView.findViewById<TextView>(R.id.item_settings_text).text = applicationContext.getText(R.string.item_settings_text)

                    Log.i(TAG, "after marginTop: ${itemView.marginTop}, paddingTop: ${itemView.paddingTop}")
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
            onAutoRemoveToggleClicked,
            autoRemoveStatus
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
