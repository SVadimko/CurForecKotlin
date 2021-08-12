package com.vadimko.curforeckotlin.ui.calc

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.Saver
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for Calc fragment
 */

class CalcViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    fun getData(): MutableLiveData<List<CurrencyTCS>> {
        if (dataForCalc.value?.size == null) {
            loadDataTCS()
        }
        return dataForCalc
    }

    fun getDataList(): MutableLiveData<List<List<CurrencyTCS>>> {
        if (dataAutoUpdate.value?.size == null) {
            loadGraphData()
        }
        return dataAutoUpdate
    }

    /**
     * @property rubValue contains ruble result for buying and selling which calcs in [calculating]
     */
    var rubValue: MutableLiveData<String> = MutableLiveData<String>()

    /**
     * @property liveDataTKS getting information about courses from the database,
     * which is updated every time the widget is updated
     */
    val liveDataTKS = CurrenciesRepository.get().getCurrencies()

    /**
     * fun perform result of calculating for buing and selling currency
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
     * @property dataForCalc currency data used for calculating values
     * @property dataAutoUpdate currency data saved as a result of auto-update
     */
    companion object {
        var dataForCalc: MutableLiveData<List<CurrencyTCS>> = NowViewModel.data

        /**
         * load currencies values from Tinkov through [TCSRepository] which post it to [dataForCalc]
         */
        fun loadDataTCS() {
            val tcsRepository = TCSRepository()
            tcsRepository.getCurrentTCS()
        }

        var dataAutoUpdate: MutableLiveData<List<List<CurrencyTCS>>> =
            MutableLiveData<List<List<CurrencyTCS>>>()

        /**
         * load currencies values from storage through [Saver] to [dataAutoUpdate]
         */
        fun loadGraphData() {
            GlobalScope.launch(Dispatchers.IO) {
                dataAutoUpdate.postValue(Saver.loadTcsLast())
            }
        }
    }
}