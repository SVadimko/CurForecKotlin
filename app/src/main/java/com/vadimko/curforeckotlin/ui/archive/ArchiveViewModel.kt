package com.vadimko.curforeckotlin.ui.archive

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbxmlApi.CBxmlRepository
import com.vadimko.curforeckotlin.cbxmlApi.CurrencyCBarhive
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexApi.MOEXRepository
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel.Companion.dataCB
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel.Companion.dataMOEX
import com.vadimko.curforeckotlin.utils.ArchivePreferences
import com.vadimko.curforeckotlin.utils.CheckConnection
import com.vadimko.curforeckotlin.utils.DateConverter
import com.vadimko.curforeckotlin.utils.ScopeCreator
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * ViewModel for Archive fragment
 * @property context Application context injected by Koin
 * @property moexRepository repository for retrofit request to MOEX
 * @property cbxmlRepository repository for retrofit request to Central Bank
 * @property scopeCreator provide Coroutine context
 * @property dataCB MutableStateFlow contains list of actual currency values [CurrencyCBarhive]
 * from CB through [CBxmlRepository]
 * @property dataMOEX MutableStateFlow contains list of actual currency values [CurrencyMOEX]
 * from MOEX through [MOEXRepository]
 */

class ArchiveViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()
    private val moexRepository: MOEXRepository by inject()
    private val cbxmlRepository: CBxmlRepository by inject()
    private val scopeCreator: ScopeCreator by inject()


    /**
     * If [dataCB] is null get last request from [ArchivePreferences] and load it by [loadCBArchive]
     * @return [dataCB]
     */
    fun getDataCB(): MutableLiveData<List<CurrencyCBarhive>> {
        if (dataCB.value?.size == null) {
            val archPr = ArchivePreferences.loadPrefs()
            loadCBArchive(archPr[4], archPr[5], archPr[3])
        }
        return dataCB
    }


    /**
     * Load currencies values from CB through [CBxmlRepository] and post it to [dataCB]
     */
    private fun loadCBArchive(date_req1: String, date_req2: String, VAL_NM_RQ: String) {
        if (CheckConnection.checkConnect()) {
            scopeCreator.getScope().launch {
                val list: List<CurrencyCBarhive> =
                    cbxmlRepository.getResponse(date_req1, date_req2, VAL_NM_RQ)
                dataCB.postValue(list)
            }
        }
    }


    /**
     * If [dataMOEX] is null, load last user request params from [ArchivePreferences] and send
     * request to server through [loadDataMOEX]
     * @return [dataMOEX]
     */
    fun getDataMOEX(): MutableLiveData<List<CurrencyMOEX>> {
        if (dataMOEX.value?.size == null) {
            val archPr = ArchivePreferences.loadPrefs()
            loadDataMOEX(
                archPr[6],
                archPr[7],
                archPr[8],
                archPr[9]
            )
        }
        return dataMOEX
    }


    /**
     * Depending on the selected values of the spinners, forms parts of the request to the server
     * and send it via [loadDataMOEX] or [loadCBArchive]
     * @param currChosen Spinner currency selector position
     * @param fromDate "From" date using in request to server
     * @param tillDate "Till" date using in request to server
     */
    fun createRequestStrings(currChosen: Int, chartChosen: Int, fromDate: Date, tillDate: Date) {
        var jsonCurr = ""
        var xmlCurr = ""
        val jsonDate: Array<String>
        val xmlDate: Array<String>
        when (currChosen) {
            0 -> {
                jsonCurr = "USD000000TOD"
                xmlCurr = "R01235"
            }
            1 -> {
                jsonCurr = "EUR_RUB__TOD"
                xmlCurr = "R01239"
            }
            2 -> {
                jsonCurr = "GBPRUB_TOD"
                xmlCurr = "R01035"
            }
        }

        if (checkDates(fromDate, tillDate)) {
            val result: ArrayList<Array<String>> =
                DateConverter.getFromTillDate(fromDate, tillDate)
            jsonDate = result[0]
            xmlDate = result[1]
            loadCBArchive(xmlDate[0], xmlDate[1], xmlCurr)
            loadDataMOEX(jsonCurr, jsonDate[0], jsonDate[1], "24")
            ArchivePreferences.savePrefs(
                fromDate.time, tillDate.time, currChosen, xmlCurr,
                xmlDate[0], xmlDate[1], jsonCurr, jsonDate[0], jsonDate[1], "24", chartChosen
            )
        } else showToast(context.getString(R.string.ARCFRAGError))
    }

    /**
     * Load currencies values from MOEX through [MOEXRepository] and post it to [dataMOEX]
     */
    private fun loadDataMOEX(request: String, from: String, till: String, interval: String) {
        if (CheckConnection.checkConnect()) {
            scopeCreator.getScope().launch {
                val list: List<CurrencyMOEX> =
                    moexRepository.getResponse(
                        request,
                        from,
                        till,
                        interval,
                        true
                    )
                dataMOEX.postValue(list)
            }
        }
    }

    /**
     * Checking correct chosen input dates
     * @param from "From" date using in request to server
     * @param till "Till" date using in request to server
     * @return true if "from" and "till" set correctly
     */
    private fun checkDates(from: Date, till: Date): Boolean {
        val tillLong = till.time
        val fromLong = from.time
        if (tillLong - fromLong > 63072000000)
            showToast(context.getString(R.string.choosedwarn))
        return (till.compareTo(from)) > 0
    }


    /**
     * Show warning messages if data received from CB or/and MOEX is not enough to build graph
     */
    fun showToast(s: String) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }


    /**
     * Companion object for operating with MutableStateFlow [dataCB], [dataMOEX] and load them through
     * [loadCBArchive], [loadDataMOEX]
     * @property dataCB MutableStateFlow contains list of actual currency values [CurrencyCBarhive]
     * from CB through [CBxmlRepository]
     * @property dataMOEX MutableStateFlow contains list of actual currency values [CurrencyMOEX]
     * from MOEX through [MOEXRepository]
     */
    companion object : KoinComponent {
        private var dataCB: MutableLiveData<List<CurrencyCBarhive>> =
            MutableLiveData<List<CurrencyCBarhive>>()
        private var dataMOEX: MutableLiveData<List<CurrencyMOEX>> =
            MutableLiveData<List<CurrencyMOEX>>()
    }
}
