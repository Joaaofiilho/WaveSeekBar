package com.joaaoferreira.styledseekbar.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import com.joaaoferreira.styledseekbar.R
import kotlinx.android.synthetic.main.ssb_layout.view.*
import kotlin.math.absoluteValue

class StyledSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var texts = mutableListOf<String>()

    /** A personalized list to display the text.
     * An empty list will not change anything.*/
    var personalizedList: List<String>? = null
    set(value) {
        value?.let {
            if(it.isNotEmpty()) {
                field = it
                plMin = 0
                plMax = it.size

                invalidate()
            }
        } ?: run {
            field = value
            min = min
            max = max

            invalidate()
        }
    }

    private var plMin: Int = 0
    set(value) {
        field = value
        seekbar.max = plMax - field - 1
    }

    private var plMax: Int = 0
    set(value) {
        field = value
        seekbar.max = field - plMin - 1
    }

    var min: Int = 0
    set(value) {
        field = value

        seekbar.max = max - field - 1

        if(personalizedList == null) {
            texts.clear()
            for (i in field until max) {
                texts.add(i.toString())
            }

            invalidate()
        }
    }

    var max: Int = 0
    set(value) {
        field = value

        seekbar.max = field - min - 1

        if(personalizedList == null) {
            texts.clear()
            for (i in min until field) {
                texts.add(i.toString())
            }

            invalidate()
        }
    }

    var maxTextElevation: Float = 32F
        set(value) {
            field = value
            (seekbar.layoutParams as LayoutParams).setMargins(0, (textSize + field).toInt(), 0, 0)

            invalidate()
        }

    var distorcionRange: Int = 0
    set(value) {
        field = value

        invalidate()
    }

    var textSize: Float = 0F
    set(value) {
        field = value

        pencil.textSize = field

        val padding = (field).toInt()
        seekbar.setPaddingRelative(padding, 0, padding, 0)
        (seekbar.layoutParams as LayoutParams).setMargins(0, (maxTextElevation + field).toInt(), 0, 0)

        invalidate()
    }

    var textColor: Int = -1
    set(value) {
        field = value
        pencil.color = field

        invalidate()
    }

    /* Private attributes */

    private val pencil = Paint().apply {
        textAlign = Paint.Align.CENTER
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.ssb_layout, this, true)

        maxTextElevation = 32F
        min = 1
        max = 10
        textSize = 32F
        textColor = Color.BLACK
        distorcionRange = 1

        background = ColorDrawable(0xFF00FF00.toInt())

        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                invalidate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas?.let {
            val list = personalizedList?.let { pl -> pl } ?: texts

            val step = (width.toFloat() - seekbar.paddingStart - seekbar.paddingEnd) / (list.size - 1)

            val textIndex = seekbar.progress

            for(i in 0 until list.size) {
                val distanceFromValue = (i - textIndex).absoluteValue
                val elevation = maxTextElevation - maxTextElevation / (distanceFromValue + 1)

                pencil.alpha = 0xFF / (distanceFromValue + 1)

                if(distanceFromValue <= distorcionRange) {
                    it.drawText(list[i], seekbar.paddingStart + step * i, textSize + elevation, pencil)
                }
            }
        }
    }
}