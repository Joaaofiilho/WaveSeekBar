package com.joaaoferreira.styledseekbar

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        with(ssb) {
//            min = -5
//            max = 5
//            textColor = Color.BLUE
//            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24F, resources.displayMetrics)
//            maxTextElevation = 24F * resources.displayMetrics.density
//            waveRange = 2
//            waveMultiplier = 1F
//        }

//        ssb.personalizedList = listOf("A", "B", "C", "D", "E")
    }
}
