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
    private val onEdit: (Note) -> Unit
) : ListAdapter<Note, NoteListAdapter.NoteViewHolder>(NoteDiffCallback) {

    class NoteViewHolder(
        itemView: View,
        private val onDelete: (Note) -> Unit,
        private val onEdit: (Note) -> Unit
    ): RecyclerView.ViewHolder(itemView) {
        private val noteName: TextView = itemView.findViewById(R.id.item_note_name)
        private val editNoteName: ImageView = itemView.findViewById(R.id.item_note_delete)
        private val deleteNote: ImageView = itemView.findViewById(R.id.item_note_edit)

        private var currentNote: Note? = null

        init {
            editNoteName.setOnClickListener {
                currentNote?.let(onEdit)
            }
            deleteNote.setOnClickListener {
                currentNote?.let(onDelete)
            }
        }

        fun bind(note: Note) {
            currentNote = note
            noteName.text = note.noteName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view, onDelete, onEdit)
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