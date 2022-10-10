package com.thinkers.whiteboard.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.database.entities.Note

class NoteListAdapter(
    private val onDelete: (Note) -> Unit,
    private val onEdit: (Note) -> Unit,
    private val onMove: (Note) -> Unit,
    private val isActionMode: Boolean
) : ListAdapter<Note, NoteListAdapter.NoteViewHolder>(NoteDiffCallback) {

    class NoteViewHolder(
        itemView: View,
        onDelete: (Note) -> Unit,
        onEdit: (Note) -> Unit,
        onMove: (Note) -> Unit,
        isActionMode: Boolean
    ): RecyclerView.ViewHolder(itemView) {
        private val noteName: TextView = itemView.findViewById(R.id.item_note_name)
        private var editNoteName: ImageView? = null
        private var deleteNote: ImageView? = null

        var currentNote: Note? = null

        init {
            if (!isActionMode) {
                editNoteName = itemView.findViewById(R.id.item_note_edit)
                deleteNote = itemView.findViewById(R.id.item_note_delete)
            }

            editNoteName?.setOnClickListener {
                currentNote?.let(onEdit)
            }
            deleteNote?.setOnClickListener {
                currentNote?.let(onDelete)
            }

            if (isActionMode) {
                noteName.setOnClickListener {
                    currentNote?.let(onMove)
                }
            }
        }

        fun bind(note: Note) {
            currentNote = note
            noteName.text = note.noteName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        if (isActionMode) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_note_actionmode, parent, false)
            return NoteViewHolder(view, onDelete, onEdit, onMove, isActionMode)
        } else {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(view, onDelete, onEdit, onMove, isActionMode)
        }
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }
}

object NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.noteName == newItem.noteName
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}
