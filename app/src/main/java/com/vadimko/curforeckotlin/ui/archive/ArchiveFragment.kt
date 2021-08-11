package com.vadimko.curforeckotlin.ui.archive

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
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
import com.vadimko.curforeckotlin.DateConverter
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.R.*
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.cbxmlApi.CurrencyCBarhive
import com.vadimko.curforeckotlin.databinding.FragmentArchiveBinding
import com.vadimko.curforeckotlin.forecastsMethods.ExponentSmooth
import com.vadimko.curforeckotlin.forecastsMethods.LessSquare
import com.vadimko.curforeckotlin.forecastsMethods.WMA
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.prefs.ArchivePreferences
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

 const val FROM_DATE_PICKER = "fromDatePicker"
 const val TILL_DATE_PICKER = "tillDatePicker"

/**
 * Archive fragment representing courses for the interval selected by the user
 */


class ArchiveFragment : Fragment(){


    private lateinit var linearCbrf: LineChart
    private lateinit var linearChartForec: LineChart

    private var histCBRF: MutableList<CurrencyCBarhive> = mutableListOf()

    private var dates: MutableList<String> = mutableListOf()
    private var open: MutableList<Float> = mutableListOf()
    private var close: MutableList<Float> = mutableListOf()
    private var low: MutableList<Float> = mutableListOf()
    private var high: MutableList<Float> = mutableListOf()
    private var warprice: MutableList<Float> = mutableListOf()

    private var datesForecast: MutableList<String> = mutableListOf()
    private var datesForecastSmooth: MutableList<String> = mutableListOf()

    private var datesCB: MutableList<String> = mutableListOf()

    private var choosenCurrency = ""
    private lateinit var currSpinner: Spinner


   /* private val archiveViewModel: ArchiveViewModel by lazy {
        ViewModelProvider(this).get(ArchiveViewModel::class.java)
    }*/

    private val archiveViewModel by viewModel<ArchiveViewModel>()

    private lateinit var root: View

    private var _binding: FragmentArchiveBinding? = null

    private val binding get() = _binding!!

    private lateinit var fromTv: TextView
    private lateinit var tillTv: TextView

    private lateinit var tillDate: Date //? = Date(System.currentTimeMillis())
    private lateinit var fromDate: Date //? = Date(System.currentTimeMillis() - 604800000)

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        root = binding.root

        val loadPrefs = context?.let { ArchivePreferences.loadPrefs(it) }
        if (loadPrefs != null) {
            tillDate = Date(loadPrefs[1].toLong())
        }
        if (loadPrefs != null) {
            fromDate = Date(loadPrefs[0].toLong())
        }

        currSpinner = binding.currchoose
        val currAdapter = ArrayAdapter(
            requireContext(),
            layout.spinner_layout_main,
            resources.getStringArray(array.currency)
        )
        currAdapter.setDropDownViewResource(layout.spinner_layout_main)
        currSpinner.apply {
            adapter = currAdapter
            setSelection(loadPrefs?.get(2)!!.toInt(), true)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    position: Int, id: Long
                ) {
                    choosenCurrency = currSpinner.getItemAtPosition(position) as String
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }
        val buttonGraphBuild = binding.buildgraph
        buttonGraphBuild.apply {
            setOnClickListener {
                val choosen = currSpinner.selectedItemPosition
                archiveViewModel.createRequestStrings(
                    choosen,
                    fromDate,
                    tillDate,
                )
            }
        }
        fromTv = binding.from
        fromTv.apply {
            setOnClickListener {
                val c = Calendar.getInstance()
                c.time = fromDate
                val newFragment = DatePickerFragment.newInstance(
                    c.time,
                )
                newFragment.show(parentFragmentManager, FROM_DATE_PICKER)
            }
        }
        tillTv = binding.till
        tillTv.apply {
            setOnClickListener {
                val c = Calendar.getInstance()
                c.time = tillDate
                val newFragment = DatePickerFragment.newInstance(
                    c.time,
                )
                newFragment.show(parentFragmentManager, TILL_DATE_PICKER)
            }
        }
        val c = Calendar.getInstance()
        c.time = fromDate
        fromTv.text = DateConverter.dateWithOutTimeFormat(fromDate)
        c.time = tillDate
        tillTv.text = DateConverter.dateWithOutTimeFormat(tillDate)
        return root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager
            .setFragmentResultListener("fromDate", this) { _, bundle ->
            fromDate = bundle.get("bundleKey") as Date
            fromTv.text = DateConverter.dateWithOutTimeFormat(fromDate)
        }
        parentFragmentManager
            .setFragmentResultListener("tillDate", this) { _, bundle ->
            tillDate = bundle.get("bundleKey") as Date
            tillTv.text = DateConverter.dateWithOutTimeFormat(tillDate)
        }
        //subscribe to data from the Central Bank in ArchiveViewModel
        archiveViewModel.getDataCB().observe(viewLifecycleOwner, { archiveCB ->
            archiveCB?.let {
                histCBRF = it as MutableList<CurrencyCBarhive>
                extractDataCB(archiveCB)
            }

        })
        //subscribe to data from the MOEX in ArchiveViewModel
        archiveViewModel.getDataMOEX().observe(viewLifecycleOwner, { archiveMOEX ->
            archiveMOEX?.let {
                extractDataMOEX(archiveMOEX)
            }
        })
    }

    //extraction of data received from MOEX for graph building
    private fun extractDataMOEX(dataListMOEX: List<CurrencyMOEX>) {
        dates.clear()
        open.clear()
        close.clear()
        high.clear()
        low.clear()
        warprice.clear()
        datesForecast.clear()
        datesForecastSmooth.clear()
        dataListMOEX.forEach {
            dates.add(it.dates)
            open.add(it.open.toFloat())
            close.add(it.close.toFloat())
            high.add(it.high.toFloat())
            low.add(it.low.toFloat())
            warprice.add(it.warprice.toFloat())
            datesForecast.add(it.dates.split(" ")[0])
            datesForecastSmooth.add(it.dates)
        }
        for (i in 1 until 3)
            datesForecast.add("${getString(string.ARCFRAGforecDayGraph)}  $i ")
        datesForecastSmooth.add("${getString(string.ARCFRAGforecDayGraph)}  1 ")
        redrawMoex()
    }

    //extraction of data received from CB for graph building
    private fun extractDataCB(dataListCB: List<CurrencyCBarhive>) {
        datesCB.clear()
        dataListCB.forEach {
            datesCB.add(it.datetimeConv)
        }
        redrawCB(dataListCB)
    }

    //redraw CB chart
    private fun redrawCB(dataListCB: List<CurrencyCBarhive>) {
        createClearCbrfGraph()
        fillClearCbrfGraph(currSpinner.selectedItem as String, dataListCB)
    }

    //redraw MOEX chart
    private fun redrawMoex() {
        createLinearSetForecast()
        fillLinearSetForecast(currSpinner.selectedItem as String)
    }

    //creating and setting the parameters of the CB chart
    private fun createClearCbrfGraph() {
        linearCbrf = binding.chartcbrf
        linearCbrf.clear()
        linearCbrf.setDrawGridBackground(false)
        linearCbrf.description.isEnabled = true
        val tempDescription = Description()
        tempDescription.text = "cbr.ru"
        linearCbrf.description = tempDescription
        linearCbrf.setDrawBorders(true)
        linearCbrf.axisLeft.isEnabled = true
        linearCbrf.axisRight.setDrawAxisLine(true)
        linearCbrf.axisRight.setDrawGridLines(true)
        linearCbrf.xAxis.setDrawAxisLine(true)
        linearCbrf.xAxis.setDrawGridLines(true)
        linearCbrf.xAxis.labelRotationAngle = -45f
        val xAxis: XAxis = linearCbrf.xAxis
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < datesCB.size) datesCB[value.toInt()] else ""
            }
        }
        val rightAxis: YAxis = linearCbrf.axisRight
        rightAxis.setDrawGridLines(true)
        val leftAxis: YAxis = linearCbrf.axisLeft
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
        linearCbrf.setTouchEnabled(true)
        linearCbrf.isDragEnabled = true
        linearCbrf.setScaleEnabled(true)
        linearCbrf.setPinchZoom(true)
        val animationLong = 5 * datesCB.size
        linearCbrf.animateX(animationLong)
        val l: Legend = linearCbrf.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        xAxis.textColor = resources.getColor(color.white, requireActivity().application.theme)
        leftAxis.textColor = resources.getColor(color.white, requireActivity().application.theme)
        rightAxis.textColor = resources.getColor(color.white, requireActivity().application.theme)
        tempDescription.textColor =
            resources.getColor(color.white, requireActivity().application.theme)
        l.textColor = resources.getColor(color.white, requireActivity().application.theme)
    }


    //filling CB chart with data
    private fun fillClearCbrfGraph(s: String, dataListCB: List<CurrencyCBarhive>) {
        val dataSets = ArrayList<ILineDataSet>()
        val entries = ArrayList<Entry>()
        for (i in dataListCB.indices) {
            val str: String = dataListCB[i].offCur.replace(',', '.')
            entries.add(Entry(i.toFloat(), str.toFloat()))
        }
        val d = LineDataSet(entries, "$s ${getString(string.ARCFRAGcursCBGraphLabel)}")
        d.lineWidth = 2.5f
        d.circleRadius = 1f
        d.color = Color.parseColor("#1231db")
        d.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d.mode =
            if (d.mode == LineDataSet.Mode.STEPPED) LineDataSet.Mode.LINEAR else LineDataSet.Mode.STEPPED
        dataSets.add(d)
        val datSets = LineData(dataSets)
        linearCbrf.data = datSets
        linearCbrf.notifyDataSetChanged()
        linearCbrf.invalidate()
    }

    //creating and setting the parameters of the MOEX chart
    private fun createLinearSetForecast() {
        linearChartForec = binding.linearforec
        linearChartForec.clear()
        linearChartForec.notifyDataSetChanged()
        linearChartForec.invalidate()
        linearChartForec.setDrawGridBackground(false)
        linearChartForec.description.isEnabled = true
        val tempDescription = Description()
        tempDescription.text = "iss.moex.com"
        linearChartForec.description = tempDescription
        linearChartForec.setDrawBorders(true)
        linearChartForec.axisLeft.isEnabled = true
        linearChartForec.axisRight.setDrawAxisLine(true)
        linearChartForec.axisRight.setDrawGridLines(true)
        linearChartForec.xAxis.setDrawAxisLine(true)
        linearChartForec.xAxis.setDrawGridLines(true)
        linearChartForec.xAxis.labelRotationAngle = -45f
        val xAxis: XAxis = linearChartForec.xAxis
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < datesForecast.size) datesForecast[value.toInt()] else ""
            }
        }
        val rightAxis: YAxis = linearChartForec.axisRight
        rightAxis.setDrawGridLines(true)
        val leftAxis: YAxis = linearChartForec.axisLeft
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
        linearChartForec.setTouchEnabled(true)
        linearChartForec.isDragEnabled = true
        linearChartForec.setScaleEnabled(true)
        linearChartForec.setPinchZoom(true)
        val l: Legend = linearChartForec.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        xAxis.textColor = resources.getColor(color.white, requireActivity().application.theme)
        leftAxis.textColor = resources.getColor(color.white, requireActivity().application.theme)
        rightAxis.textColor = resources.getColor(color.white, requireActivity().application.theme)
        tempDescription.textColor =
            resources.getColor(color.white, requireActivity().application.theme)
        l.textColor = resources.getColor(color.white, requireActivity().application.theme)
    }

    //filling MOEX chart with data
    private fun fillLinearSetForecast(s: String) {
        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        val entries: MutableList<Entry> = mutableListOf()
        for (i in warprice.indices)
            if (warprice[i] != 0f) {
                entries.add(Entry(i.toFloat(), warprice[i]))
            }

        val entries2: MutableList<Entry> = mutableListOf()
        val warpFlt: MutableList<Float> = mutableListOf()
        for (i in warprice.indices) {
            warpFlt.add(warprice[i])
        }

        val temp = ExponentSmooth(warpFlt)
        temp.calc()
        val smooth1: MutableList<Float> = temp.getSmooth()
        for (i in smooth1.indices) {
            entries2.add(Entry(i.toFloat(), smooth1[i]))
        }
        val entries3: MutableList<Entry> = mutableListOf()
        val forecast1: MutableList<Float> = temp.getForecast()
        for (i in forecast1.indices) {
            entries3.add(
                Entry(
                    ((i + smooth1.size - 1).toFloat()),
                    forecast1[i]
                )
            )
        }
        val entries4: MutableList<Entry> = mutableListOf()
        val temp2 = LessSquare(warpFlt)
        temp2.calc()
        val square1: MutableList<Float> = temp2.getOutputVal()
        for (i in square1.indices) {
            entries4.add(Entry(i.toFloat(), square1[i]))
        }
        val entries5: MutableList<Entry> = mutableListOf()
        val forecast2: MutableList<Float> = temp2.getForecastVal()
        for (i in forecast2.indices) {
            entries5.add(
                Entry(
                    (i + square1.size - 1).toFloat(),
                    forecast2[i]
                )
            )
        }

        val entries6:
                MutableList<Entry> = mutableListOf()
        var temp3Error = "0"
        if (warprice.size > 3) {
            val temp3 = WMA(warpFlt, 3)
            temp3.calc()
            temp3Error = temp3.getAverageErr().toString()
            val forecast: MutableList<Float> = temp3.getForecast()
            for (i in forecast.indices) {
                entries6.add(
                    Entry(
                        (i + warprice.size - 1).toFloat(),
                        forecast[i]
                    )
                )
            }
        }

        val d = LineDataSet(entries, "$s ${getString(string.ARCFRAGaverage)} ")
        d.lineWidth = 2.5f
        d.circleRadius = 1f
        d.color = Color.rgb(23, 100, 255)
        d.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d.mode =
            if (d.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d)

        val d6 = LineDataSet(
            entries6,
            "$s ${getString(string.ARCFRAGmovAverForecGraph)} $temp3Error %"
        )
        d6.enableDashedLine(10f, 10f, 0f)
        d6.lineWidth = 2.5f
        d6.circleRadius = 1f
        d6.color = Color.rgb(40, 120, 230)
        d6.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d6.mode =
            if (d6.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d6)


        val d2 = LineDataSet(entries2, "$s ${getString(string.ARCFRAGexponWeighGraph)}")
        d2.lineWidth = 2.5f
        d2.circleRadius = 1f
        d2.color = Color.rgb(210, 80, 90)
        d2.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d2.mode =
            if (d2.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d2)
        val d3 =
            LineDataSet(
                entries3,
                "$s  ${getString(string.ARCFRAGexponWeighForecGraph)} ${temp.getErrSmooth()}%"
            )
        d3.enableDashedLine(10f, 10f, 0f)
        d3.lineWidth = 2.5f
        d3.circleRadius = 1f
        d3.color = Color.rgb(170, 100, 100)
        d3.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d3.mode =
            if (d3.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d3)
        val d4 = LineDataSet(entries4, "$s ${getString(string.ARCFRAGleastSqMeth)}")
        d4.lineWidth = 2.5f
        d4.circleRadius = 1f
        d4.color = Color.rgb(130, 210, 120)
        d4.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d4.mode =
            if (d4.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d4)
        val d5 =
            LineDataSet(
                entries5,
                "$s  ${getString(string.ARCFRAGleastSqMethGraph)} ${temp2.getErrVal()} %"
            )
        d5.enableDashedLine(10f, 10f, 0f)
        d5.lineWidth = 2.5f
        d5.circleRadius = 1f
        d5.color = Color.rgb(100, 170, 80)
        d5.valueTextColor = resources.getColor(color.white, requireActivity().application.theme)
        d5.mode =
            if (d3.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(d5)


        val dateSet = LineData(dataSets)
        linearChartForec.data = dateSet
        linearChartForec.notifyDataSetChanged()
        val animationLong = 5 * dates.size
        linearChartForec.animateX(animationLong)
        linearChartForec.invalidate()
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

    /*override fun onDateSelected(date: Date, frommtill: Boolean) {
        val c = Calendar.getInstance()
        c.time = date
        if (!frommtill) {
            fromTv.text = longToDate(date)
            fromDate = date
        }
        if (frommtill) {
            tillTv.text = longToDate(date)
            tillDate = date
        }
    }*/

}

