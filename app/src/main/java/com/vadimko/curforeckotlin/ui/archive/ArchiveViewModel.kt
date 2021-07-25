package com.vadimko.curforeckotlin.ui.archive

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.vadimko.curforeckotlin.DateConverter
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbxmlapi.CBXMLRepository
import com.vadimko.curforeckotlin.cbxmlapi.CurrencyCBarhive
import com.vadimko.curforeckotlin.moexapi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexapi.MOEXRepository
import com.vadimko.curforeckotlin.prefs.ArchivePreferences
import com.vadimko.curforeckotlin.updateWorkers.ArchiveMOEXWorker
import com.vadimko.curforeckotlin.updateWorkers.ArchiveWorker
import java.util.*
import kotlin.collections.ArrayList

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private var custCur = ""

    fun getData(): MutableLiveData<List<CurrencyCBarhive>> {
        if (data.value?.size == null) {
            val archPr = ArchivePreferences.loadPrefs(getApplication())
            loadCBArhieve(archPr[4], archPr[5], archPr[3])
        }
        return data
    }

    fun getData2(): MutableLiveData<List<CurrencyMOEX>> {
        if (dataMOEX.value?.size == null) {
            val archPr = ArchivePreferences.loadPrefs(getApplication())
            loadDataMOEX(
                archPr[6],
                archPr[7],
                archPr[8],
                archPr[9]
            )
        }
        return dataMOEX
    }

    //в зависимости от выбранных значений спиннеров формируем части запроса к сайтам
    fun createRequestStrings(choosen: Int, fromDate: Date, tillDate: Date) {
        var jsonCurr = ""
        var xmlCurr = ""
        val jsonDate: Array<String>
        val xmlDate: Array<String>
        when (choosen) {
            0 -> {
                jsonCurr = "USD000000TOD"
                xmlCurr = "R01235"
                custCur = "USD"
            }
            1 -> {
                jsonCurr = "EUR_RUB__TOD"
                xmlCurr = "R01239"
                custCur = "EUR"
            }
            2 -> {
                jsonCurr = "GBPRUB_TOD"
                xmlCurr = "R01035"
                custCur = "GBP"
            }
        }

        if (checkDates(fromDate, tillDate)) {
            val result: ArrayList<Array<String>> =
                DateConverter.getFromTillDate(fromDate, tillDate, context)
            jsonDate = result[0]
            xmlDate = result[1]
            startArchiveWorker(xmlDate[0], xmlDate[1], xmlCurr)
            startArchiveMOEXWorker(jsonCurr, jsonDate[0], jsonDate[1])
            ArchivePreferences.savePrefs(
                context, fromDate.time, tillDate.time, choosen, xmlCurr,
                xmlDate[0], xmlDate[1], jsonCurr, jsonDate[0], jsonDate[1], "24"
            )
        } else Toast.makeText(context, context.getString(R.string.ARCFRAGError), Toast.LENGTH_LONG)
            .show()
    }

    //проверяем корректность введенных дат
    private fun checkDates(from: Date, till: Date): Boolean {
        val tillLong = till.time
        val fromLong = from.time
        if (tillLong - fromLong > 63072000000)
            Toast.makeText(context, context.getString(R.string.choosedwarm), Toast.LENGTH_LONG)
                .show()
        return (till.compareTo(from)) > 0
    }

    //запуск воркера для получения данных с сайта ЦБ
    private fun startArchiveWorker(from: String, till: String, request: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            //.setRequiresCharging(true)
            .build()

        val data =
            workDataOf("request" to request, "from" to from, "till" to till)

        val workManager = WorkManager.getInstance(context)
        val myWorkRequest = OneTimeWorkRequest.Builder(
            ArchiveWorker::class.java//,
            //15,
            //TimeUnit.MINUTES,
            //15,
            //TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager.enqueue(myWorkRequest)
    }


    //запуск воркера для получения данных с сайта Московской биржи
    private fun startArchiveMOEXWorker(request: String, from: String, till: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data =
            workDataOf("request" to request, "from" to from, "till" to till)

        val workManager = WorkManager.getInstance(context)
        val myWorkRequest = OneTimeWorkRequest.Builder(
            ArchiveMOEXWorker::class.java//,
        )
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        workManager.enqueue(myWorkRequest)
    }

    companion object {
        //лайвдата данных от ЦБ
        var data: MutableLiveData<List<CurrencyCBarhive>> =
            MutableLiveData<List<CurrencyCBarhive>>()

        //лайвдата данных от МБ
        var dataMOEX: MutableLiveData<List<CurrencyMOEX>> = MutableLiveData<List<CurrencyMOEX>>()

        ////функциия вызова загрузки данных ЦБ
        fun loadCBArhieve(date_req1: String, date_req2: String, VAL_NM_RQ: String) {
            val cbxmlRepository = CBXMLRepository()
            cbxmlRepository.getXMLarchive(date_req1, date_req2, VAL_NM_RQ)
        }

        //функция вызова загрузки данных МБ
        fun loadDataMOEX(request: String, from: String, till: String, interval: String) {
            val moexRepository = MOEXRepository()
            moexRepository.getMOEX(request, from, till, interval, true)
        }
    }

}