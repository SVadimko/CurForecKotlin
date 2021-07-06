package com.vadimko.curforeckotlin.ui.today

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.vadimko.curforeckotlin.DateConverter
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.databinding.FragmentTodayBinding
import com.vadimko.curforeckotlin.forecastsMethods.WMA
import com.vadimko.curforeckotlin.moexapi.CurrencyMOEX
import com.vadimko.curforeckotlin.prefs.TodayPreferences
import com.vadimko.curforeckotlin.updateWorkers.TodayWorker
import java.util.*


class TodayFragment : Fragment() {

    private var dates: MutableList<String> = mutableListOf()
    private var open: MutableList<Double> = mutableListOf()
    private var close: MutableList<Double> = mutableListOf()
    private var low: MutableList<Double> = mutableListOf()
    private var high: MutableList<Double> = mutableListOf()
    private var warprice: MutableList<Double> = mutableListOf()
    private var datesForecast: MutableList<String> = mutableListOf()


    private lateinit var comboChartForec: CombinedChart
    private val yValsCandleStick2 = ArrayList<CandleEntry>()

    private val todayViewModel: TodayViewModel by lazy {
        ViewModelProvider(this).get(TodayViewModel::class.java)
    }

    private lateinit var root: View


    private var _binding: FragmentTodayBinding? = null
    private var choosenCurrency = ""
    private var choosenPeriod = ""
    private var choosenRate = ""
    private lateinit var currSpinner: Spinner
    private lateinit var perSpinner: Spinner
    private lateinit var rateSpinner: Spinner

    private var recCur = ""
    private var recDay = "0"

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        root = binding.root


        val loadPrefs = context?.let { TodayPreferences.loadPrefs(it) }

        currSpinner = root.findViewById(R.id.curr_choose)
        val currAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout_main,
            resources.getStringArray(R.array.currency)
        )
        currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
        // currAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currSpinner.apply {
            adapter = currAdapter
            setSelection(loadPrefs?.get(4)!!.toInt(), true)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    position: Int, id: Long
                ) {
                    choosenCurrency = currSpinner.getItemAtPosition(position) as String
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }
        perSpinner = root.findViewById(R.id.period_choose)
        val perAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout_main,
            resources.getStringArray(R.array.period)
        )
        perAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
        perSpinner.apply {
            adapter = perAdapter
            setSelection(loadPrefs?.get(5)!!.toInt(), true)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    position: Int, id: Long
                ) {
                    choosenPeriod = perSpinner.getItemAtPosition(position) as String


                    if ((position > 0) and (rateSpinner.selectedItemPosition == 0))
                        rateSpinner.setSelection(1)
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }
        rateSpinner = root.findViewById(R.id.rate_choose)
        val rateAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout_main,
            resources.getStringArray(R.array.rate)
        )
        rateAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
        rateSpinner.apply {
            adapter = rateAdapter
            setSelection(loadPrefs?.get(6)!!.toInt(), true)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    position: Int, id: Long
                ) {
                    choosenRate = rateSpinner.getItemAtPosition(position) as String
                    if ((position == 0) and (perSpinner.selectedItemPosition > 0)) perSpinner.setSelection(
                        0
                    )
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }

        val button = root.findViewById<View>(R.id.build_graph) as Button
        button.apply {
            setOnClickListener {
                val choosen = IntArray(3)
                choosen[0] = currSpinner.selectedItemPosition
                choosen[1] = perSpinner.selectedItemPosition
                choosen[2] = rateSpinner.selectedItemPosition
                createRequestStrings(choosen)
            }
        }
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val settings = Intent(activity, SettingsActivity::class.java)
                startActivity(settings)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //подписываемся на данные с МБ
        todayViewModel.getData().observe(viewLifecycleOwner, { forecMOEX ->
            forecMOEX?.let {
                extractData(forecMOEX)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //выбор параметров запроса в зависимости от состояний спиннера
    private fun createRequestStrings(choosen: IntArray) {
        var jsonCurr = ""
        val jsonDate: Array<String>
        var recDays = 0L
        var rates = 0
        when (choosen[0]) {
            0 -> {
                jsonCurr = "USD000000TOD"
                recCur = "USD"
            }
            1 -> {
                jsonCurr = "EUR_RUB__TOD"
                recCur = "EUR"
            }
            2 -> {
                jsonCurr = "GBPRUB_TOD"
                recCur = "GBP"
            }
        }
        when (choosen[1]) {
            0 -> {
                recDays = 1
                recDay = "1 день"
            }
            1 -> {
                recDays = 2
                recDay = "2 дня"
            }
            2 -> {
                recDays = 3
                recDay = "3 дня"
            }
            3 -> {
                recDays = 4
                recDay = "4 дня"
            }
            4 -> {
                recDays = 5
                recDay = "5 дней"
            }
        }
        when (choosen[2]) {
            0 -> rates = 1
            1 -> rates = 10
            2 -> rates = 60
        }
        val till = Date(System.currentTimeMillis())
        val from = Date(System.currentTimeMillis() - 86400000 * recDays)
        //val result = dateConverter(recDays)
        val result = DateConverter.getFromTillDate(from,till,requireContext())
        jsonDate = result[0]
        startTodayWorker(jsonCurr, jsonDate[0], jsonDate[1], rates.toString())
        context?.let {
            TodayPreferences.savePrefs(
                it,
                jsonCurr,
                jsonDate[0],
                jsonDate[1],
                rates.toString(),
                currSpinner.selectedItemPosition,
                perSpinner.selectedItemPosition,
                rateSpinner.selectedItemPosition
            )
        }
    }


    //конфигурируем и запускаем воркер для обновления данных за указанный период
    private fun startTodayWorker(request: String, from: String, till: String, interval: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data =
            workDataOf("request" to request, "from" to from, "till" to till, "interval" to interval)
        //val data2 = workDataOf("from" to from)
        //val data3 = workDataOf("till" to till)
        //val data4 = workDataOf("interval" to interval)
        val workManager = context?.let { WorkManager.getInstance(it) }
        val myWorkRequest = OneTimeWorkRequest.Builder(
            TodayWorker::class.java//,
        )
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager?.enqueue(myWorkRequest)
    }

    //извлекаем полученные данные
    private fun extractData(dataList: List<CurrencyMOEX>) {
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

        for (i in 1 until 3) {
            datesForecast.add("Прогноз + ${rateSpinner.getItemAtPosition(rateSpinner.selectedItemPosition)}")
        }
        createComboChartForecast()

    }

    //создаем комбинированный график для свечей и линии
    private fun createComboChartForecast() {
        comboChartForec = root.findViewById(R.id.candlforec)
        comboChartForec.clear()
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
            DrawOrder.CANDLE, DrawOrder.LINE
        )
        val l: Legend = comboChartForec.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < datesForecast.size) datesForecast[value.toInt()] else ""
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
        xAxis.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        leftAxis.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        rightAxis.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        tempDescription.textColor =
            resources.getColor(R.color.white, requireActivity().application.theme)
        l.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        comboChartForec.setTouchEnabled(true)
        comboChartForec.isDragEnabled = true
        comboChartForec.setScaleEnabled(true)
        val animationLong =
            5 * Integer.parseInt((perSpinner.selectedItem as String).split(" ")[0]) * Integer.parseInt(
                (rateSpinner.selectedItem as String).split(" ")[0]
            )
        comboChartForec.animateX(animationLong)
        comboChartForec.setPinchZoom(true)

        if (datesForecast.size > 6) {
            fillComboChartForecast((currSpinner.selectedItem as String))
        } else {
            Toast.makeText(
                context,
                getString(R.string.TODAYFRAGchoosediffinterval),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //функция, вызывающая функции заполнения графика данными
    private fun fillComboChartForecast(s: String) {
        val data = CombinedData()
        data.setData(generateLineData(s))
        data.setData(generateCandleData(s))
        comboChartForec.data = data
        comboChartForec.invalidate()
    }

    //функция заполнения линейного графика данными
    private fun generateLineData(s: String): LineData {
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
                "s ${getString(R.string.ARCFRAGmovAverForecGraph)}  ${temp.getAverageErr()}  %"
            )
            d2.enableDashedLine(10f, 10f, 0f)

            d2.lineWidth = 2.5f
            d2.circleRadius = 1f
            d2.color = Color.rgb(40, 120, 230)
            d2.valueTextColor =
                resources.getColor(R.color.white, requireActivity().application.theme)
            d2.mode =
                if (d2.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
            dataSets.add(d2)
        }
        val d = LineDataSet(entries, "$s ${getString(R.string.ARCFRAGaverage)}")
        d.lineWidth = 2.5f
        d.circleRadius = 1f
        d.color = Color.rgb(23, 100, 255)
        d.valueTextColor = resources.getColor(R.color.white, requireActivity().application.theme)
        d.mode =
            if (d.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d)
        return LineData(dataSets)
    }

    //функция заполнения графика свечей
    private fun generateCandleData(s: String): CandleData {
        yValsCandleStick2.clear()
        for (i in 0 until open.size) {
            if (open[i] != 0.0)
                if (close[i] != 0.0) {
                    yValsCandleStick2.add(
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
        val set1 = CandleDataSet(yValsCandleStick2, "$s ${getString(R.string.TODAYFRAGMMVB)}")
        set1.color = Color.rgb(80, 80, 80)
        set1.shadowWidth = 2f
        set1.valueTextColor = resources.getColor(R.color.white, requireActivity().application.theme)
        set1.decreasingColor = Color.rgb(220, 60, 78)
        set1.decreasingPaintStyle = Paint.Style.FILL
        set1.increasingColor = Color.rgb(60, 220, 78)
        set1.increasingPaintStyle = Paint.Style.FILL
        set1.neutralColor = resources.getColor(R.color.white, requireActivity().application.theme)
        set1.setDrawValues(true)
        set1.shadowColorSameAsCandle = true
        return CandleData(set1)
    }


}