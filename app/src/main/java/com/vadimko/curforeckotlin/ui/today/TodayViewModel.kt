package com.vadimko.curforeckotlin.ui.today

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexApi.MOEXRepository
import com.vadimko.curforeckotlin.ui.now.NowViewModel.Companion.data
import com.vadimko.curforeckotlin.updateWorkers.TodayWorker
import com.vadimko.curforeckotlin.utils.DateConverter
import com.vadimko.curforeckotlin.utils.TodayPreferences
import java.util.*

/**
 * ViewModel for Today fragment
 */

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private var recCur = ""


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
     * Creates string for request to MOEX server API
     */
    fun createRequestStrings(
        choosen: IntArray,
        currSpinnerPos: Int,
        perSpinnerPos: Int,
        rateSpinnerPos: Int
    ) {
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
        when (choosen[2]) {
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
     * Configure and launch worker to receive currencies values for requested days through [TodayWorker]
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

    fun showToast() {
        Toast.makeText(
            context,
            context.getString(R.string.TODAYFRAGchoosediffinterval),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * @property data MutableLiveData contains list of actual currency values [CurrencyMOEX] from MOEX through [MOEXRepository]
     */
    companion object {
        internal var data: MutableLiveData<List<CurrencyMOEX>> =
            MutableLiveData<List<CurrencyMOEX>>()

        /**
         * Load currencies values from MOEX through [CurrencyMOEX] which post it to [data]
         */
        fun loadDataMOEX(request: String, from: String, till: String, interval: String) {
            val moexRepository = MOEXRepository()
            moexRepository.getMOEX(request, from, till, interval, false)
        }
    }
}