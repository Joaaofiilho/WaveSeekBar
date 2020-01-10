package com.joaaoferreira.styledseekbar.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import com.joaaoferreira.styledseekbar.R
import kotlinx.android.synthetic.main.ssb_layout.view.*
import kotlin.math.absoluteValue
import kotlin.math.round

class WaveSeekBar @JvmOverloads constructor(
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

    var min: Int = 0
    set(value) {
        field = value

        seekbar.max = (max - field) * animationSmoothness

        if(personalizedList == null) {
            texts.clear()
            for (i in field..max) {
                texts.add(i.toString())
            }

            invalidate()
        }
    }

    var max: Int = 0
    set(value) {
        field = value

        seekbar.max = (field - min) * animationSmoothness

        if(personalizedList == null) {
            texts.clear()
            for (i in min..field) {
                texts.add(i.toString())
            }

            invalidate()
        }
    }

    var maxTextElevation: Float = 0F
        set(value) {
            field = value
            (seekbar.layoutParams as LayoutParams).setMargins(0, (textSize + field + textGap).toInt(), 0, 0)

            requestLayout()
        }

    var textGap: Float = 0F
        set(value) {
            field = value
            (seekbar.layoutParams as LayoutParams).setMargins(0, (textSize + maxTextElevation + field).toInt(), 0, 0)

            requestLayout()
        }

    var waveRange: Int = 0
    set(value) {
        field = value

        invalidate()
    }

    var waveMultiplier: Float = 0F
    set(value) {
        field = value
        invalidate()
    }

    var textSize: Float = 0F
    set(value) {
        field = value

        pencil.textSize = field

        val padding = (field/2).toInt()

        seekbar.setPaddingRelative(padding, 0, padding, 0)

        (seekbar.layoutParams as LayoutParams).setMargins(0, (maxTextElevation + field + textGap).toInt(), 0, 0)

        requestLayout()
    }

    var textColor: Int = -1
    set(value) {
        field = value
        pencil.color = field

        invalidate()
    }

    var thumbTint: Int = -1
        set(value) {
            field = value
            seekbar.thumb.setTint(thumbTint)
        }

    var progressTint: Int = -1
        set(value) {
            field = value

            @Suppress("DEPRECATION")
            seekbar.progressDrawable.setColorFilter(progressTint, PorterDuff.Mode.MULTIPLY)

            invalidate()
        }

    /* Private attributes */

    private val pencil = Paint().apply {
        textAlign = Paint.Align.CENTER
    }

    private var plMin: Int = 0
        set(value) {
            field = value
            seekbar.max = (plMax - field - 1) * animationSmoothness
        }

    private var plMax: Int = 0
        set(value) {
            field = value
            seekbar.max = (field - plMin - 1) * animationSmoothness
        }

    private val animationSmoothness: Int = 100

    private fun enableSeekbarTouch(enable: Boolean) {
        seekbar.setOnTouchListener { _, _ -> !enable }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.ssb_layout, this, true)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WaveSeekBar)

        attributes.let {
            maxTextElevation = it.getDimension(R.styleable.WaveSeekBar_wsb_maxTextElevation, 16F * resources.displayMetrics.density)
            textGap = it.getDimension(R.styleable.WaveSeekBar_wsb_textGap, 4F * resources.displayMetrics.density)
            min = it.getInteger(R.styleable.WaveSeekBar_wsb_min, 0)
            max = it.getInteger(R.styleable.WaveSeekBar_wsb_max, 10)
            textSize = it.getDimension(R.styleable.WaveSeekBar_wsb_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20F, resources.displayMetrics))
            textColor = it.getColor(R.styleable.WaveSeekBar_wsb_textColor, Color.BLACK)
            waveRange = it.getInteger(R.styleable.WaveSeekBar_wsb_waveRange, 1)
            waveMultiplier = it.getFloat(R.styleable.WaveSeekBar_wsb_waveMultiplier, 1F)
            thumbTint = it.getColor(R.styleable.WaveSeekBar_wsb_thumbTint, Color.BLACK)
            progressTint = it.getColor(R.styleable.WaveSeekBar_wsb_progressTint, Color.BLACK)
        }

        attributes.recycle()

        background = ColorDrawable(0)

        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                invalidate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekbar?.let { sb ->
                    if(sb.progress % animationSmoothness != 0) {
                        enableSeekbarTouch(false)
                        ValueAnimator.ofInt(seekbar.progress, round(seekbar.progress.toFloat() / animationSmoothness).toInt() * animationSmoothness)
                            .apply {
                                addUpdateListener {
                                    seekbar.progress = it.animatedValue as Int
                                }

                                addListener(object: AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                                        super.onAnimationEnd(animation, isReverse)
                                        enableSeekbarTouch(true)
                                        Log.v("PROGRESS", seekbar.progress.toString())
                                    }
                                })

                                duration = 100L
                                start()
                            }
                    }
                }
            }

        })
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            val list = personalizedList?.let { pl -> pl } ?: texts

            val step = (width.toFloat() - seekbar.paddingStart - seekbar.paddingEnd) / (list.size - 1)

            for(i in 0 until list.size) {
                val distanceFromValue = ((i * animationSmoothness - seekbar.progress) / animationSmoothness.toFloat()).absoluteValue
                var elevation = maxTextElevation * distanceFromValue / 2 * waveMultiplier

                if(elevation > maxTextElevation) {
                    elevation = maxTextElevation
                }

                pencil.alpha = (0xFF / (distanceFromValue + 1)).toInt()

                if(i == 1) {
                    Log.v("PROGRESS",((i * animationSmoothness - seekbar.progress) / animationSmoothness.toFloat()).toString())
                }

                if(distanceFromValue.absoluteValue <= waveRange + 0.5) {
                    it.drawText(list[i], seekbar.paddingStart + i*step, textSize + elevation, pencil)
                }
            }
        }
    }
}