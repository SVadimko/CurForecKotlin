package com.vadimko.curforeckotlin.ui.today

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.data.CandleEntry
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.databinding.FragmentTodayBinding
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.utils.TodayChartBuilder
import com.vadimko.curforeckotlin.utils.TodayPreferences
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Today fragment representing chart for latest 1-5 days
 * @property comboChartForec - chart combining line and candle graphs
 * @property yValsCandleStick - list contains data for build candle graph
 * @property todayViewModel [TodayViewModel] for [TodayFragment]
 * @property root root view of [TodayFragment]
 * @property chosenCurrency contains selected value of [currSpinner]
 * @property chosenPeriod contains selected value of [perSpinner]
 * @property chosenRate contains selected value of [rateSpinner]
 * @property currSpinner spinner for selecting currency for request to server
 * @property perSpinner spinner for selecting period ot time for request to server
 * @property rateSpinner spinner for selecting rate of time for request to server
 */

class TodayFragment : Fragment() {

    private lateinit var comboChartForec: CombinedChart
    private val yValsCandleStick = ArrayList<CandleEntry>()

    private val todayViewModel by viewModel<TodayViewModel>()

    private var _binding: FragmentTodayBinding? = null
    private var chosenCurrency = ""
    private var chosenPeriod = ""
    private var chosenRate = ""
    private lateinit var currSpinner: Spinner
    private lateinit var perSpinner: Spinner
    private lateinit var rateSpinner: Spinner


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
        val root = binding.root


        val loadPrefs = context?.let { TodayPreferences.loadPrefs() }

        currSpinner = binding.currchoose
        val currAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout_main,
            resources.getStringArray(R.array.currency)
        )
        currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
        currSpinner.apply {
            adapter = currAdapter
            setSelection(loadPrefs?.get(4)!!.toInt(), true)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    position: Int, id: Long
                ) {
                    chosenCurrency = currSpinner.getItemAtPosition(position) as String
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }
        perSpinner = binding.periodchoose
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
                    chosenPeriod = perSpinner.getItemAtPosition(position) as String
                    if ((position > 0) and (rateSpinner.selectedItemPosition == 0))
                        rateSpinner.setSelection(1)
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }
        rateSpinner = binding.ratechoose
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
                    chosenRate = rateSpinner.getItemAtPosition(position) as String
                    if ((position == 0) and (perSpinner.selectedItemPosition > 0))
                        perSpinner.setSelection(0)
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }

        val showGraphButton = binding.buildgraph
        showGraphButton.apply {
            setOnClickListener {
                val chosen = IntArray(3)
                chosen[0] = currSpinner.selectedItemPosition
                chosen[1] = perSpinner.selectedItemPosition
                chosen[2] = rateSpinner.selectedItemPosition
                todayViewModel.createRequestStrings(
                    chosen,
                    currSpinner.selectedItemPosition,
                    perSpinner.selectedItemPosition,
                    rateSpinner.selectedItemPosition
                )
            }
        }
        comboChartForec = binding.candlforec
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

    /**
     * Observe data receiving from Central bank bank through [TodayViewModel]
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var notShow = true

        lifecycleScope.launchWhenStarted {
            todayViewModel.getData().collect {
                if (it.size > 3)
                    createComboChartForecast(it)
                else {
                    if (!notShow) todayViewModel.showToast()
                    notShow = false
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

    }

    /**
     * Create a combined candlestick and line chart
     */
    private fun createComboChartForecast(dataList: List<CurrencyMOEX>) {
        comboChartForec = TodayChartBuilder.createChart(
            comboChartForec,
            dataList,
            rateSpinner.selectedItem as String,
        )
        TodayChartBuilder.fillComboChartForecast(
            comboChartForec,
            currSpinner.selectedItem as String,
            dataList
        )
    }
}