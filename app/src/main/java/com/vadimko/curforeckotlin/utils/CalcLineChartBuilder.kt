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
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Utility class with configure and fill graph functions for Calc Fragment
 */
object CalcLineChartBuilder : KoinComponent {
    private val context: Context by inject()
    private val color = context.getColor(R.color.white)

    private val datesTime: MutableList<String> = mutableListOf()
    private val usdDataBuy: MutableList<Double> = mutableListOf()
    private val usdDataSell: MutableList<Double> = mutableListOf()
    private val eurDataBuy: MutableList<Double> = mutableListOf()
    private val eurDataSell: MutableList<Double> = mutableListOf()
    private val gbpDataBuy: MutableList<Double> = mutableListOf()
    private val gbpDataSell: MutableList<Double> = mutableListOf()


    /**
     * Creating and configuring the graph
     * @param chart takes chart to configure
     * @param data takes values to build axis X values
     * @param dataType define which type of [chart] and [data] used
     */
    @Suppress("UNCHECKED_CAST")
    fun createGraph(chart: LineChart, data: Any, dataType: Boolean): LineChart {
        val timeDate: MutableList<String> = mutableListOf()
        if (!dataType) {
            data as List<List<CurrencyTCS>>
            data.forEach {
                it[0].datetime?.let { it1 -> timeDate.add(DateConverter.longToDateWithTime(it1)) }
            }
        } else {
            data as List<Currencies>
            data.forEach {
                timeDate.add(it.dt)
            }
        }
        chart.clear()
        chart.setDrawGridBackground(false)
        chart.description.isEnabled = true
        val tempDescription = Description()
        tempDescription.text = "https://www.tinkoff.ru/"
        chart.description = tempDescription
        chart.setDrawBorders(true)
        chart.axisLeft.isEnabled = true
        chart.axisRight.setDrawAxisLine(true)
        chart.axisRight.setDrawGridLines(true)
        chart.xAxis.setDrawAxisLine(true)
        chart.xAxis.setDrawGridLines(true)
        chart.xAxis.labelRotationAngle = -45f
        val xAxis: XAxis = chart.xAxis
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if ((value < timeDate.size) && (value.toInt() >= 0)) timeDate[value.toInt()]
                else ""
            }
        }
        val rightAxis: YAxis = chart.axisRight
        rightAxis.setDrawGridLines(true)
        val leftAxis: YAxis = chart.axisLeft
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
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        val animationLong: Int = if (timeDate.size < 20) 200
        else 500
        chart.animateX(animationLong)
        val l: Legend = chart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        xAxis.textColor = color
        leftAxis.textColor = color
        rightAxis.textColor = color
        tempDescription.textColor = color
        l.textColor = color
        return chart
    }

    /**
     * Filling autoupdate graph with selected currency
     * @param lineChart takes chart to fill it with values
     * @param curr - spinner of selection currency position
     *  @param data takes values to fill chart with values
     * @param dataType define which type of [lineChart] and [data] used
     */
    @Suppress("UNCHECKED_CAST")
    fun fillChart(
        lineChart: LineChart,
        curr: Int,
        data: Any,
        dataType: Boolean
    ) {
        usdDataBuy.clear()
        usdDataSell.clear()
        eurDataBuy.clear()
        eurDataSell.clear()
        gbpDataBuy.clear()
        gbpDataSell.clear()
        datesTime.clear()
        if (!dataType) {
            data as List<List<CurrencyTCS>>
            data.forEach {
                it[0].buy?.let { it1 -> usdDataBuy.add(it1) }
                it[0].sell?.let { it1 -> usdDataSell.add(it1) }
                it[0].datetime?.let { it1 -> datesTime.add(DateConverter.longToDateWithTime(it1)) }
                it[1].buy?.let { it1 -> eurDataBuy.add(it1) }
                it[1].sell?.let { it1 -> eurDataSell.add(it1) }
                it[2].buy?.let { it1 -> gbpDataBuy.add(it1) }
                it[2].sell?.let { it1 -> gbpDataSell.add(it1) }
            }
        } else {
            data as List<Currencies>
            data.forEach {
                usdDataBuy.add(it.usdBuy)
                usdDataSell.add(it.usdSell)
                eurDataBuy.add(it.eurBuy)
                eurDataSell.add(it.eurSell)
                gbpDataBuy.add(it.gbpBuy)
                gbpDataSell.add(it.gbpSell)
                datesTime.add(it.dt)
            }
        }
        when (curr) {
            0 -> {
                fillLineChart(lineChart, "USD", usdDataBuy, usdDataSell)
            }
            1 -> {
                fillLineChart(lineChart, "EUR", eurDataBuy, eurDataSell)
            }
            2 -> {
                fillLineChart(lineChart, "GBP", gbpDataBuy, gbpDataSell)
            }
        }
    }

    private fun fillLineChart(
        lineChart: LineChart,
        curr: String,
        buy: List<Double>,
        sell: List<Double>
    ) {
        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        val buyEntries: MutableList<Entry> = mutableListOf()
        val sellEntries: MutableList<Entry> = mutableListOf()
        for (i in buy.indices) {
            buyEntries.add(Entry(i.toFloat(), buy[i].toFloat()))
            // Log.wtf("buy", i.toString())
        }
        val buyLineDataSet =
            LineDataSet(buyEntries, "$curr ${context.getString(R.string.CALCFRAGbuying)}")
        LinearDataSetsConfigure.configureLineDataSets(
            buyLineDataSet,
            false,
            55,
            70,
            170,
            0
        )
        dataSets.add(buyLineDataSet)

        for (i in sell.indices) {
            sellEntries.add(Entry(i.toFloat(), sell[i].toFloat()))
        }
        val sellLineDataSet =
            LineDataSet(sellEntries, "$curr ${context.getString(R.string.CALCFRAGselling)}")
        LinearDataSetsConfigure.configureLineDataSets(
            sellLineDataSet,
            false,
            240,
            70,
            55,
            0
        )
        dataSets.add(sellLineDataSet)
        val dateSet = LineData(dataSets)
        lineChart.data = dateSet
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }
}
