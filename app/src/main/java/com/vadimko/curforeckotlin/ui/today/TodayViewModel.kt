package com.vadimko.curforeckotlin.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.vadimko.curforeckotlin.DateConverter
import com.vadimko.curforeckotlin.prefs.TodayPreferences
import com.vadimko.curforeckotlin.moexapi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexapi.MOEXRepository
import com.vadimko.curforeckotlin.updateWorkers.TodayWorker
import java.util.*

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private var recCur = ""
    private var recDay = "0"


    fun getData(): MutableLiveData<List<CurrencyMOEX>> {
        if (data.value?.size == null) {
            data = MutableLiveData()
            val loadedPrefs = TodayPreferences.loadPrefs(getApplication())
            loadDataMOEX(
                loadedPrefs.component1(),
                loadedPrefs.component2(),
                loadedPrefs.component3(),
                loadedPrefs.component4()
            )
        }
        return data
    }

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
                recDay = "1 день"
            }
            1 -> {
                recDays = 2
                recDay = "2 дня"
            }
            2 -> {
                recDays = 3
                recDay = "3 дня"
            }
            3 -> {
                recDays = 4
                recDay = "4 дня"
            }
            4 -> {
                recDays = 5
                recDay = "5 дней"
            }
        }
        when (choosen[2]) {
            0 -> rates = 1
            1 -> rates = 10
            2 -> rates = 60
        }
        val till = Date(System.currentTimeMillis())
        val from = Date(System.currentTimeMillis() - 86400000 * recDays)
        val result = DateConverter.getFromTillDate(from, till, context)
        jsonDate = result[0]
        startTodayWorker(jsonCurr, jsonDate[0], jsonDate[1], rates.toString())
        TodayPreferences.savePrefs(
            context,
            jsonCurr,
            jsonDate[0],
            jsonDate[1],
            rates.toString(),
            currSpinnerPos,
            perSpinnerPos,
            rateSpinnerPos
        )

    }

    //конфигурируем и запускаем воркер для обновления данных за указанный период
    private fun startTodayWorker(request: String, from: String, till: String, interval: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data =
            workDataOf("request" to request, "from" to from, "till" to till, "interval" to interval)
        val workManager =  WorkManager.getInstance(context)
        val myWorkRequest = OneTimeWorkRequest.Builder(
            TodayWorker::class.java//,
        )
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager.enqueue(myWorkRequest)
    }

    companion object {
        var data: MutableLiveData<List<CurrencyMOEX>> = MutableLiveData<List<CurrencyMOEX>>()

        fun loadDataMOEX(request: String, from: String, till: String, interval: String) {

            val moexRepository = MOEXRepository()
            moexRepository.getMOEX(request, from, till, interval, false)

        }
    }
}