package com.thinkers.whiteboard.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.thinkers.whiteboard.R

class CustomNumberButtonView : View {
    var buttonColor: Int = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    var buttonTextColor: Int = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    var buttonText: String = "1"
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    private lateinit var buttonPaint: Paint

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.custom_number_button_view)
        buttonPaint = Paint()

        try {
            buttonColor = typedArray.getInt(R.styleable.custom_number_button_view_button_color, context.getColor(R.color.default_icon))
            buttonTextColor = typedArray.getInt(R.styleable.custom_number_button_view_button_text_color, context.getColor(R.color.default_icon))
            buttonText = typedArray.getString(R.styleable.custom_number_button_view_button_text)!!
        } catch (e: Exception) {

        } finally {
            typedArray.recycle()
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

    }

    override fun onDraw(canvas: Canvas?) {
        val viewWidthHalf = this.measuredWidth / 2
        val viewHeightHalf = this.measuredHeight / 2

        val radius = if (viewWidthHalf > viewHeightHalf) viewHeightHalf - 10 else viewWidthHalf - 10
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.isAntiAlias = true
        buttonPaint.color = buttonColor
        canvas?.drawCircle(
            viewWidthHalf.toFloat(),
            viewHeightHalf.toFloat(),
            radius.toFloat(),
            buttonPaint
        )

        buttonPaint.color = buttonTextColor
        buttonPaint.textAlign = Paint.Align.CENTER
        buttonPaint.textSize = 80f
        canvas?.drawText(
            buttonText,
            viewWidthHalf.toFloat(),
            viewHeightHalf.toFloat() + 16f,
            buttonPaint
        )
    }

    companion object {
        const val TAG = "CustomNumberButton"
    }
}