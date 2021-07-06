package com.vadimko.curforeckotlin.ui.now

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.vadimko.curforeckotlin.*
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.adapters.CBmainAdapter
import com.vadimko.curforeckotlin.adapters.TCSmainAdapter
import com.vadimko.curforeckotlin.cbjsonapi.CurrencyCBjs
import com.vadimko.curforeckotlin.databinding.FragmentNowBinding
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import com.vadimko.curforeckotlin.updateWorkers.NowWorker


/*
private const val USD_BUY = "usdbuy"
private const val USD_SELL = "usdsell"
private const val EUR_BUY = "eurbuy"
private const val EUR_SELL = "eursell"
private const val GBP_BUY = "gbpbuy"
private const val GBP_SELL = "gbpsell"
*/

class NowFragment : Fragment() {

    private val nowViewModel: NowViewModel by lazy {
        ViewModelProviders.of(this).get(NowViewModel::class.java)
    }
    private var _binding: FragmentNowBinding? = null

    private val binding get() = _binding!!
    private lateinit var root: View
    private lateinit var tcsRecycle: RecyclerView
    private lateinit var cbRecycle: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNowBinding.inflate(inflater, container, false)
        root = binding.root

        tcsRecycle = root.findViewById(R.id.recycletinkoff)
        tcsRecycle.layoutManager = LinearLayoutManager(context)

        cbRecycle = root.findViewById(R.id.recyclecbrf)
        cbRecycle.layoutManager = LinearLayoutManager(context)
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //подписка на данные получаемые с сервера ЦБ
        nowViewModel.getData().observe(viewLifecycleOwner, { forecTCS ->
            forecTCS?.let {
                setupAdapterTCS(forecTCS)
                val lastChk = root.findViewById<TextView>(R.id.lastchk)
                lastChk.text =
                    "${getString(R.string.lastupdateTCS)} ${forecTCS[0].curr} ${getString(R.string.NOWFRAGsource)} tinkoff.ru"
                //fillBundle(forecTCS)
                CalcViewModel.data.postValue(forecTCS)
            }
        })
        //подписка на данные получаемые с сервера Тиньков
        nowViewModel.getData2().observe(viewLifecycleOwner, { forecCB ->
            forecCB?.let {
                setupAdapterCB(forecCB)
                val lastChck = root.findViewById<TextView>(R.id.lastchk_cbrf)
                lastChck.text =
                    "${getString(R.string.lastupdateTCS)} ${forecCB[0].datetime} ${getString(R.string.NOWFRAGsource)} cbr-xml-daily.ru"
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //конфигурирование и запуск воркера для обновления данных о курсах
    private fun startWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            //.setRequiresCharging(true)
            .build()

        //val datastring: String = "inputDATA once"
        //val randomData = workDataOf("dt" to datastring)
        val workManager = context?.let { WorkManager.getInstance(it) }
        val myWorkRequest = OneTimeWorkRequest.Builder(
            NowWorker::class.java
        )
            .setConstraints(constraints)
            //.setInputData(randomData)
            .build()
        workManager?.enqueue(myWorkRequest)
    }

    private fun setupAdapterTCS(tcsForec: List<CurrencyTCS>) {
        tcsRecycle.adapter = TCSmainAdapter(tcsForec)
    }

    private fun setupAdapterCB(cbForec: List<CurrencyCBjs>) {
        cbRecycle.adapter = CBmainAdapter(cbForec)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val settings = Intent(activity, SettingsActivity::class.java)
                startActivity(settings)
                return true
            }
            R.id.refresh -> {
                startWorker()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}