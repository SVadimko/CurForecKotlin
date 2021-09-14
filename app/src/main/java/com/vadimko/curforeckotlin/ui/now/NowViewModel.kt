package com.vadimko.curforeckotlin.ui.now

import android.content.Context
import android.graphics.Rect
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbjsonApi.CBjsonRepository
import com.vadimko.curforeckotlin.cbjsonApi.CurrencyCBjs
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowViewModel.Companion.dataCB
import com.vadimko.curforeckotlin.ui.now.NowViewModel.Companion.dataTCs
import com.vadimko.curforeckotlin.utils.CheckConnection
import com.vadimko.curforeckotlin.utils.CoinsAnimator
import com.vadimko.curforeckotlin.utils.LastValueHolder
import com.vadimko.curforeckotlin.utils.ScopeCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

/**
 * [ViewModel] for [NowFragment]
 * @property context Application context injected by Koin
 * @property dataTCs MutableStateFlow contains list of actual currency values [CurrencyTCS]
 * from Tinkov through [TCSRepository]
 * @property dataCB MutableStateFlow contains list of actual currency values [CurrencyCBjs]
 * from CB through [CBjsonRepository]
 * @property coinsAnimator realized on refresh rate animation
 * @property tcsRepository repository for retrofit request to Tinkov bank
 * @property cBjsonRepository repository for retrofit request to Central Bank
 * @property scopeCreator provide Coroutine context
 */
class NowViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()
    private lateinit var coinsAnimator: CoinsAnimator
    private val scopeCreator: ScopeCreator by inject()
    private val tcsRepository: TCSRepository by inject()
    private val cBjsonRepository: CBjsonRepository by inject()


    /**
     * If [dataTCs] value (Data from Tinkov server) is null- request data from server through [TCSRepository]
     * @return [dataTCs] MutableStateFlow list of [CurrencyTCS]
     */
    fun getDataTCs(): MutableStateFlow<List<CurrencyTCS>> {
        if (dataTCs.value[0].name == "") {
            loadDataTCs()
        }
        return dataTCs
    }

    /**
     * Get actual values of [CurrencyTCS] through [TCSRepository]  which post it to [dataTCs]
     */
    private fun loadDataTCs() {
        if (CheckConnection.checkConnect()) {
            scopeCreator.getScope().launch {
                var list: List<CurrencyTCS>
                do {
                    //list = Parser.parseTcsResponse(tcsRepository.getResponse())
                    list = tcsRepository.getResponse()
                } while (list.size != 3)
                //setDataTCs(list)
                dataTCs.value = list
                LastValueHolder.lastValueList = list
                withContext(Dispatchers.Main) { onRefreshRatesActions() }
            }
        }
    }


    /**
     * If [dataCB] value (Data from CB server) is null- request data from server through [CBjsonRepository]
     * @return [dataCB] MutableStateFlow list of [CurrencyCBjs]
     */
    fun getDataCB(): MutableStateFlow<List<CurrencyCBjs>> {
        if (dataCB.value[0].curr == "") {
            loadDataCB()
        }
        return dataCB
    }

    /**
     * Get actual values of [CurrencyCBjs] through [CBjsonRepository] which post it to [dataCB]
     */
    private fun loadDataCB() {
        if (CheckConnection.checkConnect()) {
            scopeCreator.getScope().launch {
                val list = cBjsonRepository.getResponse()
                dataCB.value = list
            }
        }
    }

    /**
     * Launching coroutines [loadDataTCs] and [loadDataCB] to get Tinkov [dataTCs] and CB [dataCB] from servers
     */
    fun startRefresh() {
        loadDataTCs()
        loadDataCB()
    }

    /**
     * Get display params form NowFragment and create animator class [CoinsAnimator]
     *
     */
    fun prepareAnimations(mScale: Float, mDisplaySize: Rect, layout: FrameLayout) {
        val layoutWeakReference: WeakReference<FrameLayout> = WeakReference(layout)
        coinsAnimator = CoinsAnimator(mScale, mDisplaySize, layoutWeakReference)
    }

    /**
     * Call to stop timer in animation class and delete added views to prevent memory leak
     */
    fun stopAnimation() {
        val onRefreshAnimation =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("onRefreshAnimation", true)
        if (onRefreshAnimation) {
            CoinsAnimator.stopAnimation()
        }
    }

    /**
     * Start animation after updating courses with [CoinsAnimator] if it enabled in
     * SharedPreferences and shows [Toast]
     */
    private fun onRefreshRatesActions() {
        val onRefreshAnimation =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("onRefreshAnimation", true)
        if (onRefreshAnimation) {
            coinsAnimator.coinsAnimate()
        }
        Toast.makeText(context, R.string.refreshed, Toast.LENGTH_SHORT).show()
    }

    /**
     * Companion object for operating with MutableStateFlow [dataTCs], [dataCB] and loading
     * it by [loadDataTCs], [loadDataCB]
     * @property dataTCs MutableStateFlow contains list of actual currency values [CurrencyTCS]
     * from Tinkov through [TCSRepository]
     * @property dataCB MutableStateFlow contains list of actual currency values [CurrencyCBjs]
     * from CB through [CBjsonRepository]
     */
    companion object : KoinComponent {

        private val dataTCs: MutableStateFlow<List<CurrencyTCS>> =
            MutableStateFlow(listOf(CurrencyTCS(), CurrencyTCS()))


        private val dataCB: MutableStateFlow<List<CurrencyCBjs>> =
            MutableStateFlow(listOf(CurrencyCBjs(), CurrencyCBjs()))
    }
}
