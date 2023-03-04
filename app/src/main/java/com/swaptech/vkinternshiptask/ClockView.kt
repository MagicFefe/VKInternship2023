package com.swaptech.vkinternshiptask

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.*
import kotlin.math.min


class ClockView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private var hour = 0
    private var minute = 0
    private var second = 0

    private val clockWidth = min(
        resources.displayMetrics.widthPixels,
        resources.displayMetrics.heightPixels
    )
    private val clockHeight = clockWidth
    private val scope = MainScope() + SupervisorJob()
    private val stepSecond = 360f / 60f
    private val stepMinute = 360f / 60f
    private val stepHour = 360f / 12f

    private val dialDrawable by lazy {
        context.scaledDrawable(
            R.drawable.dial,
            width,
            height
        )
    }
    private val hourHandDrawable by lazy {
        context.scaledDrawable(
            R.drawable.minute_hand,
            (width * ERROR).toInt(),
            (height / (2.0 + DIAL_GAP_PERCENT)).toInt()
        )
    }
    private val minuteHandDrawable by lazy {
        context.scaledDrawable(
            R.drawable.minute_hand,
            (width * ERROR).toInt(),
            (height / (2.0 + DIAL_GAP_PERCENT)).toInt()
        )
    }
    private val secondHandDrawable by lazy {
        context.scaledDrawable(
            R.drawable.second_hand,
            (width * ERROR).toInt(),
            (height / (2.0 + DIAL_GAP_PERCENT)).toInt()
        )
    }

    init {
        startReceivingTime()
    }

    private fun startReceivingTime() {
        scope.launch {
            while (true) {
                delay(1000)
                val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
                hour = calendar.get(Calendar.HOUR)
                minute = calendar.get(Calendar.MINUTE)
                second = calendar.get(Calendar.SECOND)
                invalidate()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val measuredWidth = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                clockWidth
            }
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                clockWidth.coerceAtMost(widthSize)
            }
            else -> error("Cannot measure width")
        }
        val measuredHeight = when (heightMode) {
            MeasureSpec.UNSPECIFIED -> {
                clockHeight
            }
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                clockHeight.coerceAtMost(heightSize)
            }
            else -> error("Cannot measure height")
        }
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        with(canvas) {
            drawDial()
            withSave {
                drawHourHand(stepHour * hour)
            }
            withSave {
                drawMinuteHand(stepMinute * minute)
            }
            withSave {
                drawSecondHand(stepSecond * second)
            }
        }
    }

    private fun Canvas.drawDial() {
        with(dialDrawable) {
            setBounds(0, 0, width, height)
            draw(this@drawDial)
        }
    }

    private fun Canvas.drawHourHand(angle: Float = 0f) {
        with(
            hourHandDrawable
        ) {
            withSave {
                setBounds(
                    ((width / 2) - intrinsicWidth / 2 - (width * ERROR) / 2).toInt(),
                    (height * DIAL_GAP_PERCENT * 2.5 + height * ERROR).toInt(),
                    ((width / 2) + intrinsicWidth / 2 + (width * ERROR) / 2).toInt(),
                    ((height / 2) + (height * ERROR)).toInt()
                )
                rotate(angle, (width / 2f), (height / 2f))
                draw(this@drawHourHand)
            }
        }
    }

    private fun Canvas.drawMinuteHand(angle: Float = 0f) {
        with(
            minuteHandDrawable
        ) {
            withSave {
                setBounds(
                    ((width / 2) - intrinsicWidth / 2 - (width * ERROR) / 2).toInt(),
                    (height * DIAL_GAP_PERCENT + height * ERROR).toInt(),
                    ((width / 2) + intrinsicWidth / 2 + (width * ERROR) / 2).toInt(),
                    ((height / 2) + (height * ERROR)).toInt()
                )
                rotate(angle, (width / 2f), (height / 2f))
                draw(this@drawMinuteHand)
            }
        }
    }

    private fun Canvas.drawSecondHand(angle: Float = 0f) {
        with(
            secondHandDrawable
        ) {
            withSave {
                setBounds(
                    ((width / 2) - intrinsicWidth / 2 - (width * ERROR) / 2).toInt(),
                    ((height / 2) - intrinsicHeight + (height * ERROR)).toInt(),
                    ((width / 2) + intrinsicWidth / 2 + (width * ERROR) / 2).toInt(),
                    ((height / 2) + (height * ERROR)).toInt()
                )
                rotate(angle, (width / 2f), (height / 2f))
                draw(this@drawSecondHand)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }

    companion object {
        private const val ERROR = 0.03
        private const val DIAL_GAP_PERCENT = 0.1
    }
}
