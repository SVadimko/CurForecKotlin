package com.vadimko.curforeckotlin.ui.calc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vadimko.curforeckotlin.Saver
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsapi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowViewModel

class CalcViewModel : ViewModel() {

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

    //получение информации о курсах из базы данных, которая дополняется каждый раз при обновлении виджета
    val livedataTKS = CurrenciesRepository.get().getCurrencies()

    companion object {
        //данные о курсах, которые используются при рассчете значений калькулятора
        var data: MutableLiveData<List<CurrencyTCS>> = NowViewModel.data
        fun loadDataTCS() {
            val tcsRepository = TCSRepository()
            tcsRepository.getCurrentTCS()
        }

        //данные о курсах, сохранненые в результате автообновления
        var data2: MutableLiveData<List<List<CurrencyTCS>>> =
            MutableLiveData<List<List<CurrencyTCS>>>()

        fun loadGraphData() {
            val saver = Saver()
            data2.postValue(saver.loadTcslast())
        }
    }
}