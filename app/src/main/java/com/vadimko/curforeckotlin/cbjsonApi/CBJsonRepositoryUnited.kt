package com.vadimko.curforeckotlin.cbjsonApi

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.ui.now.NowViewModel
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

class CBJsonRepositoryUnited(
    val requestMode: Boolean,
    val appWidgetManager: AppWidgetManager?,
    val appWidgetID: Int?
) : KoinComponent {
    private val cBjsonApi: CBjsonApi
    private val context: Context by inject()

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        okHttpClientBuilder.addInterceptor(logging)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
        cBjsonApi = retrofit.create(CBjsonApi::class.java)
    }


    fun getCurrentCB() {
        val currentRequest: Call<CBjsonResponse> = cBjsonApi.getCBForec()
        currentRequest.enqueue(object : Callback<CBjsonResponse> {
            /**
             * Get list of [CurrencyCBjs] and post it to [NowViewModel.dataCB]
             */
            override fun onResponse(
                call: Call<CBjsonResponse>,
                response: Response<CBjsonResponse>
            ) {
                val cBjsonResponse: CBjsonResponse? = response.body()
                val date: String? = cBjsonResponse?.Date
                val dateSplit = date?.split("T")?.toTypedArray()
                val timeSplit = dateSplit?.get(1)?.split("+")?.toTypedArray()
                val dateWas = timeSplit!![0] + " " + dateSplit[0]
                val valuteResponse = cBjsonResponse.Valute
                val valUSD: CBjsonValute = valuteResponse.USD
                val valEUR: CBjsonValute = valuteResponse.EUR
                val valGBP: CBjsonValute = valuteResponse.GBP
                val valBYN: CBjsonValute = valuteResponse.BYN
                val valTRY: CBjsonValute = valuteResponse.TRY
                val valUAH: CBjsonValute = valuteResponse.UAH
                val valueUSD = valUSD.Value
                val valueEUR = valEUR.Value
                val valueGBP = valGBP.Value
                val valueBYN = valBYN.Value
                val valueTRY = valTRY.Value
                val valueUAH = valUAH.Value

                val previousUSD = valUSD.Previous
                val previousEUR = valEUR.Previous
                val previousGBP = valGBP.Previous
                val previousBYN = valBYN.Previous
                val previousTRY = valTRY.Previous
                val previousUAH = valUAH.Previous

                val flagUSD = R.drawable.usd
                val flagEUR = R.drawable.eur
                val flagGBP = R.drawable.gbp
                val flagBYN = R.drawable.byn
                val flagTRY = R.drawable.ty
                val flagUAH = R.drawable.uah

                val curUSD = CurrencyCBjs(valueUSD, previousUSD, dateWas, flagUSD, "USD")
                val curEUR = CurrencyCBjs(valueEUR, previousEUR, dateWas, flagEUR, "EUR")
                val curGBP = CurrencyCBjs(valueGBP, previousGBP, dateWas, flagGBP, "GBP")
                val curBYN = CurrencyCBjs(valueBYN, previousBYN, dateWas, flagBYN, "BYN")
                val curTRY = CurrencyCBjs(valueTRY / 10, previousTRY / 10, dateWas, flagTRY, "TRY")
                val curUAH = CurrencyCBjs(valueUAH / 10, previousUAH / 10, dateWas, flagUAH, "UAH")

                if (!requestMode) {
                    val cbCurr: List<CurrencyCBjs> =
                        listOf(curUSD, curEUR, curGBP, curBYN, curTRY, curUAH)
                    NowViewModel.dataCB.postValue(cbCurr)
                } else {
                    val views = RemoteViews(context.packageName, R.layout.main_widget)
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
                        "${context.resources.getString(R.string.CBtill)} $dateWas"
                    )
                    if (appWidgetID != null) {
                        appWidgetManager?.updateAppWidget(appWidgetID, views)
                    }
                }
            }

            override fun onFailure(call: Call<CBjsonResponse>, t: Throwable) {
            }
        })
    }
}