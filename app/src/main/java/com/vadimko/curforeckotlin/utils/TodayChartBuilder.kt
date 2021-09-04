package com.vadimko.curforeckotlin.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.forecastsMethods.WMA
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Utility class with configure and fill graph functions for Today Fragment
 */
object TodayChartBuilder : KoinComponent {

    private val context: Context by inject()
    private val color = context.getColor(R.color.white)

    /**
     * Configure a combined candlestick and line chart
     * @param comboChartForec - takes chart to configure
     * @param dataList used to build x Axis values
     * @param period used to add to forecast x Axis values type of period
     * @param animationLong how lasts animation of chart drawing
     */
    fun createChart(
        comboChartForec: CombinedChart,
        dataList: List<CurrencyMOEX>,
        period: String,
        animationLong: Int,
    ): CombinedChart {
        val dates: MutableList<String> = mutableListOf()
        dataList.forEach {
            dates.add(it.dates)
        }
        for (i in 1 until 4) {
            dates.add(
                "${context.getString(R.string.forec)} +$period"
            )
        }
        comboChartForec.clear()
        comboChartForec.invalidate()
        comboChartForec.setDrawGridBackground(false)
        comboChartForec.description.isEnabled = true
        val tempDescription = Description()
        tempDescription.text = "iss.moex.com"
        comboChartForec.description = tempDescription
        comboChartForec.setDrawBorders(true)
        comboChartForec.axisLeft.isEnabled = true
        comboChartForec.axisRight.setDrawAxisLine(true)
        comboChartForec.axisRight.setDrawGridLines(true)
        comboChartForec.xAxis.setDrawAxisLine(true)
        comboChartForec.xAxis.setDrawGridLines(true)
        comboChartForec.xAxis.labelRotationAngle = -45f
        val xAxis: XAxis = comboChartForec.xAxis
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        val rightAxis: YAxis = comboChartForec.axisRight
        rightAxis.setDrawGridLines(true)
        val leftAxis: YAxis = comboChartForec.axisLeft
        leftAxis.setDrawGridLines(true)
        comboChartForec.drawOrder = arrayOf(
            CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        )
        val l: Legend = comboChartForec.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < dates.size) dates[value.toInt()] else ""
            }
        }
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value) + " ₽"
            }
        }
        rightAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value) + " ₽"
            }
        }
        xAxis.textColor = color
        leftAxis.textColor = color
        rightAxis.textColor = color
        tempDescription.textColor =
            color
        l.textColor = color
        comboChartForec.setTouchEnabled(true)
        comboChartForec.isDragEnabled = true
        comboChartForec.setScaleEnabled(true)
        comboChartForec.animateX(animationLong)
        comboChartForec.setPinchZoom(true)
        return comboChartForec
    }


    /**
     * Fill the [comboChartForec] with data
     * @param dataList takes values of [CurrencyMOEX] to fill the graphs
     * @param comboChartForec takes chart to fill it with [dataList] of [CurrencyMOEX]
     * @param s used to name the graphs
     */
    fun fillComboChartForecast(
        comboChartForec: CombinedChart,
        s: String,
        dataList: List<CurrencyMOEX>,
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val dates: MutableList<String> = mutableListOf()
            val open: MutableList<Double> = mutableListOf()
            val close: MutableList<Double> = mutableListOf()
            val low: MutableList<Double> = mutableListOf()
            val high: MutableList<Double> = mutableListOf()
            val warprice: MutableList<Double> = mutableListOf()
            val datesForecast: MutableList<String> = mutableListOf()
            dates.clear()
            open.clear()
            close.clear()
            high.clear()
            low.clear()
            warprice.clear()
            datesForecast.clear()
            dataList.forEach {
                dates.add(it.dates)
                open.add(it.open)
                close.add(it.close)
                high.add(it.high)
                low.add(it.low)
                warprice.add(it.warprice)
                datesForecast.add(it.dates)
            }
            val data = CombinedData()
            data.setData(generateLineData(s, warprice))
            data.setData(generateCandleData(s, open, close, high, low))
            withContext(Dispatchers.Main) {
                comboChartForec.data = data
                comboChartForec.notifyDataSetChanged()
                comboChartForec.invalidate()
            }
        }
    }

    /**
     * Prepare dataSets for linear graph with values
     */
    private fun generateLineData(s: String, warprice: List<Double>): LineData {
        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        val entries:
                MutableList<Entry> = mutableListOf()
        for (i in warprice.indices) if (warprice[i] != 0.0) {
            entries.add(
                Entry(
                    i.toFloat(),
                    warprice[i].toFloat()
                )
            )
        }
        val d = LineDataSet(entries, "$s ${context.getString(R.string.ARCFRAGaverage)}")
        LinearDataSetsConfigure.configureLineDataSets(d, false, 23, 100, 255, 0)
        dataSets.add(d)

        val entries2:
                MutableList<Entry> = mutableListOf()
        val warpFlt: MutableList<Float> = mutableListOf()
        for (i in warprice.indices) {
            warpFlt.add(warprice[i].toFloat())
        }
        if (warprice.size > 3) {
            val temp = WMA(warpFlt, 3)
            temp.calc()
            val forecast: MutableList<Float> = temp.getForecast()
            for (i in forecast.indices) {
                entries2.add(
                    Entry(
                        (i + warprice.size - 1).toFloat(),
                        forecast[i]
                    )
                )
            }
            val d2 = LineDataSet(
                entries2,
                "$s ${context.getString(R.string.ARCFRAGmovAverForecGraph)}  ${temp.getAverageErr()}  %"
            )
            LinearDataSetsConfigure.configureLineDataSets(d2, true, 40, 120, 230, 0)
            dataSets.add(d2)
        }
        return LineData(dataSets)
    }

    /**
     * Prepare dataSets for candle graph
     */
    private fun generateCandleData(
        s: String,
        open: List<Double>,
        close: List<Double>,
        high: List<Double>,
        low: List<Double>
    ): CandleData {
        val yValsCandleStick = ArrayList<CandleEntry>()
        yValsCandleStick.clear()
        for (i in open.indices) {
            if (open[i] != 0.0)
                if (close[i] != 0.0) {
                    yValsCandleStick.add(
                        CandleEntry(
                            i.toFloat(),
                            high[i].toFloat(),
                            low[i].toFloat(),
                            open[i].toFloat(),
                            close[i].toFloat()
                        )
                    )
                }
        }
        val set1 =
            CandleDataSet(yValsCandleStick, "$s ${context.getString(R.string.TODAYFRAGMMVB)}")
        set1.color = Color.rgb(80, 80, 80)
        set1.shadowWidth = 2f
        set1.valueTextColor = color
        set1.decreasingColor = Color.rgb(220, 60, 78)
        set1.decreasingPaintStyle = Paint.Style.FILL
        set1.increasingColor = Color.rgb(60, 220, 78)
        set1.increasingPaintStyle = Paint.Style.FILL
        set1.neutralColor = color
        set1.setDrawValues(true)
        set1.shadowColorSameAsCandle = true
        return CandleData(set1)
    }
}