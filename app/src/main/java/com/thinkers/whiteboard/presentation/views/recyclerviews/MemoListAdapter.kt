package com.thinkers.whiteboard.presentation.views.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.database.entities.Memo
import java.text.SimpleDateFormat
import java.util.*

class MemoListAdapter(
    private val onClick: (View, Memo) -> Unit,
    private val onLongClick: (View, Memo) -> Boolean,
    private val onBind: (View, Memo) -> Unit,
    private val isCustomNote: Boolean
    ) :
    ListAdapter<Memo, MemoListAdapter.MemoViewHolder>(MemoDiffCallback) {
    inner class MemoViewHolder(
        private val itemView: View,
        private val onClick: (View, Memo) -> Unit,
        private val onLongClick: (View, Memo) -> Boolean,
        private val onBind: (View, Memo) -> Unit,
        private val isCustomNote: Boolean
    ): RecyclerView.ViewHolder(itemView) {
        private val memoText: TextView = itemView.findViewById(R.id.memo_text)
        private val memoNoteName: TextView = itemView.findViewById(R.id.memo_note_name)
        private val memoDate: TextView = itemView.findViewById(R.id.memo_date)

        private var currentMemo: Memo? = null

        fun bind(memo: Memo) {
            currentMemo = memo
            memoText.text = memo.text
            memoNoteName.text = memo.noteName
            memoDate.text = getDateFormat(memo.createdTime)

            onBind(itemView, memo)

            if (isCustomNote) {
                memoNoteName.visibility = View.GONE
            }

            itemView.setOnClickListener { view ->
                onClick(view, currentMemo!!)
            }
            itemView.setOnLongClickListener { view ->
                onLongClick(view, currentMemo!!)
            }
        }

        private fun getDateFormat(timeInmillis: Long): String {
            val dateFormat = "yyyy/MM/dd"
            val formatter = SimpleDateFormat(dateFormat)
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInmillis
            return formatter.format(calendar.time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memo, parent, false)
        return MemoViewHolder(view, onClick, onLongClick, onBind, isCustomNote)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = getItem(position)
        holder.bind(memo)
    }
}

object MemoDiffCallback : DiffUtil.ItemCallback<Memo>() {
    override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem.memoId == newItem.memoId
    }

    override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem == newItem
    }
}
