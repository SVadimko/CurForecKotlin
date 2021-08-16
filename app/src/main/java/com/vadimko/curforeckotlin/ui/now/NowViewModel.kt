package com.vadimko.curforeckotlin.ui.now

import android.app.Application
import android.graphics.Rect
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.CoinsAnimator
import com.vadimko.curforeckotlin.cbjsonApi.CBJsonRepository
import com.vadimko.curforeckotlin.cbjsonApi.CurrencyCBjs
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * ViewModel for Now fragment
 */

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

    /**
     * launching coroutines to get Tinkov and CB
     */
    fun startRefresh() {
        loadDataTCS()
        loadDataCB()
    }

    /**
     * start animation after updating courses with [CoinsAnimator]
     */
    fun startAnimations(mScale: Float, mDisplaySize: Rect, mRootLayout: FrameLayout) {
        val onRefreshAnimation =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("onRefreshAnimation", true)
        if (onRefreshAnimation) {
            val coinsAnimator = CoinsAnimator(mScale, mDisplaySize, mRootLayout, context)
            coinsAnimator.coinsAnimate()
        }
        //Toast.makeText(context, R.string.refreshed, Toast.LENGTH_SHORT).show()
    }

    /**
     * @property data MutableLiveData contains list of actual currency values [CurrencyTCS] from Tinkov through [TCSRepository]
     * @property dataCB MutableLiveData contains list of actual currency values [CurrencyCBjs] from CB through [CBJsonRepository]
     */
    companion object {

        internal var data: MutableLiveData<List<CurrencyTCS>> = MutableLiveData<List<CurrencyTCS>>()

        internal var dataCB: MutableLiveData<List<CurrencyCBjs>> =
            MutableLiveData<List<CurrencyCBjs>>()

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

