package com.vadimko.curforeckotlin.ui.archive

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.R.array
import com.vadimko.curforeckotlin.R.layout
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.cbxmlApi.CurrencyCBarhive
import com.vadimko.curforeckotlin.databinding.FragmentArchiveBinding
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.utils.ArchiveLineChartBuilder
import com.vadimko.curforeckotlin.utils.ArchivePreferences
import com.vadimko.curforeckotlin.utils.DateConverter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


const val FROM_DATE_PICKER = "fromDatePicker"
const val TILL_DATE_PICKER = "tillDatePicker"

/**
 * Archive fragment representing courses for the interval selected by the user
 * @property linearCbrf [LineChart] represent chart of CB currencies values
 * @property linearChartForec [LineChart] represent chart of MOEX currencies values and forecast for
 * this values
 * @property currSpinner spinner to select currency type
 * @property archiveViewModel [ArchiveViewModel] for [ArchiveFragment]
 * @property root root view of [ArchiveFragment]
 * @property fromTv textview with listener to set "from" date range
 * @property tillTv textview with listener to set "till" date range
 * @property tillDate date contains selected "till" date value
 * @property fromDate date contains selected "from" date value
 *
 */
class ArchiveFragment : Fragment() {

    private lateinit var linearCbrf: LineChart
    private lateinit var linearChartForec: LineChart

    private lateinit var currSpinner: Spinner

    private lateinit var chartSpinner: Spinner


    /* private val archiveViewModel: ArchiveViewModel by lazy {
         ViewModelProvider(this).get(ArchiveViewModel::class.java)
     }*/

    private val archiveViewModel: ArchiveViewModel by viewModel()

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
        val root = binding.root

        val loadPrefs = context?.let { ArchivePreferences.loadPrefs() }
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
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }

        chartSpinner = binding.chartchoose
        val chartAdapter = ArrayAdapter(
            requireContext(),
            layout.spinner_layout_main,
            resources.getStringArray(array.chartsource)
        )
        chartAdapter.setDropDownViewResource(layout.spinner_layout_main)
        chartSpinner.apply {
            adapter = chartAdapter
            setSelection(loadPrefs?.get(10)!!.toInt(), true)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    position: Int, id: Long
                ) {
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }

        val buttonGraphBuild = binding.buildgraph
        buttonGraphBuild.apply {
            setOnClickListener {
                val currChosen = currSpinner.selectedItemPosition
                val chartChosen = chartSpinner.selectedItemPosition

                archiveViewModel.createRequestStrings(
                    currChosen,
                    chartChosen,
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

    /**
     * Set listener to receive data from [DatePickerFragment] with first date
     * Set listener to receive data from [DatePickerFragment] with first date
     * Subscribe to data from the Central Bank through [ArchiveViewModel]
     * Subscribe to data from the MOEX through [ArchiveViewModel]
     */
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


        /**
         * subscribe to data from the Central Bank throught [ArchiveViewModel]
         */
        archiveViewModel.getDataCB().observe(viewLifecycleOwner, { archiveCB ->
            archiveCB?.let {
                if (it.size > 2)
                    redrawCB(it)
                else {
                    archiveViewModel.showToast(requireContext().getString(R.string.wrongcountCB))
                }
            }

        })
        /**
         * subscribe to data from the MOEX throught [ArchiveViewModel]
         */
        archiveViewModel.getDataMOEX().observe(viewLifecycleOwner, { archiveMOEX ->
            archiveMOEX?.let {
                if (it.size > 2)
                    redrawMoex(it)
                else {
                    archiveViewModel.showToast(requireContext().getString(R.string.wrongcountTK))
                }
            }
        })
    }

    /**
     * Call functions to redraw CB graph
     * @param dataListCB listof [CurrencyCBarhive] used for build x Axis in [ArchiveLineChartBuilder.createLineChart]
     * and fill chart by values in [ArchiveLineChartBuilder.fillClearCbrfGraph]
     */
    private fun redrawCB(dataListCB: List<CurrencyCBarhive>) {
        if (chartSpinner.selectedItemPosition == 1) {
            linearCbrf = binding.chart
            linearCbrf = ArchiveLineChartBuilder.createLineChart(linearCbrf, dataListCB, "cbr.ru")
            ArchiveLineChartBuilder.fillClearCbrfGraph(
                linearCbrf,
                currSpinner.selectedItem as String,
                dataListCB
            )
        }

    }

    /**
     * Call functions to redraw MOEX graph
     * @param dataListMOEX listof [CurrencyMOEX] used for build x Axis in [ArchiveLineChartBuilder.createLineChart]
     * and fill chart by values in [ArchiveLineChartBuilder.fillLinearSetForecast]
     */
    private fun redrawMoex(dataListMOEX: List<CurrencyMOEX>) {
        if (chartSpinner.selectedItemPosition == 0) {
            linearChartForec = binding.chart
            linearChartForec =
                ArchiveLineChartBuilder.createLineChart(
                    linearChartForec,
                    dataListMOEX,
                    "iss.moex.com"
                )
            ArchiveLineChartBuilder.fillLinearSetForecast(
                linearChartForec,
                currSpinner.selectedItem as String,
                dataListMOEX
            )
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

}

