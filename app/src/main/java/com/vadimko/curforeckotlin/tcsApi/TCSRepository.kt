package com.vadimko.curforeckotlin.tcsApi

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.utils.DateConverter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


/**
 * Request using Retrofit to https://www.tinkoff.ru/api/v1/
 *
 */

class TCSRepository : KoinComponent {
    private val currenciesRepository: CurrenciesRepository by inject()
    private val context: Context by inject()
    private val tcsApi: TCSApi


    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        okHttpClientBuilder.addInterceptor(logging)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.tinkoff.ru/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
        tcsApi = retrofit.create(TCSApi::class.java)
    }

    /**
     * Depends of flag [requestMode] if received from server some 0 value, repeat [getCurrentTCS]
     * else post list of [CurrencyTCS] to [NowViewModel] and [CalcViewModel] or update widget
     * through [AppWidgetManager]
     */
    fun getCurrentTCS(
        requestMode: Boolean,
        appWidgetManager: AppWidgetManager?,
        appWidgetID: Int?
    ) {
        val currentRequest: Call<TCSResponse> = tcsApi.getTCSForec()
        currentRequest.enqueue(object : Callback<TCSResponse> {
            override fun onResponse(call: Call<TCSResponse>, response: Response<TCSResponse>) {
                val tcsResponse: TCSResponse? = response.body()
                val tcsPayload: TCSPayload? = tcsResponse?.payload
                val tcsRates: List<TCSRates>? = tcsPayload?.rates
                val tcsLastUpdate: TCSLastUpdate? = tcsPayload?.lastUpdate
                val flagUSD = R.drawable.usd
                val nameUSD = tcsRates?.get(15)?.fromCurrency?.name
                val buyUSD = tcsRates?.get(15)?.buy
                val sellUSD = tcsRates?.get(15)?.sell
                val dt = tcsLastUpdate?.milliseconds
                val flagEUR = R.drawable.eur
                val nameEUR = tcsRates?.get(18)?.fromCurrency?.name
                val buyEUR = tcsRates?.get(18)?.buy
                val sellEUR = tcsRates?.get(18)?.sell
                val flagGBP = R.drawable.gbp
                val nameGBP = tcsRates?.get(21)?.fromCurrency?.name
                val buyGBP = tcsRates?.get(21)?.buy
                val sellGBP = tcsRates?.get(21)?.sell
                if (!requestMode) {
                    val usdTCS = CurrencyTCS(flagUSD, dt, sellUSD, buyUSD, nameUSD)
                    val eurTCS = CurrencyTCS(flagEUR, dt, sellEUR, buyEUR, nameEUR)
                    val gbpTCS = CurrencyTCS(flagGBP, dt, sellGBP, buyGBP, nameGBP)
                    if (usdTCS.buy == 0.0 || eurTCS.buy == 0.0 || gbpTCS.buy == 0.0) {
                        getCurrentTCS(requestMode, appWidgetManager, appWidgetID)
                    } else {
                        val currentTCS: List<CurrencyTCS> = listOf(usdTCS, eurTCS, gbpTCS)
                        NowViewModel.setDataTCs(currentTCS)
                        NowViewModel.onRefreshRatesActions()
                    }
                } else {
                    if (buyUSD == 0.0 || buyEUR == 0.0 || buyGBP == 0.0) {
                        getCurrentTCS(requestMode, appWidgetManager, appWidgetID)
                    } else {
                        currenciesRepository.insertCurrencies(
                            Currencies(
                                usdBuy = buyUSD!!,
                                usdSell = sellUSD!!,
                                eurBuy = buyEUR!!,
                                eurSell = sellEUR!!,
                                gbpBuy = buyGBP!!,
                                gbpSell = sellGBP!!,
                                dt = DateConverter.longToDateWithTime(dt!!)
                            )
                        )
                        val views = RemoteViews(context.packageName, R.layout.main_widget)
                        views.setTextViewText(
                            R.id.usd_buy, String.format(
                                Locale.US, "%.2f",
                                buyUSD
                            ) + "₽"
                        )
                        views.setTextViewText(
                            R.id.usd_sell, String.format(
                                Locale.US, "%.2f",
                                sellUSD
                            ) + "₽"
                        )
                        views.setTextViewText(
                            R.id.eur_buy, String.format(
                                Locale.US, "%.2f",
                                buyEUR
                            ) + "₽"
                        )
                        views.setTextViewText(
                            R.id.eur_sell, String.format(
                                Locale.US, "%.2f",
                                sellEUR
                            ) + "₽"
                        )
                        views.setTextViewText(
                            R.id.dt,
                            "${context.resources.getString(R.string.tcsfrom)} " + " "
                                    + DateConverter.longToDateWithTime(dt)
                        )
                        if (appWidgetID != null) {
                            appWidgetManager?.updateAppWidget(appWidgetID, views)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TCSResponse>, t: Throwable) {
            }
        })
    }
}