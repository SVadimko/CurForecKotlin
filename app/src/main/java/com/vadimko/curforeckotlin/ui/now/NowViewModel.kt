package com.vadimko.curforeckotlin.ui.now

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vadimko.curforeckotlin.cbjsonapi.CBjsonRepository
import com.vadimko.curforeckotlin.cbjsonapi.CurrencyCBjs
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsapi.TCSRepository

class NowViewModel : ViewModel() {


    fun getData(): MutableLiveData<List<CurrencyTCS>> {
        if (data.value?.size == null) {
            loadDataTCS()
        }
        return data
    }

    fun getData2(): MutableLiveData<List<CurrencyCBjs>> {
        if (dataCB.value?.size == null) {
            loadDataCB()
        }
        return dataCB
    }

    companion object {
        //лайвдата, получаемая с сайта тиньков
        var data: MutableLiveData<List<CurrencyTCS>> = MutableLiveData<List<CurrencyTCS>>()

        //лайвдата, получаемая с сайта ЦБ
        var dataCB: MutableLiveData<List<CurrencyCBjs>> = MutableLiveData<List<CurrencyCBjs>>()

        fun loadDataTCS() {
            val tcsRepository = TCSRepository()
            tcsRepository.getCurrentTCS()
        }

        fun loadDataCB() {
            val cbjsonRepository = CBjsonRepository()
            cbjsonRepository.getCurrentCB()
        }
    }

}
