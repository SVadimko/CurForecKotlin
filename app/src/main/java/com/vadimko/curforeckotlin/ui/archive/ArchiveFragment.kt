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
 */
class ArchiveFragment : Fragment() {

    private lateinit var linearCbrf: LineChart
    private lateinit var linearChartForec: LineChart

    private var warprice: MutableList<Float> = mutableListOf()
    private var datesForecast: MutableList<String> = mutableListOf()
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
        linearCbrf = binding.chartcbrf
        linearChartForec = binding.linearforec
        return root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * Set listener to receive data from [DatePickerFragment] with first date
         */
        parentFragmentManager
            .setFragmentResultListener("fromDate", this) { _, bundle ->
                fromDate = bundle.get("bundleKey") as Date
                fromTv.text = DateConverter.dateWithOutTimeFormat(fromDate)
            }
        /**
         * Set listener to receive data from [DatePickerFragment] with first date
         */
        parentFragmentManager
            .setFragmentResultListener("tillDate", this) { _, bundle ->
                tillDate = bundle.get("bundleKey") as Date
                tillTv.text = DateConverter.dateWithOutTimeFormat(tillDate)
            }
        /**
         * Subscribe to data from the Central Bank throught [ArchiveViewModel]
         */
        archiveViewModel.getDataCB().observe(viewLifecycleOwner, { archiveCB ->
            archiveCB?.let {
                if (archiveCB.size > 2)
                    redrawCB(archiveCB)
                else
                    archiveViewModel.showToast(requireContext().getString(R.string.wrongcountCB))
            }

        })
        /**
         * Subscribe to data from the MOEX throught [ArchiveViewModel]
         */
        archiveViewModel.getDataMOEX().observe(viewLifecycleOwner, { archiveMOEX ->
            archiveMOEX?.let {
                if (archiveMOEX.size > 2)
                    redrawMoex(archiveMOEX)
                else
                    archiveViewModel.showToast(requireContext().getString(R.string.wrongcountTK))
            }
        })
    }

    /**
     * Call functions to redraw CB graph
     */
    private fun redrawCB(dataListCB: List<CurrencyCBarhive>) {
        linearCbrf = ArchiveLineChartBuilder.createLineChart(linearCbrf, dataListCB, "cbr.ru")
        ArchiveLineChartBuilder.fillClearCbrfGraph(
            linearCbrf,
            currSpinner.selectedItem as String,
            dataListCB
        )
    }

    /**
     * Call functions to redraw MOEX graph
     */
    private fun redrawMoex(dataListMOEX: List<CurrencyMOEX>) {
        linearChartForec =
            ArchiveLineChartBuilder.createLineChart(linearChartForec, dataListMOEX, "iss.moex.com")
        ArchiveLineChartBuilder.fillLinearSetForecast(
            linearChartForec,
            currSpinner.selectedItem as String,
            dataListMOEX
        )
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

