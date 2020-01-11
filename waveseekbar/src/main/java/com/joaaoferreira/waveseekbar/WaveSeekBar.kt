package com.joaaoferreira.waveseekbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
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

    /**
     * A personalized list to display the text.
     * If you listen to the progress, you'll recieve the index of the list instead of the actual number
     * An empty list will not change anything.*/
    var personalizedList: List<String>? = null
        set(value) {
            value?.let {
                if(it.isNotEmpty()) {
                    field = it
                    plMin = 0
                    plMax = it.size

                    updateSeekbarMaxValue()
                }
            } ?: run {
                field = value
                updateSeekbarMaxValue()
            }
        }

    /* Behavior attributes */

    var min: Int = 0
        set(value) {
            field = value

            texts.clear()
            for (i in field..max) {
                texts.add(i.toString())
            }

            updateSeekbarMaxValue()
        }

    var max: Int = 0
        set(value) {
            field = value

            texts.clear()
            for (i in min..field) {
                texts.add(i.toString())
            }

            updateSeekbarMaxValue()
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

    /**
     * This attribute controls how many progress are between two main progress points.
     * How high this number is, more smoothly the animations will run.
     * <i>Lower value = Better Performance</i>
     * <i>Higher value = Better Animations</i>
     *
     * <p><i>The default value is 100.</i></p>*/
    var animationSmoothness: Int = 100
        set(value) {
            field = value
            updateSeekbarMaxValue()
        }

    /* Design attributes */

    var maxTextElevation: Float = 0F
        set(value) {
            field = value
            (seekbar.layoutParams as LayoutParams).setMargins(seekbar.marginLeft, (textSize + field + textGap).toInt(), seekbar.marginRight , seekbar.marginBottom)

            requestLayout()
        }

    var textGap: Float = 0F
        set(value) {
            field = value
            (seekbar.layoutParams as LayoutParams).setMargins(seekbar.marginLeft, (textSize + maxTextElevation + field).toInt(), seekbar.marginRight, seekbar.marginBottom)

            requestLayout()
        }

    var textSize: Float = 0F
        set(value) {
            field = value

            pencil.textSize = field

            val padding = (field/2).toInt()

            seekbar.setPaddingRelative(padding, seekbar.paddingTop, padding, seekbar.paddingBottom)

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

    /* Listeners */
    private var _onProgressChanged: ((progress: Int, fromUser: Boolean) -> Unit)? = null
    private var _onStartTrackingTouch: ((progress: Int) -> Unit)? = null
    private var _onStopTrackingTouch: ((progress: Int) -> Unit)? = null

    /**
     * @param progress gives the actual number displayer. If you have a personalized
     * list, it gives the index of that list instead. */
    fun setOnProgressChanged(event: (progress: Int, fromUser: Boolean) -> Unit) {
        _onProgressChanged = event
    }

    /**
     * @param progress gives the actual number displayer. If you have a personalized
     * list, it gives the index of that list instead. */
    fun setOnStartTrackingTouch(event: (progress: Int) -> Unit) {
        _onStartTrackingTouch = event
    }

    /**
     * @param progress gives the actual number displayer. If you have a personalized
     * list, it gives the index of that list instead. */
    fun setOnStopTrackingTouch(event: (progress: Int) -> Unit) {
        _onStopTrackingTouch = event
    }

    /* Private attributes */

    private val pencil = Paint().apply {
        textAlign = Paint.Align.CENTER
    }

    private var plMin: Int = 0

    private var plMax: Int = 0

    private fun getRelativeProgress(): Int {
        var cleanProgress = round(seekbar.progress.toFloat() / animationSmoothness).toInt()
        if(personalizedList == null) {
            cleanProgress += min
        }
        return cleanProgress
    }

    private fun updateSeekbarMaxValue() {
        val _max: Int
        val _min: Int

        if(personalizedList == null) {
            _max = max
            _min = min
        } else {
            _max = plMax - 1
            _min = plMin
        }

        seekbar.max = (_max - _min) * animationSmoothness
    }

    private fun animateProgressTo(endValue: Int) {
        ValueAnimator.ofInt(seekbar.progress, endValue)
            .apply {
                addUpdateListener {
                    seekbar.progress = it.animatedValue as Int
                }

                duration = 200L
                start()
            }
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

        animationSmoothness = 100

        background = ColorDrawable(0)

        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                this@WaveSeekBar._onProgressChanged?.let {
                    it(getRelativeProgress(), fromUser)
                }

                invalidate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    this@WaveSeekBar._onStartTrackingTouch?.let {
                        it(getRelativeProgress())
                    }
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekbar?.let { sb ->

                    if(sb.progress % animationSmoothness != 0) {
                        val endValue = round(seekbar.progress.toFloat() / animationSmoothness).toInt() * animationSmoothness
                        animateProgressTo(endValue)
                    }

                    this@WaveSeekBar._onStopTrackingTouch?.let {
                        it(getRelativeProgress())
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
                if(distanceFromValue.absoluteValue <= waveRange + 0.5) {
                    var elevation = maxTextElevation * distanceFromValue / 2 * waveMultiplier

                    if(elevation > maxTextElevation) {
                        elevation = maxTextElevation
                    }

                    pencil.alpha = (0xFF / (distanceFromValue + 1)).toInt()

                    it.drawText(list[i], seekbar.paddingStart + i*step, textSize + elevation, pencil)
                }
            }
        }
    }
}