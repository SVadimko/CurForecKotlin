package com.vadimko.curforeckotlin.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbjsonApi.CBjsonRepository
import com.vadimko.curforeckotlin.cbjsonApi.CurrencyCBjs
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.utils.DateConverter
import com.vadimko.curforeckotlin.utils.Parser
import com.vadimko.curforeckotlin.utils.ScopeCreator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Perform request to servers to get Tinkov and CB data
 * @param appWidgetID Id of updating widget
 * @param appWidgetManager Application widget Manager
 */

class WidgetUpdater(context: Context, appWidgetManager: AppWidgetManager, appWidgetID: Int) :
    KoinComponent {
    private val mappWidgetManager = appWidgetManager
    private val mappWidgetID = appWidgetID
    private val currenciesRepository: CurrenciesRepository by inject()
    private val mContext = context
    //private val tcsRepository: TCSRepository by inject()
    //private val cBjsonRepository: CBjsonRepository by inject()

    init {
        updateTCs()
        updateCB()
    }

    /**
     * Performs Retrofit [TCSRepository] request to Tinkov server
     */
    private fun updateTCs() {
        val tcsRepository = TCSRepository()
        //tcsRepository.getCurrentTCS(true, mappWidgetManager, mappWidgetID)
        GlobalScope.launch {
            var list: List<CurrencyTCS> = listOf()
            do {
                //list = Parser.parseTcsResponse(tcsRepository.getResponse())
                list = tcsRepository.getResponse()
            } while (list.size != 3)
            refreshTCsPart(list)
        }
    }

    /**
     * Performs Retrofit [CBjsonRepository] request to CB server
     */
    private fun updateCB() {
        val cBjsonRepository = CBjsonRepository()
        //cbJsonRepository.getCurrentCB(true, mappWidgetManager, mappWidgetID)
        GlobalScope.launch {
            val list = cBjsonRepository.getResponse()
            refreshCBPart(list)
        }
    }

    private fun refreshTCsPart(currentTCS: List<CurrencyTCS>) {
        val usdBuy = currentTCS[0].buy
        val usdSell = currentTCS[0].sell
        val eurBuy = currentTCS[1].buy
        val eurSell = currentTCS[1].sell
        val gbpBuy = currentTCS[2].buy
        val gbpSell = currentTCS[2].sell


        currenciesRepository.insertCurrencies(
            Currencies(
                usdBuy = usdBuy!!,
                usdSell = usdSell!!,
                eurBuy = eurBuy!!,
                eurSell = eurSell!!,
                gbpBuy = gbpBuy!!,
                gbpSell = gbpSell!!,
                dt = DateConverter.longToDateWithTime(currentTCS[0].datetime!!)
            )
        )

        val views = RemoteViews(mContext.packageName, R.layout.main_widget)
        views.setTextViewText(
            R.id.usd_buy, String.format(
                Locale.US, "%.2f",
                usdBuy
            ) + "₽"
        )
        views.setTextViewText(
            R.id.usd_sell, String.format(
                Locale.US, "%.2f",
                usdSell
            ) + "₽"
        )
        views.setTextViewText(
            R.id.eur_buy, String.format(
                Locale.US, "%.2f",
                eurBuy
            ) + "₽"
        )
        views.setTextViewText(
            R.id.eur_sell, String.format(
                Locale.US, "%.2f",
                eurSell
            ) + "₽"
        )
        views.setTextViewText(
            R.id.dt,
            "${mContext.resources.getString(R.string.tcsfrom)} " + " "
                    + DateConverter.longToDateWithTime(currentTCS[0].datetime!!)
        )
        mappWidgetManager.updateAppWidget(mappWidgetID, views)
    }

    private fun refreshCBPart(currentCB: List<CurrencyCBjs>) {
        val curUSD = currentCB[0]
        val curEUR = currentCB[1]


        val views = RemoteViews(mContext.packageName, R.layout.main_widget)
        if (curUSD.value <= curUSD.valueWas) views.setTextColor(
            R.id.cbrf_usd,
            Color.GREEN
        ) else views.setTextColor(R.id.cbrf_usd, Color.RED)
        if (curEUR.value <= curEUR.valueWas) views.setTextColor(
            R.id.cbrf_euro,
            Color.GREEN
        ) else views.setTextColor(R.id.cbrf_euro, Color.RED)
        views.setTextViewText(
            R.id.cbrf_usd, String.format(
                Locale.US, "%.2f",
                curUSD.value
            ) + " (" + String.format(
                Locale.US, "%+.2f",
                -curUSD.valueWas + curUSD.value
            ) + ")" + "₽"
        )
        views.setTextViewText(
            R.id.cbrf_euro, String.format(
                Locale.US, "%.2f",
                curEUR.value
            ) + " (" + String.format(
                Locale.US, "%+.2f",
                -curEUR.valueWas + curEUR.value
            ) + ")" + "₽"
        )
        views.setTextViewText(
            R.id.dt2,
            "${mContext.resources.getString(R.string.CBtill)} ${curUSD.dateTime}"
        )
        mappWidgetManager.updateAppWidget(mappWidgetID, views)
    }
}
