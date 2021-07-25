package com.vadimko.curforeckotlin.ui.now

import android.app.Application
import android.graphics.Rect
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.CoinsAnimator
import com.vadimko.curforeckotlin.cbjsonapi.CBJsonRepository
import com.vadimko.curforeckotlin.cbjsonapi.CurrencyCBjs
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsapi.TCSRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NowViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

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

    //конфигурирование и запуск воркера для обновления данных о курсах
    fun startWorker() {
        loadDataTCS()
        loadDataCB()
    }

    //запуск анимации после обновления курсов
    fun startAnimations(mScale: Float, mDisplaySize: Rect, mRootLayout: FrameLayout) {
        val onRefreshAnimation =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("onRefreshAnimation", true)
        if (onRefreshAnimation) {
            val coinsAnimator = CoinsAnimator(mScale, mDisplaySize, mRootLayout, context)
            coinsAnimator.weatherAnimationSnow()
        }
        //Toast.makeText(context, R.string.refreshed, Toast.LENGTH_SHORT).show()
    }

    companion object {
        //лайвдата, получаемая с сайта тиньков
        var data: MutableLiveData<List<CurrencyTCS>> = MutableLiveData<List<CurrencyTCS>>()

        //лайвдата, получаемая с сайта ЦБ
        var dataCB: MutableLiveData<List<CurrencyCBjs>> = MutableLiveData<List<CurrencyCBjs>>()

        fun loadDataTCS() {
            GlobalScope.launch(Dispatchers.IO) {
                val tcsRepository = TCSRepository()
                tcsRepository.getCurrentTCS()
            }
        }

        fun loadDataCB() {
            GlobalScope.launch(Dispatchers.IO) {
                val cbJsonRepository = CBJsonRepository()
                cbJsonRepository.getCurrentCB()
            }
        }
    }
}

