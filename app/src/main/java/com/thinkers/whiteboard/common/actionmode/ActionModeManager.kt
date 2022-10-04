package com.thinkers.whiteboard.common.actionmode

import android.app.Activity
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.database.entities.Memo

class ActionModeManager(
    private val actionModeSetMemoList: List<Memo>,
    private val activity: Activity,
    private val viewModel: ViewModel,
    private val onDestroyActionMode: () -> Unit
): ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mode_share -> {
                true
            }
            R.id.action_mode_delete -> {
                true
            }
            R.id.action_mode_move -> {
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        onDestroyActionMode()
    }
}