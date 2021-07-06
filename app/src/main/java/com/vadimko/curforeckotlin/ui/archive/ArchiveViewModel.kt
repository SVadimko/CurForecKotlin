package com.vadimko.curforeckotlin.ui.archive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vadimko.curforeckotlin.cbxmlapi.CBXMLRepository
import com.vadimko.curforeckotlin.cbxmlapi.CurrencyCBarhive
import com.vadimko.curforeckotlin.moexapi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexapi.MOEXRepository
import com.vadimko.curforeckotlin.prefs.ArchivePreferences

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {


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