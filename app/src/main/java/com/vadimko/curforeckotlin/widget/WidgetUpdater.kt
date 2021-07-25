package com.vadimko.curforeckotlin.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbjsonapi.CBjsonApi
import com.vadimko.curforeckotlin.cbjsonapi.CBjsonResponse
import com.vadimko.curforeckotlin.cbjsonapi.CBjsonValute
import com.vadimko.curforeckotlin.database.Currencies
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.tcsapi.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class WidgetUpdater(context: Context, appWidgetManager: AppWidgetManager, appWidgetID: Int) {
    private val tcsApi: TCSApi
    private val cBjsonApi: CBjsonApi
    private val mContext = context
    private val mappWidgetManager = appWidgetManager
    private val mappWidgetID = appWidgetID


    init {
        val retrofitTCs: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.tinkoff.ru/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        tcsApi = retrofitTCs.create(TCSApi::class.java)

        val retrofitCB: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        cBjsonApi = retrofitCB.create(CBjsonApi::class.java)
        updateTCs()
        updateCB()
    }

    private fun updateTCs() {
        val currentRequest: Call<TCSResponse> = tcsApi.getTCSForec()
        currentRequest.enqueue(object : Callback<TCSResponse> {
            override fun onResponse(call: Call<TCSResponse>, response: Response<TCSResponse>) {
                val tcsResponse: TCSResponse? = response.body()
                val tcsPayload: TCSPayload? = tcsResponse?.payload
                val tcsRates: List<TCSRates>? = tcsPayload?.rates
                val tcsLastUpdate: TCSLastUpdate? = tcsPayload?.lastUpdate
                val buyUSD = tcsRates?.get(15)?.buy
                val sellUSD = tcsRates?.get(15)?.sell
                val dt = tcsLastUpdate?.milliseconds
                val buyEUR = tcsRates?.get(18)?.buy
                val sellEUR = tcsRates?.get(18)?.sell
                val buyGBP = tcsRates?.get(21)?.buy
                val sellGBP = tcsRates?.get(21)?.sell
                if (buyUSD == 0.0 || buyEUR == 0.0 || buyGBP == 0.0) {
                    updateTCs()
                } else {
                    CurrenciesRepository.initialize(mContext)
                    val currenciesRepository = CurrenciesRepository.get()
                    currenciesRepository.insertCurrencies(
                        Currencies(
                            usdBuy = buyUSD!!,
                            usdSell = sellUSD!!,
                            eurBuy = buyEUR!!,
                            eurSell = sellEUR!!,
                            gbpBuy = buyGBP!!,
                            gbpSell = sellGBP!!,
                            dt = longToTime(dt!!)
                        )
                    )
                    val views = RemoteViews(mContext.packageName, R.layout.main_widget)
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
                        //"ТКС  от " + " " + longToTime(
                        "${mContext.resources.getString(R.string.tcsfrom)} " + " " + longToTime(
                            dt
                        )
                    )
                    mappWidgetManager.updateAppWidget(mappWidgetID, views)
                }
            }

            override fun onFailure(call: Call<TCSResponse>, t: Throwable) {
            }


        })
    }

    private fun updateCB() {
        val currentRequest: Call<CBjsonResponse> = cBjsonApi.getCBForec()
        currentRequest.enqueue(object : Callback<CBjsonResponse> {
            override fun onResponse(
                call: Call<CBjsonResponse>,
                response: Response<CBjsonResponse>
            ) {
                val cBJsonResponse: CBjsonResponse? = response.body()
                val date: String? = cBJsonResponse?.Date
                val dateSplit = date?.split("T")?.toTypedArray()
                val timeSplit = dateSplit?.get(1)?.split("+")?.toTypedArray()
                val dateWas = timeSplit!![0] + " " + dateSplit[0]
                val valuteResponse = cBJsonResponse.Valute
                val uSD: CBjsonValute = valuteResponse.USD
                val eUR: CBjsonValute = valuteResponse.EUR
                val valueUSD = uSD.Value
                val valueEUR = eUR.Value

                val previousUSD = uSD.Previous
                val previousEUR = eUR.Previous

                val views = RemoteViews(mContext.packageName, R.layout.main_widget)
                if (valueUSD <= previousUSD) views.setTextColor(
                    R.id.cbrf_usd,
                    Color.GREEN
                ) else views.setTextColor(R.id.cbrf_usd, Color.RED)
                if (valueEUR <= previousEUR) views.setTextColor(
                    R.id.cbrf_euro,
                    Color.GREEN
                ) else views.setTextColor(R.id.cbrf_euro, Color.RED)
                views.setTextViewText(
                    R.id.cbrf_usd, String.format(
                        Locale.US, "%.2f",
                        valueUSD
                    ) + " (" + String.format(
                        Locale.US, "%+.2f",
                        -previousUSD + valueUSD
                    ) + ")" + "₽"
                )
                views.setTextViewText(
                    R.id.cbrf_euro, String.format(
                        Locale.US, "%.2f",
                        valueEUR
                    ) + " (" + String.format(
                        Locale.US, "%+.2f",
                        -previousEUR + valueEUR
                    ) + ")" + "₽"
                )
                views.setTextViewText(
                    R.id.dt2,
                    "${mContext.resources.getString(R.string.CBtill)} $dateWas"
                )
                mappWidgetManager.updateAppWidget(mappWidgetID, views)
            }

            override fun onFailure(call: Call<CBjsonResponse>, t: Throwable) {
            }
        })
    }

    private fun longToTime(time: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("HH:mm:ss dd.MM.yyyy", mContext.resources.configuration.locales[0]).format(
                Date(
                    time
                )
            )
        } else {
            return SimpleDateFormat("HH:mm:ss dd.MM.yyyy", mContext.resources.configuration.locale).format(
                Date(time)
            )
        }
    }
}
