package com.vadimko.curforeckotlin.ui.calc

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.LineChart
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.TCSUpdateService
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.databinding.FragmentCalcBinding
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.utils.CalcServiceChartBuilder
import com.vadimko.curforeckotlin.utils.CalcWidgetChartBuilder
import com.vadimko.curforeckotlin.widget.AppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Calc fragment representing calculator and chart of widget update data and auto update data
 * @property calcViewModel [CalcViewModel] for [CalcFragment]
 * @property linearChartTCsService [LineChart] shows data, received by [TCSUpdateService]
 * @property linearChartTCsWidget [LineChart] shows data, received by update of [AppWidget]
 * @property root root view of [CalcFragment]
 * @property viewAcceptService parent view for [viewChildService]
 * @property viewAcceptWidget parent view for [viewChildWidget]
 * @property viewChildService view contains [linearChartTCsService]
 * @property viewChildWidget view contains [linearChartTCsWidget]
 * @property currSpinner spinner for selecting currency for calc
 * @property currGraphSpinnerService spinner to select currency displayed on [linearChartTCsService]
 * @property currGraphSpinnerWidget spinner to select currency displayed on [linearChartTCsWidget]
 * @property toBuy radio button select buy mode calculator
 * @property toSell radio button select sell mode calculator
 * @property rubValue textview shows calculating result
 * @property currValue edittext to input value to calculate
 * @property curSigna sign of selected currency
 * @property eqSign sign representing buy or sell mode calculating
 * @property dataToCalc data contains last [CurrencyTCS] values used in calculation
 * @property serviceSpinnerValue selected item position of [currGraphSpinnerService]
 * @property widgetSpinnerValue selected item position of [currGraphSpinnerWidget]
 * @property widgetUpdateData data received on widget update, used to delete it
 * @property serviceUpdateData data received on service update, used to delete it
 */

class CalcFragment : Fragment() {
    private val calcViewModel by viewModel<CalcViewModel>()
    private lateinit var linearChartTCsService: LineChart
    private lateinit var linearChartTCsWidget: LineChart
    private lateinit var viewAcceptService: LinearLayout
    private lateinit var viewAcceptWidget: LinearLayout
    private var viewChildService: View? = null
    private var viewChildWidget: View? = null
    private lateinit var currSpinner: Spinner
    private lateinit var currGraphSpinnerService: Spinner
    private lateinit var currGraphSpinnerWidget: Spinner
    private lateinit var toBuy: RadioButton
    private lateinit var toSell: RadioButton
    private lateinit var rubValue: TextView
    private lateinit var eqSign: TextView
    private lateinit var currValue: EditText
    private lateinit var curSigna: TextView

    private lateinit var dataToCalc: List<CurrencyTCS>

    private var widgetUpdateData: MutableList<Currencies> = mutableListOf()
    private var serviceUpdateData: List<List<CurrencyTCS>> = mutableListOf()

    private var serviceSpinnerValue = 0
    private var widgetSpinnerValue = 0

    private var showServiceGraph = false
    private var showWidgetGraph = false

    /*  private val calcViewModel: CalcViewModel by lazy {
          ViewModelProvider(this).get(CalcViewModel::class.java)
      }*/

    private var _binding: FragmentCalcBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showServiceGraph =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("updateon", false)
        showWidgetGraph =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean("widgetOn", false)
        _binding = FragmentCalcBinding.inflate(inflater, container, false)
        val root = binding.root
        viewAcceptService = binding.viewaccept
        viewAcceptWidget = binding.viewacceptWidget
        viewChildService = inflater.inflate(R.layout.layoutgraph, container, false)
        viewChildWidget = inflater.inflate(R.layout.layoutgraph, container, false)

        curSigna = binding.cursign
        currSpinner = binding.currencycalc
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
        eqSign = binding.eqtv
        toBuy = binding.buy
        toSell = binding.sell
        currValue = binding.currvaluecalc
        currValue.requestFocus()
        rubValue = binding.rubvaluecalc

        binding.calcul.setOnClickListener {
            calcViewModel.calculating(
                currSpinner.selectedItemPosition,
                dataToCalc,
                toBuy.isChecked,
                toSell.isChecked,
                currValue.text.toString()
            )
        }
        binding.calcul.isEnabled = true

        toSell.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    eqSign.text = "->"
                    currValue.isEnabled = true
                    rubValue.isEnabled = false
                    currValue.requestFocus()
                    binding.calcul.isEnabled = true
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
                    binding.calcul.isEnabled = true
                    rubValue.text = ""
                }
            }
        }

        linearChartTCsService = binding.servicechart
        currGraphSpinnerService = binding.serviceCurr
        val serviceCurrAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout_main,
            resources.getStringArray(R.array.currency)
        )
        serviceCurrAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
        currGraphSpinnerService.apply {
            adapter = serviceCurrAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?,
                    position: Int, id: Long
                ) {
                    serviceSpinnerValue = selectedItemPosition
                    if (!serviceUpdateData.isNullOrEmpty() && serviceUpdateData[0].size > 2) {
                        createServiceGraph()
                        fillServiceGraph()
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }

        val serviceDataDelete: ImageView = binding.serviceTrashcan
        serviceDataDelete.apply {
            setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    calcViewModel.deleteServiceUpdateData(serviceUpdateData)
                }
            }
        }

        val serviceGraphLinearLayout = binding.serviceview
        val widgetGraphLinearLayout = binding.widgetview
        if (showServiceGraph)
            serviceGraphLinearLayout.visibility = View.VISIBLE
        else serviceGraphLinearLayout.visibility = View.GONE
        if (showWidgetGraph)
            widgetGraphLinearLayout.visibility = View.VISIBLE
        else widgetGraphLinearLayout.visibility = View.GONE


        linearChartTCsWidget = binding.widgetChart
        currGraphSpinnerWidget = binding.widgetCurr
        val widgetCurrAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout_main,
            resources.getStringArray(R.array.currency)
        )
        widgetCurrAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
        currGraphSpinnerWidget.apply {
            adapter = widgetCurrAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?,
                    position: Int, id: Long
                ) {

                    widgetSpinnerValue = selectedItemPosition
                    if (widgetUpdateData.isNotEmpty()) {
                        createWidgetGraph()
                        fillWidgetGraph()
                    }
                }


                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
        }

        val widgetDataDelete: ImageView = binding.widgetTrashcan
        widgetDataDelete.apply {
            setOnClickListener {
                calcViewModel.deleteWidgetUpdateData(widgetUpdateData)
            }
        }
        return root
    }

    /**
     * Subscribe to data from the Tinkov in [CalcViewModel]
     * Subscribe to [calcViewModel]. If the auto-update from service item is active in the settings,
     * configure [linearChartTCsService] by [createServiceGraph] and fill data
     * by [fillServiceGraph] which received from [calcViewModel]
     * Subscribe to [calcViewModel]. If the auto-update from widget updater item is active in
     * the settings, configure [linearChartTCsWidget] by [createWidgetGraph] and
     * fill by data [fillWidgetGraph] which received from [calcViewModel]
     * Observe result of calculating and show it
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            calcViewModel.getDataForCalc().collect {
                dataToCalc = it
            }
        }

        calcViewModel.getServiceUpdateData().observe(viewLifecycleOwner, {
            serviceUpdateData = it
            if (!it.isNullOrEmpty() && it[0].size > 2) {
                createServiceGraph()
                fillServiceGraph()
            }
        })


        calcViewModel.getDataWidgetUpdate().observe(viewLifecycleOwner) {
            widgetUpdateData = it as MutableList<Currencies>

            if (it.isNotEmpty()) {
                createWidgetGraph()
                fillWidgetGraph()
            }
        }

        lifecycleScope.launchWhenStarted {
            calcViewModel.getRubvalue().collect {
                rubValue.text = it
            }
        }

    }

    private fun createServiceGraph() {
        linearChartTCsService =
            CalcServiceChartBuilder.createGraph(linearChartTCsService, serviceUpdateData)
    }

    private fun fillServiceGraph() {
        if (showServiceGraph)
            CalcServiceChartBuilder.fillChart(
                linearChartTCsService,
                serviceSpinnerValue,
                serviceUpdateData
            )
    }

    private fun createWidgetGraph() {
        linearChartTCsWidget =
            CalcWidgetChartBuilder.createGraph(linearChartTCsWidget, widgetUpdateData)
    }

    private fun fillWidgetGraph() {
        if (showWidgetGraph)
            CalcWidgetChartBuilder.fillChart(
                linearChartTCsWidget,
                widgetSpinnerValue,
                widgetUpdateData
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}