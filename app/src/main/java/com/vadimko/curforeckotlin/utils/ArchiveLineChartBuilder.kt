package com.vadimko.curforeckotlin.utils

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbxmlApi.CurrencyCBarhive
import com.vadimko.curforeckotlin.forecastsMethods.ExponentSmooth
import com.vadimko.curforeckotlin.forecastsMethods.LessSquare
import com.vadimko.curforeckotlin.forecastsMethods.WMA
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Utility class to configure and fill linear charts for Archive fragment
 */
object ArchiveLineChartBuilder : KoinComponent {
    private val context: Context by inject()
    private val color = context.getColor(R.color.white)

    /**
     * Configure lineChart
     * @param linearChart takes [LineChart] to configure it
     * @param data list with dataclass [CurrencyCBarhive] or [CurrencyMOEX]
     * @param description depends if it equal "cbr.ru" or not fun cast [data] to list of [CurrencyCBarhive] or [CurrencyMOEX]
     */
    @Suppress("UNCHECKED_CAST")
    fun createLineChart(
        linearChart: LineChart,
        data: List<Any>,
        description: String
    ): LineChart {
        val dates = mutableListOf<String>()
        if (description == "cbr.ru") {
            data as List<CurrencyCBarhive>
            dates.clear()
            data.forEach {
                dates.add(it.datetimeConv)
            }
        } else {
            data as List<CurrencyMOEX>
            dates.clear()
            data.forEach {
                dates.add(it.dates.split(" ")[0])
            }
            for (i in 1 until 4) {
                dates.add("${context.getString(R.string.ARCFRAGforecDayGraph)}  $i")
            }
        }
        linearChart.clear()
        linearChart.setDrawGridBackground(false)
        linearChart.description.isEnabled = true
        val chartDescription = Description()
        chartDescription.text = description
        linearChart.description = chartDescription
        linearChart.setDrawBorders(true)
        linearChart.axisLeft.isEnabled = true
        linearChart.axisRight.setDrawAxisLine(true)
        linearChart.axisRight.setDrawGridLines(true)
        linearChart.xAxis.setDrawAxisLine(true)
        linearChart.xAxis.setDrawGridLines(true)
        linearChart.xAxis.labelRotationAngle = -45f
        val xAxis: XAxis = linearChart.xAxis
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < dates.size) dates[value.toInt()] else ""
            }
        }
        val rightAxis: YAxis = linearChart.axisRight
        rightAxis.setDrawGridLines(true)
        val leftAxis: YAxis = linearChart.axisLeft
        leftAxis.setDrawGridLines(true)
        rightAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value) + " ₽"
            }
        }
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value) + " ₽"
            }
        }
        linearChart.setTouchEnabled(true)
        linearChart.isDragEnabled = true
        linearChart.setScaleEnabled(true)
        linearChart.setPinchZoom(true)
        val animationLong = 5 * dates.size
        linearChart.animateX(animationLong)
        val l: Legend = linearChart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        xAxis.textColor = color
        leftAxis.textColor = color
        rightAxis.textColor = color
        chartDescription.textColor = color
        l.textColor = color
        return linearChart
    }


    /**
     * Prepare dataSets for CB graph
     * @param linearCbrf takes CB [LineChart] to fill it with [dataListCB]
     * @param dataListCB values to fill the [linearCbrf]
     * @param s - currency name
     */
    fun fillClearCbrfGraph(linearCbrf: LineChart, s: String, dataListCB: List<CurrencyCBarhive>) {
        val dataSets = ArrayList<ILineDataSet>()
        val entries = ArrayList<Entry>()
        for (i in dataListCB.indices) {
            val str: String = dataListCB[i].offCur.replace(',', '.')
            entries.add(Entry(i.toFloat(), str.toFloat()))
        }
        val d = LineDataSet(entries, "$s ${context.getString(R.string.ARCFRAGcursCBGraphLabel)}")
        LinearDataSetsConfigure.configureLineDataSets(d, false, 32, 90, 200, 1)
        dataSets.add(d)
        linearCbrf.data = LineData(dataSets)
        linearCbrf.notifyDataSetChanged()
        linearCbrf.invalidate()
    }

    /**
     * Prepare dataSets for Moex graph
     * @param linearChartForec takes MOEX [LineChart] to fill it with [dataListMOEX]
     * @param dataListMOEX values to fill the [linearChartForec]
     * @param s - currency name
     */
    fun fillLinearSetForecast(
        linearChartForec: LineChart,
        s: String,
        dataListMOEX: List<CurrencyMOEX>
    ) {
        val warprice: MutableList<Float> = mutableListOf()
        warprice.clear()
        dataListMOEX.forEach {
            warprice.add(it.warprice.toFloat())
        }
        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        val warpFlt: MutableList<Float> = mutableListOf()
        for (i in warprice.indices) {
            warpFlt.add(warprice[i])
        }

        val entriesWMA: MutableList<Entry> = mutableListOf()
        for (i in warprice.indices) {
            entriesWMA.add(Entry(i.toFloat(), warprice[i]))
        }
        val entriesWMAForecast: MutableList<Entry> = mutableListOf()
        val utilWMA = WMA(warpFlt, 3)
        utilWMA.calc()
        val wmaError = utilWMA.getAverageErr().toString()
        val forecastWMA: MutableList<Float> = utilWMA.getForecast()
        for (i in forecastWMA.indices) {
            entriesWMAForecast.add(
                Entry(
                    (i + warprice.size - 1).toFloat(),
                    forecastWMA[i]
                )
            )
        }
        warpFlt.clear()
        for (i in warprice.indices) {
            warpFlt.add(warprice[i])
        }
        val entriesSmooth: MutableList<Entry> = mutableListOf()
        val utilSmooth = ExponentSmooth(warpFlt)
        utilSmooth.calc()
        val smooth: MutableList<Float> = utilSmooth.getSmooth()
        for (i in smooth.indices) {
            entriesSmooth.add(Entry(i.toFloat(), smooth[i]))
        }
        val entriesSmoothForecast: MutableList<Entry> = mutableListOf()
        val forecast1: MutableList<Float> = utilSmooth.getForecast()
        for (i in forecast1.indices) {
            entriesSmoothForecast.add(
                Entry(
                    ((i + smooth.size - 1).toFloat()),
                    forecast1[i]
                )
            )
        }

        warpFlt.clear()
        for (i in warprice.indices) {
            warpFlt.add(warprice[i])
        }
        val entriesLessSquare: MutableList<Entry> = mutableListOf()
        val utilLessSquare = LessSquare(warpFlt)
        utilLessSquare.calc()
        val square: MutableList<Float> = utilLessSquare.getOutputVal()
        for (i in square.indices) {
            entriesLessSquare.add(Entry(i.toFloat(), square[i]))
        }
        val entriesLessSquareForecast: MutableList<Entry> = mutableListOf()
        val forecastLessSquare: MutableList<Float> = utilLessSquare.getForecastVal()
        for (i in forecastLessSquare.indices) {
            entriesLessSquareForecast.add(
                Entry(
                    (i + square.size - 1).toFloat(),
                    forecastLessSquare[i]
                )
            )
        }


        val lineDataSetWMA =
            LineDataSet(entriesWMA, "$s ${context.getString(R.string.ARCFRAGaverage)} ")
        LinearDataSetsConfigure.configureLineDataSets(
            lineDataSetWMA,
            false,
            23,
            100,
            255,
            0
        )
        dataSets.add(lineDataSetWMA)

        val lineDataSetWMAForecast = LineDataSet(
            entriesWMAForecast,
            "$s ${context.getString(R.string.ARCFRAGmovAverForecGraph)} $wmaError %"
        )
        LinearDataSetsConfigure.configureLineDataSets(
            lineDataSetWMAForecast,
            true,
            40,
            120,
            230,
            0
        )
        dataSets.add(lineDataSetWMAForecast)


        val lineDataSetSmooth =
            LineDataSet(entriesSmooth, "$s ${context.getString(R.string.ARCFRAGexponWeighGraph)}")
        LinearDataSetsConfigure.configureLineDataSets(
            lineDataSetSmooth,
            false,
            210,
            80,
            90,
            0
        )
        dataSets.add(lineDataSetSmooth)

        val lineDataSetSmoothForecast = LineDataSet(
            entriesSmoothForecast,
            "$s  ${context.getString(R.string.ARCFRAGexponWeighForecGraph)} ${utilSmooth.getErrSmooth()}%"
        )
        LinearDataSetsConfigure.configureLineDataSets(
            lineDataSetSmoothForecast,
            true,
            170,
            100,
            100,
            0
        )
        dataSets.add(lineDataSetSmoothForecast)


        val lineDataSetLessSquare =
            LineDataSet(entriesLessSquare, "$s ${context.getString(R.string.ARCFRAGleastSqMeth)}")
        LinearDataSetsConfigure.configureLineDataSets(
            lineDataSetLessSquare,
            false,
            130,
            210,
            120,
            0
        )
        dataSets.add(lineDataSetLessSquare)

        val lineDataSetLessSquareForecast = LineDataSet(
            entriesLessSquareForecast,
            "$s  ${context.getString(R.string.ARCFRAGleastSqMethGraph)} ${utilLessSquare.getErrVal()} %"
        )
        LinearDataSetsConfigure.configureLineDataSets(
            lineDataSetLessSquareForecast,
            true,
            100,
            170,
            80,
            0
        )
        dataSets.add(lineDataSetLessSquareForecast)


        val dateSet = LineData(dataSets)
        linearChartForec.data = dateSet
        linearChartForec.notifyDataSetChanged()
        val animationLong = 5 * warprice.size
        linearChartForec.animateX(animationLong)
        linearChartForec.invalidate()
    }
}