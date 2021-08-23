package com.vadimko.curforeckotlin.ui.calc

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.TCSUpdateService
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.utils.Saver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * ViewModel for Calc fragment
 * @property context Application context injected by Koin
 * @property rubValue Contains ruble result for buying and selling which calcs in [calculating]
 * @property dataWidgetUpdate Getting information about courses from the database,
 * which is updated every time the widget is updated
 */

class CalcViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()

    /**
     * Request to create/return [dataForCalc]
     */
    fun getDataForCalc(): LiveData<List<CurrencyTCS>> {
        if (dataForCalc.value?.size == null) {
            loadDataForCalc()
        }
        return dataForCalc
    }


    /**
     * Request to create/return [dataServiceUpdate]
     * @return data stored by [TCSUpdateService] dataServiceCalc
     */
    fun getServiceUpdateData(): MutableLiveData<List<List<CurrencyTCS>>> {
        if (dataServiceUpdate.value?.size == null) {
            loadServiceUpdateData()
        }
        return dataServiceUpdate
    }

    /**
     * Request to delete [dataServiceUpdate] data, except last value
     */
    fun deleteServiceUpdateData(data: List<List<CurrencyTCS>>) {
        Saver.deleteTcsLast(data)
        loadServiceUpdateData()
    }


    var rubValue: MutableLiveData<String> = MutableLiveData<String>()


    internal val dataWidgetUpdate = CurrenciesRepository.get().getCurrencies()

    /**
     * Request to delete [dataWidgetUpdate] data, except last value
     */
    fun deleteWidgetUpdateData(data: MutableList<Currencies>) {
        val currenciesRepository = CurrenciesRepository.get()
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
                rubValue.postValue(String.format(Locale.US, "%.2f", result))
            }
            if (toSell) {
                convertValue = currValue.toDouble()
                result = convertValue * buyValue
                rubValue.postValue(String.format(Locale.US, "%.2f", result))
            }
        } catch (ex: NumberFormatException) {
            Toast.makeText(context, R.string.incorrect_number, Toast.LENGTH_SHORT).show()
            //currValue.requestFocus()
        }

    }

    /**
     * Companion object for operating with LiveData [dataForCalc] [dataServiceUpdate]
     * @property dataForCalc Currency data used for calculating values
     * @property dataServiceUpdate Currency data saved as a result of work [TCSUpdateService]
     */
    companion object {


        private var dataForCalc: LiveData<List<CurrencyTCS>> = NowViewModel.getDataForCalc()

        /**
         * Load currencies values from Tinkov through [TCSRepository] which post it to [dataForCalc]
         */
        internal fun loadDataForCalc() {
            val tcsRepository = TCSRepository(false, null, null)
            tcsRepository.getCurrentTCS()
        }


        private var dataServiceUpdate: MutableLiveData<List<List<CurrencyTCS>>> =
            MutableLiveData<List<List<CurrencyTCS>>>()

        /**
         * Load currencies values from storage through [Saver] to [dataServiceUpdate]
         */
        internal fun loadServiceUpdateData() {
            GlobalScope.launch(Dispatchers.IO) {
                dataServiceUpdate.postValue(Saver.loadTcsLast())
            }
        }
    }
}