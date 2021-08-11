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
 * viewModel for Calc fragment
 */

class CalcViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    fun getData(): MutableLiveData<List<CurrencyTCS>> {
        if (data.value?.size == null) {
            loadDataTCS()
        }
        return data
    }

    fun getDataList(): MutableLiveData<List<List<CurrencyTCS>>> {
        if (data2.value?.size == null) {
            loadGraphData()
        }
        return data2
    }

    var rubValue: MutableLiveData<String> = MutableLiveData<String>()

    //getting information about courses from the database,
    // which is updated every time the widget is updated
    val livedataTKS = CurrenciesRepository.get().getCurrencies()

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

    companion object {
        //course data used to calculate the calculator values
        var data: MutableLiveData<List<CurrencyTCS>> = NowViewModel.data
        fun loadDataTCS() {

            val tcsRepository = TCSRepository()
            tcsRepository.getCurrentTCS()
        }

        //course data saved as a result of auto-update
        var data2: MutableLiveData<List<List<CurrencyTCS>>> =
            MutableLiveData<List<List<CurrencyTCS>>>()

        fun loadGraphData() {
            GlobalScope.launch(Dispatchers.IO) {
                data2.postValue(Saver.loadTcsLast())
            }
        }
    }
}