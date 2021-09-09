package com.vadimko.curforeckotlin.ui.calc

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.TCSUpdateService
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.utils.Saver
import com.vadimko.curforeckotlin.utils.ScopeCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * ViewModel for Calc fragment
 * @property context Application context injected by Koin
 * @property rubValue Contains ruble result for buying and selling which calcs in [calculating] as
 * [MutableStateFlow]
 * @property dataWidgetUpdate Getting information about courses from the database,
 * which is updated every time the widget is updated
 */

class CalcViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()

    private val currenciesRepository: CurrenciesRepository by inject()

    private var rubValue: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * @return [rubValue] MutableStateFlow
     */
    fun getRubvalue() = rubValue


    /**
     * Request to create/return [dataForCalc]
     */
    fun getDataForCalc(): StateFlow<List<CurrencyTCS>> {
        if (dataForCalc.value[0].name == "") {
            loadDataForCalc()
        }
        return dataForCalc
    }

    /**
     * Request to create/return [dataServiceUpdate]
     * @return data data stored by [TCSUpdateService] dataServiceCalc
     */
    fun getServiceUpdateData(): MutableLiveData<List<List<CurrencyTCS>>> {
        if (dataServiceUpdate.value?.size == null) {
            loadServiceUpdateData()
        }
        return dataServiceUpdate
    }


    /**
     * Request to delete [dataServiceUpdate] data, except last value, if already deleted, show [Toast]
     */
    fun deleteServiceUpdateData(data: List<List<CurrencyTCS>>) {
        Saver.deleteTcsLast(data)
        loadServiceUpdateData()

    }


    private val dataWidgetUpdate = currenciesRepository.getCurrencies().asLiveData()

    /**
     * @return [dataWidgetUpdate]
     */
    fun getDataWidgetUpdate() = dataWidgetUpdate


    /**
     * Request to delete [dataWidgetUpdate] data, except last value, if already deleted, show [Toast]
     */
    fun deleteWidgetUpdateData(data: MutableList<Currencies>) {
        //val currenciesRepository = CurrenciesRepository.get()
        currenciesRepository.clearCurrencies(data)
    }


    /**
     * Fun perform result of calculating for buing and selling currency
     */
    fun calculating(
        currSpinnerPos: Int,
        dataList: List<CurrencyTCS>,
        toBuy: Boolean,
        toSell: Boolean,
        currValue: String
    ) {
        val usdBuy = dataList[0].buy!!
        val usdSell = dataList[0].sell!!
        val eurBuy = dataList[1].buy!!
        val eurSell = dataList[1].sell!!
        val gbpBuy = dataList[2].buy!!
        val gbpSell = dataList[2].sell!!
        var result: Double
        var convertValue: Double
        var buyValue = 0.0
        var sellValue = 0.0
        try {
            when (currSpinnerPos) {
                0 -> {
                    buyValue = usdBuy
                    sellValue = usdSell
                }
                1 -> {
                    buyValue = eurBuy
                    sellValue = eurSell
                }
                2 -> {
                    buyValue = gbpBuy
                    sellValue = gbpSell
                }
            }
            if (toBuy) {
                convertValue = currValue.toDouble()
                result = convertValue * sellValue
                rubValue.value = String.format(Locale.US, "%.2f", result)
            }
            if (toSell) {
                convertValue = currValue.toDouble()
                result = convertValue * buyValue
                rubValue.value = String.format(Locale.US, "%.2f", result)
            }
        } catch (ex: NumberFormatException) {
            Toast.makeText(context, R.string.incorrect_number, Toast.LENGTH_SHORT).show()
            //currValue.requestFocus()
        }

    }

    /**
     * Companion object for operating with StateFlow [dataForCalc] [dataServiceUpdate]
     * @property dataForCalc Currency data used for calculating values
     * @property dataServiceUpdate Currency data saved as a result of work [TCSUpdateService]
     */
    companion object : KoinComponent {
        private val scopeCreator: ScopeCreator by inject()
        private val tcsRepository: TCSRepository by inject()

        private var dataForCalc: StateFlow<List<CurrencyTCS>> = NowViewModel.getDataForCalc()

        /**
         * Load currencies values from Tinkov through [TCSRepository] which post it to [dataForCalc]
         */
        internal fun loadDataForCalc() {
            tcsRepository.getCurrentTCS(false, null, null)
        }


        private var dataServiceUpdate: MutableLiveData<List<List<CurrencyTCS>>> =
            MutableLiveData<List<List<CurrencyTCS>>>()

        /**
         * Load currencies values from storage through [Saver] to [dataServiceUpdate]
         */
        internal fun loadServiceUpdateData() {
            scopeCreator.getScope().launch(Dispatchers.IO) {
                dataServiceUpdate.postValue(Saver.loadTcsLast())
            }
        }
    }
}