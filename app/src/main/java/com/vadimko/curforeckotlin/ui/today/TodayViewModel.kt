package com.vadimko.curforeckotlin.ui.today

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexApi.MOEXRepository
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel.Companion.loadDataMOEX
import com.vadimko.curforeckotlin.ui.now.NowViewModel.Companion.dataTCs
import com.vadimko.curforeckotlin.updateWorkers.TodayWorker
import com.vadimko.curforeckotlin.utils.DateConverter
import com.vadimko.curforeckotlin.utils.TodayPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * ViewModel for Today fragment
 * @property context Application context injected by Koin
 */

class TodayViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()

    /**
     * If [dataTCs] is null, load last user request params from [TodayWorker] and send
     * request to server through [loadDataMOEX]
     * @return [dataTCs] MutableLiveData listof [CurrencyMOEX] from Moex
     */
    fun getData(): MutableLiveData<List<CurrencyMOEX>> {
        if (data.value?.size == null) {
            data = MutableLiveData()
            val loadedPrefs = TodayPreferences.loadPrefs()
            loadDataMOEX(
                loadedPrefs.component1(),
                loadedPrefs.component2(),
                loadedPrefs.component3(),
                loadedPrefs.component4()
            )
        }
        return data
    }

    /**
     * Creates string for request to MOEX server API, launch [startTodayWorker] and saved request in
     * SharedPreferences
     */
    fun createRequestStrings(
        chosen: IntArray,
        currSpinnerPos: Int,
        perSpinnerPos: Int,
        rateSpinnerPos: Int
    ) {
        var jsonCurr = ""
        val jsonDate: Array<String>
        var recDays = 0L
        var rates = 0
        when (chosen[0]) {
            0 -> {
                jsonCurr = "USD000000TOD"
            }
            1 -> {
                jsonCurr = "EUR_RUB__TOD"
            }
            2 -> {
                jsonCurr = "GBPRUB_TOD"
            }
        }
        when (chosen[1]) {
            0 -> {
                recDays = 1
            }
            1 -> {
                recDays = 2
            }
            2 -> {
                recDays = 3
            }
            3 -> {
                recDays = 4
            }
            4 -> {
                recDays = 5
            }
        }
        when (chosen[2]) {
            0 -> rates = 1
            1 -> rates = 10
            2 -> rates = 60
        }
        val till = Date(System.currentTimeMillis())
        val from = Date(System.currentTimeMillis() - 86400000 * recDays)
        val result = DateConverter.getFromTillDate(from, till)
        jsonDate = result[0]
        startTodayWorker(jsonCurr, jsonDate[0], jsonDate[1], rates.toString())
        TodayPreferences.savePrefs(
            jsonCurr,
            jsonDate[0],
            jsonDate[1],
            rates.toString(),
            currSpinnerPos,
            perSpinnerPos,
            rateSpinnerPos
        )

    }

    /**
     * Configure and launch worker [TodayWorker] to receive currencies values for requested days
     */
    private fun startTodayWorker(request: String, from: String, till: String, interval: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data =
            workDataOf("request" to request, "from" to from, "till" to till, "interval" to interval)
        val workManager = WorkManager.getInstance(context)
        val myWorkRequest = OneTimeWorkRequest.Builder(
            TodayWorker::class.java//,
        )
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager.enqueue(myWorkRequest)
    }

    /**
     * Shows warning [Toast] to inform uaer to choose interval 1,10 min in case when server return not
     * enough data for selected interval
     */
    fun showToast() {
        Toast.makeText(
            context,
            context.getString(R.string.TODAYFRAGchoosediffinterval),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     *  Companion object for operating with LiveData [data] and loading it by [loadDataMOEX]
     * @property data MutableLiveData contains list of actual currency values [CurrencyMOEX] from MOEX through [MOEXRepository]
     */
    companion object {
        internal var data: MutableLiveData<List<CurrencyMOEX>> =
            MutableLiveData<List<CurrencyMOEX>>()

        /**
         * Load currencies values from MOEX through [CurrencyMOEX] which post it to [dataTCs]
         */
        fun loadDataMOEX(request: String, from: String, till: String, interval: String) {
            val moexRepository = MOEXRepository()
            moexRepository.getMOEX(request, from, till, interval, false)
        }
    }
}