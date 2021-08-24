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
import com.vadimko.curforeckotlin.utils.CheckConnection
import com.vadimko.curforeckotlin.utils.CoinsAnimator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

/**
 * [ViewModel] for [NowFragment]
 * @property context Application context injected by Koin
 */
class NowViewModel : ViewModel(), KoinComponent {

    private val context: Context by inject()

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
     * If [dataCB] value (Data from CB server) is null- request data from server through [CBjsonRepository]
     * @return [dataCB] MutableStateFlow list of [CurrencyCBjs]
     */
    fun getDataCD(): MutableStateFlow<List<CurrencyCBjs>> {
        if (dataCB.value[0].curr == "") {
            loadDataCB()
        }
        return dataCB
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
     * Companion object for operating with MutableStateFlow [dataTCs], [dataCB] and loading
     * it by [loadDataTCs], [loadDataCB]
     * @property dataTCs MutableStateFlow contains list of actual currency values [CurrencyTCS]
     * from Tinkov through [TCSRepository]
     * @property dataCB MutableStateFlow contains list of actual currency values [CurrencyCBjs]
     * from CB through [CBjsonRepository]
     */
    companion object : KoinComponent {
        private val context: Context by inject()
        private lateinit var coinsAnimator: CoinsAnimator


        private val dataTCs: MutableStateFlow<List<CurrencyTCS>> =
            MutableStateFlow(listOf(CurrencyTCS(), CurrencyTCS()))

        /**
         * Set new data to [MutableStateFlow] [dataTCs] which contains actual currency rate values
         * from Tinkov bank to represent it NowFragment]
         */
        internal fun setDataTCs(data: List<CurrencyTCS>) {
            dataTCs.value = data
        }

        /**
         * @return  data of [MutableStateFlow] [dataTCs] which contains actual currency rate values
         */
        internal fun getDataForCalc(): StateFlow<List<CurrencyTCS>> {
            return dataTCs.asStateFlow()
        }

        private val dataCB: MutableStateFlow<List<CurrencyCBjs>> =
            MutableStateFlow(listOf(CurrencyCBjs(), CurrencyCBjs()))

        /**
         * Set new data to [MutableStateFlow] [dataCB] which contains actual currency rate values
         * from Central Bank bank to represent it NowFragment]
         */
        internal fun setDataCB(data: List<CurrencyCBjs>) {
            dataCB.value = data
        }

        /**
         * Get actual values of [CurrencyTCS] through [TCSRepository]  which post it to [dataTCs]
         */
        internal fun loadDataTCs() {
            if (CheckConnection.checkConnect()) {
                val tcsRepository = TCSRepository(false, null, null)
                tcsRepository.getCurrentTCS()
            }
        }

        /**
         * Get actual values of [CurrencyCBjs] through [CBjsonRepository] which post it to [dataCB]
         */
        internal fun loadDataCB() {
            if (CheckConnection.checkConnect()) {
                val cbJsonRepository = CBjsonRepository(false, null, null)
                cbJsonRepository.getCurrentCB()
            }
        }

        /**
         * Start animation after updating courses with [CoinsAnimator] if it enabled in SharedPreferences
         * and shows [Toast]
         */
        fun onRefreshRatesActions() {
            val onRefreshAnimation =
                PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("onRefreshAnimation", true)
            if (onRefreshAnimation) {
                coinsAnimator.coinsAnimate()
            }
            Toast.makeText(context, R.string.refreshed, Toast.LENGTH_SHORT).show()
        }
    }
}

