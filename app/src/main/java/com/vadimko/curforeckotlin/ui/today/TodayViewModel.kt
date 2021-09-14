package com.vadimko.curforeckotlin.ui.today

//import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel.Companion.loadDataMOEX
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexApi.MOEXRepository
import com.vadimko.curforeckotlin.ui.today.TodayViewModel.Companion.dataMOEX
import com.vadimko.curforeckotlin.utils.CheckConnection
import com.vadimko.curforeckotlin.utils.DateConverter
import com.vadimko.curforeckotlin.utils.ScopeCreator
import com.vadimko.curforeckotlin.utils.TodayPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * ViewModel for Today fragment
 * @property context Application context injected by Koin
 * @property moexRepository repository for retrofit request to MOEX
 * @property scopeCreator provide Coroutine context
 * @property dataMOEX MutableStateFlow contains list of actual currency values [CurrencyMOEX]
 * from MOEX through [MOEXRepository]
 */

class TodayViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()
    private val moexRepository: MOEXRepository by inject()
    private val scopeCreator: ScopeCreator by inject()


    /**
     * Load currencies values from MOEX through [CurrencyMOEX] which post it to [dataMOEX]
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
                        false
                    )
                dataMOEX.value = list
            }
        }
    }

    /**
     * If [dataMOEX] is null, load last user request params and send
     * request to server through [loadDataMOEX]
     * @return [dataMOEX] MutableStateFlow listof [CurrencyMOEX] from Moex
     */

    fun getData(): MutableStateFlow<List<CurrencyMOEX>> {
        if (dataMOEX.value[0].dates == "") {
            val loadedPrefs = TodayPreferences.loadPrefs()
            loadDataMOEX(
                loadedPrefs.component1(),
                loadedPrefs.component2(),
                loadedPrefs.component3(),
                loadedPrefs.component4()
            )
        }
        return dataMOEX
    }

    /**
     * Creates string for request to MOEX server API, launch [loadDataMOEX] and saved request in
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
        loadDataMOEX(jsonCurr, jsonDate[0], jsonDate[1], rates.toString())
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
     *  Companion object for operating with MutableStateFlow [dataMOEX] and loading
     *  it by [loadDataMOEX]
     * @property dataMOEX MutableStateFlow contains list of actual currency values [CurrencyMOEX]
     * from MOEX through [MOEXRepository]
     */
    companion object : KoinComponent {
        private val dataMOEX: MutableStateFlow<List<CurrencyMOEX>> = MutableStateFlow(
            listOf(
                CurrencyMOEX(), CurrencyMOEX(), CurrencyMOEX()
            )
        )
    }
}
