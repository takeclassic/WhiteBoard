package com.thinkers.whiteboard.common

import android.text.format.DateFormat.getDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.database.entities.Memo
import java.text.SimpleDateFormat
import java.util.*

class MemoListAdapter(private val onClick: (Memo) -> Unit) :
    ListAdapter<Memo, MemoListAdapter.MemoViewHolder>(MemoDiffCallback) {

    class MemoViewHolder(itemView: View, val onClick: (Memo) -> Unit): RecyclerView.ViewHolder(itemView) {
        private val memoTitle: TextView = itemView.findViewById(R.id.memo_title)
        private val memoText: TextView = itemView.findViewById(R.id.memo_text)
        private val memoNoteName: TextView = itemView.findViewById(R.id.memo_note_name)
        private val memoDate: TextView = itemView.findViewById(R.id.memo_date)

        private var currentMemo: Memo? = null

        init {
            itemView.setOnClickListener {
                currentMemo?.let(onClick)
            }
        }

        fun bind(memo: Memo) {
            currentMemo = memo
            if (memo.title.isNullOrBlank()) {
                memoTitle.visibility = View.GONE
            }
            memoTitle.text = memo.title
            memoText.text = memo.text
            memoNoteName.text = memo.noteName
            memoDate.text = getDateFormat(memo.createdTime)
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
        return MemoViewHolder(view, onClick)
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