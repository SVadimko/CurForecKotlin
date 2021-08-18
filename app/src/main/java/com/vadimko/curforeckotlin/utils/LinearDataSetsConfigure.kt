package com.vadimko.curforeckotlin.utils

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.data.LineDataSet
import com.vadimko.curforeckotlin.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Object with function configure common LineDataSets
 */
object LinearDataSetsConfigure: KoinComponent {

    private val context: Context by inject()

    /**
     * Configure common LineDataSets for LineCharts
     * @param dash - true- enable dash
     * @param mode - 0-linear graph, 1-stepped graph
     */
     fun configureLineDataSets(d: LineDataSet, dash: Boolean, colorR: Int, colorG: Int, colorB: Int, mode:Int){
         if(dash) d.enableDashedLine(10f, 10f, 0f)
        d.lineWidth = 2.5f
        d.circleRadius = 1f
        d.color = Color.rgb(colorR, colorG, colorB)
        d.valueTextColor =
            context.getColor(R.color.white)
        if(mode ==0)
            d.mode =
                if (d.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR
                else LineDataSet.Mode.HORIZONTAL_BEZIER
        else
            d.mode =
                if (d.mode == LineDataSet.Mode.STEPPED) LineDataSet.Mode.LINEAR else LineDataSet.Mode.STEPPED
    }
}