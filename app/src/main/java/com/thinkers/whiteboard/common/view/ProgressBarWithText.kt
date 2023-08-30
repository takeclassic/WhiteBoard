package com.thinkers.whiteboard.common.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.databinding.ProgressBarWithTextBinding


class ProgressBarWithText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayoutCompat(context, attrs, defStyleAttr) {
    var progressBar: ProgressBar
    var textView: TextView
    var layout: LinearLayout

    init {
        val binding = ProgressBarWithTextBinding.inflate(LayoutInflater.from(context), this, true)
        progressBar = binding.progressBar
        textView = binding.textView
        layout = binding.cutomProgressbarLayout

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.custom_progress_bar)
        try {
            val textColor = typedArray.getInt(
                R.styleable.custom_progress_bar_textColor, context.getColor(
                    R.color.default_icon))
            textView.setTextColor(textColor)

            val progressColor = typedArray.getInt(
                R.styleable.custom_progress_bar_progressColor, context.getColor(
                    R.color.default_icon))
            progressBar.indeterminateTintList = ColorStateList.valueOf(progressColor)

            val text = typedArray.getString(R.styleable.custom_progress_bar_text)
            textView.text = text

            val textSize = typedArray.getFloat(R.styleable.custom_progress_bar_textSize, 16f)
            textView.textSize = textSize

            val backgroundColor = typedArray.getInt(R.styleable.custom_progress_bar_layoutColor, context.getColor(R.color.light_grey))
            layout.setBackgroundColor(backgroundColor)
        } catch (e: Exception) {

        } finally {
            typedArray.recycle()
        }
    }

    fun setProgressBarColor(color: Int) {
        progressBar.indeterminateTintList = ColorStateList.valueOf(color)
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    fun setText(text: String) {
        textView.setText(text)
    }

    fun setTextSize(size: Float) {
        textView.textSize = size
    }

    fun setLayoutColor(color: Int) {
        layout.setBackgroundColor(color)
    }
}