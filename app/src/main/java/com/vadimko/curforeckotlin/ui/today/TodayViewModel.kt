package com.vadimko.curforeckotlin.ui.today

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vadimko.curforeckotlin.prefs.TodayPreferences
import com.vadimko.curforeckotlin.moexapi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexapi.MOEXRepository

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text


    fun getData(): MutableLiveData<List<CurrencyMOEX>> {
        if (data.value?.size == null) {
            data = MutableLiveData()
            val loadedPrefs = TodayPreferences.loadPrefs(getApplication())
            //Log.wtf("PREFSTODAY", "${loadedPrefs.component1()} ${loadedPrefs.component2()} ${loadedPrefs.component3()} ${loadedPrefs.component4()}" )
            loadDataMOEX(
                loadedPrefs.component1(),
                loadedPrefs.component2(),
                loadedPrefs.component3(),
                loadedPrefs.component4()
            )
            //TodayViewModel.loadDataMOEX(request)
        }
        return TodayViewModel.data
    }

    companion object {
        var data: MutableLiveData<List<CurrencyMOEX>> = MutableLiveData<List<CurrencyMOEX>>()

        //var dataCB: MutableLiveData<List<CurrencyCBjs>> =MutableLiveData<List<CurrencyCBjs>>()
        fun loadDataMOEX(request: String, from: String, till: String, interval: String) {

            val moexrepository: MOEXRepository = MOEXRepository()
            moexrepository.getMOEX(request, from, till, interval, false)

        }
        // fun loadDataCB() {
        //    val cbjsonrepository: CBjsonRepository = CBjsonRepository()
        //   cbjsonrepository.getCurrentCB()
        // }
    }
}