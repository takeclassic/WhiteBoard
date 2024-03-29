package com.thinkers.whiteboard.presentation.views.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.database.entities.Memo
import java.text.SimpleDateFormat
import java.util.*

class MemoPagingAdapter(private val onClick: (Memo) -> Unit) :
    PagingDataAdapter<Memo, MemoPagingAdapter.MemoPagingViewHolder>(MemoPagingDiffCallback) {

    class MemoPagingViewHolder(itemView: View, val onClick: (Memo) -> Unit): RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoPagingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memo, parent, false)
        return MemoPagingViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: MemoPagingViewHolder, position: Int) {
        val memo = getItem(position)
        if (memo != null) {
            holder.bind(memo)
        }
    }
}

object MemoPagingDiffCallback : DiffUtil.ItemCallback<Memo>() {
    override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem.memoId == newItem.memoId
    }

    override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem == newItem
    }
}
