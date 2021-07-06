package com.vadimko.curforeckotlin.ui.calc

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
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
import com.vadimko.curforeckotlin.Saver
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.databinding.FragmentCalcBinding
import java.text.SimpleDateFormat
import java.util.*

/*
private const val USD_BUY = "usdbuy"
private const val USD_SELL = "usdsell"
private const val EUR_BUY = "eurbuy"
private const val EUR_SELL = "eursell"
private const val GBP_BUY = "gbpbuy"
private const val GBP_SELL = "gbpsell"
*/

class CalcFragment : Fragment() {
    private lateinit var linearTCs: LineChart
    private lateinit var linearTCsWidget: LineChart
    private lateinit var root: View
    private lateinit var viewAccept: LinearLayout
    private lateinit var viewAcceptWidget: LinearLayout
    private  var viewChild: View? = null
    private  var viewChildWidget: View? = null
    private lateinit var currSpinner: Spinner
    private lateinit var currGrafSpinner: Spinner
    private lateinit var currGrafSpinnerWidget: Spinner
    private lateinit var toBuy: RadioButton
    private lateinit var toSell: RadioButton
    private lateinit var rubValue: TextView
    private lateinit var eqSign: TextView
    private lateinit var currValue: EditText
    private lateinit var curSigna: TextView
    private lateinit var calculate: Button
    private var usdBuy: Double = 0.0
    private var usdSell: Double = 0.0
    private var eurBuy: Double = 0.0
    private var eurSell: Double = 0.0
    private var gbpBuy: Double = 0.0
    private var gbpSell: Double = 0.0


    val datesTime: MutableList<String> = mutableListOf()
    private val usdData: MutableList<CurrencyTCS> = mutableListOf()
    private val usdDataBuy: MutableList<Double> = mutableListOf()
    private val usdDataSell: MutableList<Double> = mutableListOf()
    private val eurData: MutableList<CurrencyTCS> = mutableListOf()
    private val eurDataBuy: MutableList<Double> = mutableListOf()
    private val eurDataSell: MutableList<Double> = mutableListOf()
    private val gbpData: MutableList<CurrencyTCS> = mutableListOf()
    private val gbpDataBuy: MutableList<Double> = mutableListOf()
    private val gbpDataSell: MutableList<Double> = mutableListOf()

    val datesTimeW: MutableList<String> = mutableListOf()
    private val usdDataW: MutableList<CurrencyTCS> = mutableListOf()
    private val usdDataBuyW: MutableList<Double> = mutableListOf()
    private val usdDataSellW: MutableList<Double> = mutableListOf()
    private val eurDataW: MutableList<CurrencyTCS> = mutableListOf()
    private val eurDataBuyW: MutableList<Double> = mutableListOf()
    private val eurDataSellW: MutableList<Double> = mutableListOf()
    private val gbpDataW: MutableList<CurrencyTCS> = mutableListOf()
    private val gbpDataBuyW: MutableList<Double> = mutableListOf()
    private val gbpDataSellW: MutableList<Double> = mutableListOf()

    private lateinit var listWidgetData: List<Currencies>

    private val calcViewModel: CalcViewModel by lazy {
        ViewModelProviders.of(this).get(CalcViewModel::class.java)
    }

    private var _binding: FragmentCalcBinding? = null


    private val binding get() = _binding!!

    /*companion object {
        fun newInstance(list: List<Double>): CalcFragment {
            val args = Bundle().apply {
                putSerializable(USD_BUY, list[0])
                putSerializable(USD_SELL, list[1])
                putSerializable(EUR_BUY, list[2])
                putSerializable(EUR_SELL, list[3])
                putSerializable(GBP_BUY, list[4])
                putSerializable(GBP_SELL, list[5])
            }
            return CalcFragment().apply {
                arguments = args
            }
        }
    }*/

    private lateinit var viewModel: CalcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalcBinding.inflate(inflater, container, false)
        root = binding.root
        root = inflater.inflate(R.layout.fragment_calc, container, false)
        viewAccept = root.findViewById(R.id.viewaccept)
        viewAcceptWidget = root.findViewById(R.id.viewacceptWidget)
        viewChild = inflater.inflate(R.layout.layoutgraph, container, false)
        viewChildWidget = inflater.inflate(R.layout.layoutgraph, container, false)

        curSigna = root.findViewById(R.id.cur_sign)
        currSpinner = root.findViewById(R.id.currency_calc)
        val currAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout,
            resources.getStringArray(R.array.currency)
        )
        currAdapter.setDropDownViewResource(R.layout.spinner_layout)
        currSpinner.apply {
            adapter = currAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?,
                    position: Int, id: Long
                ) {
                    when (position) {
                        0 -> {
                            curSigna.text = "$"
                            toBuy.text = getString(R.string.CALCFRAGbuy)
                            toSell.text = getString(R.string.CALCFRAGsell)
                        }
                        1 -> {
                            curSigna.text = "€"
                            toBuy.text = getString(R.string.CALCFRAGbuy)
                            toSell.text = getString(R.string.CALCFRAGsell)
                        }
                        2 -> {
                            curSigna.text = "₤"
                            toBuy.text = getString(R.string.CALCFRAGbuy)
                            toSell.text = getString(R.string.CALCFRAGsell)
                        }
                    }
                }
                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }
        eqSign = root.findViewById(R.id.eq_tv)
        toBuy = root.findViewById(R.id.buy)
        toSell = root.findViewById(R.id.sell)
        currValue = root.findViewById(R.id.curr_value)
        rubValue = root.findViewById(R.id.rub_value)

        calculate = root.findViewById(R.id.calcul)
        calculate.apply {
            setOnClickListener {
                calculating()
            }
        }
        toSell.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    eqSign.text = "->"
                    currValue.isEnabled = true
                    rubValue.isEnabled = false
                    currValue.requestFocus()
                    calculate.isEnabled = true
                    currValue.setText("")
                    rubValue.text = ""
                }
            }
        }
        toBuy.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    eqSign.text = "<-"
                    currValue.isEnabled = true
                    rubValue.isEnabled = false
                    currValue.requestFocus()
                    calculate.isEnabled = true
                    currValue.setText("")
                    rubValue.text = ""
                }
            }
        }
        attachGraph()
        attachWidgetGraph()
        return root
    }

    override fun onResume() {
        attachGraph()
        attachWidgetGraph()
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //подписываемся на последние курсы валют Тиньков
        calcViewModel.getData().observe(viewLifecycleOwner, { forecTCS ->
            forecTCS?.let {
                getData(forecTCS)
            }
        })
        //если в настройках активен пункт автообновление курса- извлекаем данные из подписки на сохраненные на устройстве курсы валют,
        //которые сохраняются при каждом автообновлении
        calcViewModel.getDataList().observe(viewLifecycleOwner, {
            val pref =
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean("updateon", false)
            if (pref) {
                extractGraphData(it)
            }
        })
        //если в настройках активен пункт показывать информацию с виджета курса- извлекаем данные из подписки на сохраненные в БД на устройстве курсы валют,
        //которые сохраняются при каждом обновлении виджета
        calcViewModel.livedataTKS.observe(viewLifecycleOwner, {
            val pref =
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean("widgeton", false)
            if (pref) {
                extractWidgetGraphData(it)
            }
            listWidgetData = it
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CalcViewModel::class.java)
    }

    //отвечает за визуальные изменения панели калькулятора
    private fun calculating() {
        var result: Double
        var convertValue: Double
        var buyValue = 0.0
        var sellValue = 0.0
        try {
            when (currSpinner.selectedItemPosition) {
                0 -> {
                    buyValue = usdBuy
                    sellValue = usdSell
                }
                1 -> {
                    buyValue = eurBuy
                    sellValue = eurSell
                }
                2 -> {
                    buyValue = gbpBuy
                    sellValue = gbpSell
                }
            }
            if (toBuy.isChecked) {
                convertValue = currValue.text.toString().toDouble()
                result = convertValue * sellValue
                rubValue.text = String.format(Locale.US, "%.2f", result)
            }
            if (toSell.isChecked) {
                convertValue = currValue.text.toString().toDouble()
                result = convertValue * buyValue
                rubValue.text = String.format(Locale.US, "%.2f", result)
            }
        } catch (ex: NumberFormatException) {
            Toast.makeText(context, R.string.incorrect_number, Toast.LENGTH_SHORT).show()
            currValue.requestFocus()
        }
    }

    //извлекаем последние данные курса валют Тиньков
    private fun getData(dataList: List<CurrencyTCS>) {
        usdBuy = dataList[0].buy!!
        usdSell = dataList[0].sell!!
        eurBuy = dataList[1].buy!!
        eurSell = dataList[1].sell!!
        gbpBuy = dataList[1].buy!!
        gbpSell = dataList[1].sell!!
    }


    private fun longToTime(time: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("HH:mm:ss dd.MM.yyyy", resources.configuration.locales[0]).format(
                Date(
                    time
                )
            )
        } else {
            return SimpleDateFormat("HH:mm:ss dd.MM.yyyy", resources.configuration.locale).format(
                Date(time)
            )
        }
    }

    //извлекаем данные для показа графика значений, полученных в результате автообновления
    private fun extractGraphData(dataList: List<List<CurrencyTCS>>) {
        usdData.clear()
        usdDataBuy.clear()
        usdDataSell.clear()
        eurData.clear()
        eurDataBuy.clear()
        eurDataSell.clear()
        gbpData.clear()
        gbpDataBuy.clear()
        gbpDataSell.clear()
        datesTime.clear()

        dataList.forEach {
            usdData.add(it[0])
            it[0].buy?.let { it1 -> usdDataBuy.add(it1) }
            it[0].sell?.let { it1 -> usdDataSell.add(it1) }
            it[0].datetime?.let { it1 -> datesTime.add(longToTime(it1)) }
            eurData.add(it[1])
            it[1].buy?.let { it1 -> eurDataBuy.add(it1) }
            it[1].sell?.let { it1 -> eurDataSell.add(it1) }
            gbpData.add(it[2])
            it[2].buy?.let { it1 -> gbpDataBuy.add(it1) }
            it[2].sell?.let { it1 -> gbpDataSell.add(it1) }
        }
        if (datesTime.size > 2) {
            createGraph(0, viewChild!!, datesTime)
            val s = usdData[0].name
            val e = eurData[0].name
            val g = gbpData[0].name
            if (s != null) {
                if (e != null) {
                    if (g != null) {
                        fillTCsGraph()
                    }
                }
            }
        }
    }

    //извлекаем данные для показа графика значений, полученных в результате обновления виджета
    private fun extractWidgetGraphData(dataList: List<Currencies>) {
        usdDataW.clear()
        usdDataBuyW.clear()
        usdDataSellW.clear()
        eurDataW.clear()
        eurDataBuyW.clear()
        eurDataSellW.clear()
        gbpDataW.clear()
        gbpDataBuyW.clear()
        gbpDataSellW.clear()
        datesTimeW.clear()

        dataList.forEach {
            usdDataBuyW.add(it.usdBuy)
            usdDataSellW.add(it.usdSell)
            eurDataBuyW.add(it.eurBuy)
            eurDataSellW.add(it.eurSell)
            gbpDataBuyW.add(it.gbpBuy)
            gbpDataSellW.add(it.gbpSell)
            datesTimeW.add(it.dt)
        }
        if (datesTimeW.size > 2) {
            createGraph(1, viewChildWidget!!, datesTimeW)
            fillWidgetTCsGraph()
        }
    }

    //если в настройках стоит галочка автообновления- добавляем на фрагмент вид с соответствующим графиком
    private fun attachGraph() {
        val pref =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("updateon", false)
        if (pref) {
            viewAccept.removeAllViews()
            if (viewAccept.childCount > 0)
                viewAccept.removeView(viewChild)
            viewAccept.addView(viewChild)
            currGrafSpinner = viewChild!!.findViewById(R.id.currency_graf)
            val currAdapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_layout_main,
                resources.getStringArray(R.array.currency)
            )
            currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
            currGrafSpinner.apply {
                adapter = currAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?,
                        position: Int, id: Long
                    ) {
                        createGraph(0, viewChild!!, datesTime)
                        fillTCsGraph()
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>?) {}
                }
            }
            val title = viewChild!!.findViewById<TextView>(R.id.title)
            title.text = getString(R.string.CALCFRAGdatafrautoup)
            val trashCan = viewChild!!.findViewById<ImageView>(R.id.trashcan)
            trashCan.apply {
                setOnClickListener {
                    Saver().deleteTcslast()
                    attachGraph()
                    CalcViewModel.data2.postValue(Saver().loadTcslast())
                }
            }
        } else {
            viewAccept.removeAllViews()
        }
    }

    //если в настройках стоит галочка показа информации об обновлении виджета- добавляем на фрагмент вид с соответствующим графиком
    private fun attachWidgetGraph() {
        val pref =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("widgeton", false)
        if (pref) {
            viewAcceptWidget.removeAllViews()
            if (viewAcceptWidget.childCount > 0)
                viewAcceptWidget.removeView(viewChildWidget)
            viewAcceptWidget.addView(viewChildWidget)
            currGrafSpinnerWidget = viewChildWidget!!.findViewById(R.id.currency_graf)
            val currAdapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_layout_main,
                resources.getStringArray(R.array.currency)
            )
            currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
            currGrafSpinnerWidget.apply {
                adapter = currAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?,
                        position: Int, id: Long
                    ) {
                        createGraph(1, viewChildWidget!!, datesTimeW)
                        fillWidgetTCsGraph()
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>?) {}
                }
            }
            val title = viewChildWidget!!.findViewById<TextView>(R.id.title)
            title.text = getString(R.string.CALCFRAGdatafrwidget)
            val trashCan = viewChildWidget!!.findViewById<ImageView>(R.id.trashcan)
            trashCan.apply {
                setOnClickListener {
                    val currenciesRepository = CurrenciesRepository.get()
                    currenciesRepository.clearCurrencies(listWidgetData)
                    attachWidgetGraph()
                    createGraph(1, viewChildWidget!!, datesTimeW)
                    fillWidgetTCsGraph()
                }
            }
        } else {
            viewAcceptWidget.removeAllViews()
        }
    }

    //функция создания и настройки графика
    private fun createGraph(type: Int, childView: View, timeDate: MutableList<String>) {
        val chart: LineChart = childView.findViewById(R.id.chartattach)
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
                return if (value < timeDate.size) timeDate[value.toInt()] else ""
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
        when (type) {
            0 -> linearTCs = chart
            1 -> linearTCsWidget = chart
        }
        xAxis.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        leftAxis.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        rightAxis.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
        tempDescription.textColor =
            resources.getColor(R.color.white, requireActivity().application.theme)
        l.textColor = resources.getColor(R.color.white, requireActivity().application.theme)
    }

    //заполняем данные для прорисовки графика данных виджета
    private fun fillWidgetTCsGraph() {
        when (currGrafSpinnerWidget.selectedItemPosition) {
            0 -> {
                alternativeFillTCsGraphWidget("USD", usdDataBuyW, usdDataSellW)
            }
            1 -> {
                alternativeFillTCsGraphWidget("EUR", eurDataBuyW, eurDataSellW)
            }
            2 -> {
                alternativeFillTCsGraphWidget("GBP", gbpDataBuyW, gbpDataSellW)
            }
        }
    }

    //заполняем данные для прорисовки графика автообновления значений
    private fun fillTCsGraph() {
        when (currGrafSpinner.selectedItemPosition) {
            0 -> {
                if (usdData.size > 1)
                    alternativeFillTCsGraph(usdData[0].name!!, usdData)
            }
            1 -> {
                if (eurData.size > 1)
                    alternativeFillTCsGraph(eurData[0].name!!, eurData)
            }
            2 -> {
                if (gbpData.size > 1)
                    alternativeFillTCsGraph(gbpData[0].name!!, gbpData)
            }
        }
    }


    private fun alternativeFillTCsGraph(s: String, currData: MutableList<CurrencyTCS>) {
        if (currData.size > 2) {
            val currBuy: MutableList<Double> = mutableListOf()
            val currSell: MutableList<Double> = mutableListOf()
            currData.forEach {
                currBuy.add(it.buy!!)
                currSell.add(it.sell!!)
            }

            val dataSets: MutableList<ILineDataSet> = mutableListOf()
            val entries: MutableList<Entry> = mutableListOf()
            val entries2: MutableList<Entry> = mutableListOf()
            for (i in 0 until currBuy.size) {
                entries.add(Entry(i.toFloat(), currBuy[i].toFloat()))
            }
            val d = LineDataSet(entries, "$s ${getString(R.string.CALCFRAGbuying)}")
            d.lineWidth = 2.5f
            d.circleRadius = 1f
            d.color = Color.rgb(55, 70, 170)
            d.valueTextColor =
                resources.getColor(R.color.white, requireActivity().application.theme)
            d.mode =
                if (d.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
            dataSets.add(d)

            for (i in 0 until currSell.size) {
                entries2.add(Entry(i.toFloat(), currSell[i].toFloat()))
            }
            val d2 = LineDataSet(entries2, "$s ${getString(R.string.CALCFRAGselling)}")
            d2.lineWidth = 2.5f
            d2.circleRadius = 1f
            d2.color = Color.rgb(240, 70, 55)
            d2.valueTextColor =
                resources.getColor(R.color.white, requireActivity().application.theme)
            d2.mode =
                if (d2.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
            dataSets.add(d2)

            val dateSet = LineData(dataSets)
            linearTCs.data = dateSet
            linearTCs.notifyDataSetChanged()
            linearTCs.invalidate()
        }
    }

    private fun alternativeFillTCsGraphWidget(
        s: String,
        currBuy: MutableList<Double>,
        currSell: MutableList<Double>
    ) {
        if ((currBuy.size > 2) and (currSell.size > 2)) {
            val dataSets: MutableList<ILineDataSet> = mutableListOf()
            val entries: MutableList<Entry> = mutableListOf()
            val entries2: MutableList<Entry> = mutableListOf()
            for (i in 0 until currBuy.size) {
                entries.add(Entry(i.toFloat(), currBuy[i].toFloat()))
            }
            val d = LineDataSet(entries, "$s ${getString(R.string.CALCFRAGbuying)}")
            d.lineWidth = 2.5f
            d.circleRadius = 1f
            d.color = Color.rgb(55, 70, 170)
            d.valueTextColor =
                resources.getColor(R.color.white, requireActivity().application.theme)
            d.mode =
                if (d.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
            dataSets.add(d)

            for (i in 0 until currSell.size) {
                entries2.add(Entry(i.toFloat(), currSell[i].toFloat()))
            }
            val d2 = LineDataSet(entries2, "$s ${getString(R.string.CALCFRAGselling)}")
            d2.lineWidth = 2.5f
            d2.circleRadius = 1f
            d2.color = Color.rgb(240, 70, 55)
            d2.valueTextColor =
                resources.getColor(R.color.white, requireActivity().application.theme)
            d2.mode =
                if (d2.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
            dataSets.add(d2)

            val dateSet = LineData(dataSets)
            linearTCsWidget.data = dateSet
            linearTCsWidget.notifyDataSetChanged()
            linearTCsWidget.invalidate()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}