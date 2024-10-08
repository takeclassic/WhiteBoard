package com.thinkers.whiteboard.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.utils.convertDpToPixel


class CustomNumberButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    var buttonColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var buttonTextColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var buttonText: String = "1"
        set(value) {
            field = value
            invalidate()
        }
    private var buttonPaint: Paint
    private var textBounds: Rect

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.custom_number_button_view)
        buttonPaint = Paint()
        textBounds = Rect()
        try {
            buttonColor = typedArray.getInt(
                R.styleable.custom_number_button_view_button_color,
                context.getColor(R.color.default_icon)
            )
            buttonTextColor = typedArray.getInt(
                R.styleable.custom_number_button_view_button_text_color,
                context.getColor(R.color.default_icon)
            )
            buttonText = typedArray.getString(R.styleable.custom_number_button_view_button_text)!!
        } catch (e: Exception) {

        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec);
        val widthSize = MeasureSpec.getSize(widthMeasureSpec);
        val heightMode = MeasureSpec.getMode(heightMeasureSpec);
        val heightSize = MeasureSpec.getSize(heightMeasureSpec);
        val w: Int
        val h: Int
        val desiredWidth = this.convertDpToPixel(60f)
        val desiredHeight = this.convertDpToPixel(60f)

        if (widthMode == MeasureSpec.EXACTLY) {
            w = widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            w = Math.min(desiredWidth, widthSize)
        } else {
            w = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            h = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            h = Math.min(desiredHeight, heightSize)
        } else {
            h = desiredHeight
        }

        setMeasuredDimension(w, h);
    }

    override fun onDraw(canvas: Canvas) {
        val viewWidthHalf = this.measuredWidth / 2
        val viewHeightHalf = this.measuredHeight / 2

        Log.i(TAG, "measuredWidth: $measuredWidth, half: $viewWidthHalf")
        Log.i(TAG, "measuredHeight: $measuredHeight, half: $viewHeightHalf")

        val radius = if (viewWidthHalf > viewHeightHalf) viewHeightHalf - 10 else viewWidthHalf - 10
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.isAntiAlias = true
        buttonPaint.color = buttonColor
        canvas.drawCircle(
            viewWidthHalf.toFloat(),
            viewHeightHalf.toFloat(),
            radius.toFloat(),
            buttonPaint
        )

        buttonPaint.color = buttonTextColor
        buttonPaint.textSize = 80f

        buttonPaint.getTextBounds(buttonText, 0, buttonText.length, textBounds)
        canvas.drawText(
            buttonText,
            viewWidthHalf - textBounds.exactCenterX(),
            viewHeightHalf - textBounds.exactCenterY(),
            buttonPaint
        )
    }

    companion object {
        const val TAG = "CustomNumberButton"
    }
}