package com.vadimko.curforeckotlin.ui.calc

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.LineChart
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.databinding.FragmentCalcBinding
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.utils.CalcLineChartBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Calc fragment representing calculator and chart of widget update data and auto update data
 */
class CalcFragment : Fragment() {
    private lateinit var linearTCs: LineChart
    private lateinit var linearTCsWidget: LineChart
    private lateinit var root: View
    private lateinit var viewAccept: LinearLayout
    private lateinit var viewAcceptWidget: LinearLayout
    private var viewChild: View? = null
    private var viewChildWidget: View? = null
    private lateinit var currSpinner: Spinner
    private lateinit var currGrafSpinner: Spinner
    private lateinit var currGrafSpinnerWidget: Spinner
    private lateinit var toBuy: RadioButton
    private lateinit var toSell: RadioButton
    private lateinit var rubValue: TextView
    private lateinit var eqSign: TextView
    private lateinit var currValue: EditText
    private lateinit var curSigna: TextView

    private lateinit var dataToCalc: List<CurrencyTCS>

    private var widgetUpdateData: MutableList<Currencies> = mutableListOf()
    private var serviceUpdateData: List<List<CurrencyTCS>> = mutableListOf()

    private var updateSpinnerValue = 0
    private var widgetSpinnerValue = 0

    /*  private val calcViewModel: CalcViewModel by lazy {
          ViewModelProvider(this).get(CalcViewModel::class.java)
      }*/

    private val calcViewModel by viewModel<CalcViewModel>()

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
        _binding = FragmentCalcBinding.inflate(inflater, container, false)
        root = binding.root
        viewAccept = binding.viewaccept
        viewAcceptWidget = binding.viewacceptWidget
        viewChild = inflater.inflate(R.layout.layoutgraph, container, false)
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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Subscribe to data from the Tinkov in [CalcViewModel]
         */
        calcViewModel.getDataForCalc().observe(viewLifecycleOwner, { forecTCS ->
            forecTCS?.let {
                //getData(forecTCS)
                dataToCalc = forecTCS
            }
        })

        /**
         * If the auto-update rate item is active in the settings, we extract data from the
         * subscription to the currency rates stored on the device,
         * which are saved on every auto-update
         */

        //if (pref) {
        calcViewModel.getServiceUpdateData().observe(viewLifecycleOwner, {
            serviceUpdateData = it
            //showServiceChart()
            val pref =
                PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("updateon", false)
            //attachGraph(pref, it)
            attachChart(pref, it, false)
            if (pref)
            // serviceUpdateData = it
                if (!it.isNullOrEmpty() && it[0].size > 2) {
                    createGraph(it, false)
                    fillGraph(false, it, updateSpinnerValue)
                }
        })

        /**
         * If the item show information from the rate widget is active in the settings, we extract
         * data from the subscription to the currency rates stored in the database on the device,
         * which are saved every time the widget is updated
         */

        calcViewModel.dataWidgetUpdate.observe(viewLifecycleOwner) {
            widgetUpdateData = it as MutableList<Currencies>
            val pref =
                PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("widgetOn", false)
            //attachWidgetGraph(pref, it)
            attachChart(pref, it, true)
            if (pref) {
                if (it.isNotEmpty()) {
                    createGraph(it, true)
                    fillGraph(true, it, widgetSpinnerValue)
                }
            }
        }
        /**
         * Observe result of calculating and show it
         */
        calcViewModel.rubValue.observe(viewLifecycleOwner, {
            rubValue.text = it
        })
    }


    /**
     * Retrieve the latest Tinkov currency rate data
     *//*
    private fun getData(dataList: List<CurrencyTCS>) {
        usdBuy = dataList[0].buy!!
        usdSell = dataList[0].sell!!
        eurBuy = dataList[1].buy!!
        eurSell = dataList[1].sell!!
        gbpBuy = dataList[2].buy!!
        gbpSell = dataList[2].sell!!
    }*/


    /* */
    /**
     * If the auto-update checkbox is enabled in the settings, add a view with the
     * corresponding graph to the fragment
     *//*
    private fun attachGraph(pref: Boolean, data: List<List<CurrencyTCS>>) {
        *//* val pref =
             PreferenceManager.getDefaultSharedPreferences(context)
                 .getBoolean("updateon", false)*//*
        if (pref) {
            if (viewChild?.isAttachedToWindow == false) {
                viewAccept.removeAllViews()
                if (viewAccept.childCount > 0)
                    viewAccept.removeView(viewChild)
                viewAccept.addView(viewChild)
                linearTCs = viewChild!!.findViewById(R.id.chartattach)  //!!!!!!!!!!!!
                currGrafSpinner = viewChild!!.findViewById(R.id.currency_graf)
                val currAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_layout_main,
                    resources.getStringArray(R.array.currency)
                )
                currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
                updateSpinnerValue = currGrafSpinner.selectedItemPosition
                currGrafSpinner.apply {
                    adapter = currAdapter
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?,
                            position: Int, id: Long
                        ) {
                            updateSpinnerValue = currGrafSpinner.selectedItemPosition
                            if (!data.isNullOrEmpty() && data[0].size > 2) {
                                createGraph(data, false)
                                fillGraph(false, data, updateSpinnerValue)
                            }
                        }

                        override fun onNothingSelected(arg0: AdapterView<*>?) {}
                    }
                }
                val title = viewChild!!.findViewById<TextView>(R.id.title)
                title.text = getString(R.string.CALCFRAGdatafrautoup)
                val trashCan = viewChild!!.findViewById<ImageView>(R.id.trashcan)
                trashCan.apply {
                    setOnClickListener {
                        GlobalScope.launch(Dispatchers.IO) {
                            //val mutex = Mutex()
                            //mutex.withLock { Saver.deleteTcsLast(data) }
                            //mutex.withLock { CalcViewModel.loadGraphData() }

                            //delay(600)

                            //withContext(Dispatchers.Main) { attachGraph() }
                            //mutex.withLock { CalcViewModel.dataAutoUpdate.postValue(Saver.loadTcsLast()) }
                            calcViewModel.deleteServiceUpdateData(data)
                        }
                    }
                }
            }
        } else {
            viewAccept.removeAllViews()
        }
    }

    */
    /**
     * If in the settings there is a check mark showing information about updating the widget, add
     * a view with the corresponding graph to the fragment
     *//*
    private fun attachWidgetGraph(pref: Boolean, data: List<Currencies>) {
        *//* val pref =
             PreferenceManager.getDefaultSharedPreferences(context)
                 .getBoolean("widgeton", false)*//*
        if (pref) {
            if (viewChildWidget?.isAttachedToWindow == false) {
                viewAcceptWidget.removeAllViews()
                if (viewAcceptWidget.childCount > 0)
                    viewAcceptWidget.removeView(viewChildWidget)
                viewAcceptWidget.addView(viewChildWidget)
                linearTCsWidget = viewChildWidget!!.findViewById(R.id.chartattach)
                currGrafSpinnerWidget = viewChildWidget!!.findViewById(R.id.currency_graf)
                val currAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_layout_main,
                    resources.getStringArray(R.array.currency)
                )
                currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
                widgetSpinnerValue = currGrafSpinnerWidget.selectedItemPosition
                currGrafSpinnerWidget.apply {
                    adapter = currAdapter
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?,
                            position: Int, id: Long
                        ) {
                            widgetSpinnerValue = currGrafSpinnerWidget.selectedItemPosition
                            if (data.isNotEmpty()) {
                                createGraph(data, true)
                                fillGraph(true, data, widgetSpinnerValue)
                            }
                        }

                        override fun onNothingSelected(arg0: AdapterView<*>?) {}
                    }
                }
                val title = viewChildWidget!!.findViewById<TextView>(R.id.title)
                title.text = getString(R.string.CALCFRAGdatafrwidget)
                val trashCan = viewChildWidget!!.findViewById<ImageView>(R.id.trashcan)
                trashCan.apply {
                    setOnClickListener {
                        *//* val currenciesRepository = CurrenciesRepository.get()
                         currenciesRepository.clearCurrencies(listWidgetData)

                         CalcViewModel.loadDataTCS()*//*

                        calcViewModel.deleteWidgetUpdateData(widgetUpdateData)
                    }
                }
            }
        } else {
            viewAcceptWidget.removeAllViews()
        }
    }
*/
    @Suppress("UNCHECKED_CAST")
    private fun attachChart(pref: Boolean, data: Any, type: Boolean) {
        val parent: LinearLayout?
        val child: View?
        val spinner: Spinner
        var spinnerPos: Int
        val text: String
        val serviceData = data as List<List<CurrencyTCS>>
        val updateData = data as List<Currencies>
        if (!type) {
            parent = viewAccept
            child = viewChild
            currGrafSpinner = child!!.findViewById(R.id.currency_graf)
            spinner = currGrafSpinner
            spinnerPos = updateSpinnerValue
            text = getString(R.string.CALCFRAGdatafrautoup)
        } else {
            parent = viewAcceptWidget
            child = viewChildWidget
            currGrafSpinnerWidget = child!!.findViewById(R.id.currency_graf)
            spinner = currGrafSpinnerWidget
            spinnerPos = widgetSpinnerValue
            text = getString(R.string.CALCFRAGdatafrwidget)
        }
        if (pref) {
            if (!child.isAttachedToWindow) {
                parent.removeAllViews()
                if (parent.childCount > 0)
                    parent.removeView(child)
                parent.addView(child)
                if (!type) {
                    linearTCs = child.findViewById(R.id.chartattach)
                } else {
                    linearTCsWidget = child.findViewById(R.id.chartattach)
                }
                val currAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_layout_main,
                    resources.getStringArray(R.array.currency)
                )
                currAdapter.setDropDownViewResource(R.layout.spinner_layout_main)
                spinnerPos = spinner.selectedItemPosition
                spinner.apply {
                    adapter = currAdapter
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?,
                            position: Int, id: Long
                        ) {
                            spinnerPos = spinner.selectedItemPosition
                            if (!type) {
                                if (!serviceData.isNullOrEmpty() && serviceData[0].size > 2) {
                                    createGraph(serviceData, false)
                                    fillGraph(false, serviceData, spinnerPos)
                                }
                            } else {
                                if (updateData.isNotEmpty()) {
                                    createGraph(updateData, true)
                                    fillGraph(true, updateData, widgetSpinnerValue)
                                }
                            }
                        }

                        override fun onNothingSelected(arg0: AdapterView<*>?) {}
                    }
                }
                val title = child.findViewById<TextView>(R.id.title)

                title.text = text
                val trashCan = child.findViewById<ImageView>(R.id.trashcan)
                trashCan.apply {
                    setOnClickListener {
                        if (!type)
                            GlobalScope.launch(Dispatchers.IO) {
                                calcViewModel.deleteServiceUpdateData(data)
                            }
                        else
                            calcViewModel.deleteWidgetUpdateData(widgetUpdateData)
                    }
                }
            }
        } else {
            parent.removeAllViews()
        }
    }

    /**
     * Creating and configuring the graph
     */
    private fun createGraph(data: Any, dataType: Boolean) {
        if (!dataType) {
            linearTCs = CalcLineChartBuilder.createGraph(linearTCs, data, dataType)
        } else {
            linearTCsWidget = CalcLineChartBuilder.createGraph(linearTCsWidget, data, dataType)
        }
    }

    private fun fillGraph(dataType: Boolean, data: Any, spinnerPos: Int) {
        if (!dataType)
            CalcLineChartBuilder.fillChart(linearTCs, spinnerPos, data, dataType)
        else
            CalcLineChartBuilder.fillChart(linearTCsWidget, spinnerPos, data, dataType)
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