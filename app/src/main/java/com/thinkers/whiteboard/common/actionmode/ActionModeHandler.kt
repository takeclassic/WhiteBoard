package com.thinkers.whiteboard.common.actionmode

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.database.entities.Memo

class ActionModeHandler(
    private val actionModeSetMemoList: List<Memo>,
    private val activity: Activity,
    private val onActionModeRemove: () -> Unit,
    private val onActionModeMove: () -> Boolean,
    private val onDestroyActionMode: () -> Unit
): ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.action_mode, menu)
        Log.i(TAG, "onCreateActionMode")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        Log.i(TAG, "onPrepareActionMode")
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mode_share -> {
                if (actionModeSetMemoList.size != 1) { return false }

                val memoToSend = actionModeSetMemoList[0]
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, memoToSend.text)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                activity.startActivity(shareIntent)
                true
            }
            R.id.action_mode_delete -> {
                onActionModeRemove()
                true
            }
            R.id.action_mode_move -> {
                onActionModeMove()
            }
            else -> {
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        Log.i(TAG, "onDestroyActionMode")
        onDestroyActionMode()
    }

    companion object {
        const val TAG = "ActionModeHandler"
    }
}