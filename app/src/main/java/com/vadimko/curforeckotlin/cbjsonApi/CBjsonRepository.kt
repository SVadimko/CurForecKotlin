package com.vadimko.curforeckotlin.cbjsonApi

import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.utils.Parser
import com.vadimko.curforeckotlin.widget.AppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinComponent
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

/**
 * Request using Retrofit to https://www.cbr-xml-daily.ru/
 */
class CBjsonRepository : KoinComponent {
    private val cBjsonApi: CBjsonApi
    /* private val context: Context by inject()
     private val scopeCreator: ScopeCreator by inject()*/

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


    /**
     * Depends of flag [requestMode] post list of [CurrencyCBjs]
     * to [NowViewModel] or update widget [AppWidget]
     */
    /* fun getCurrentCB(
         requestMode: Boolean,
         appWidgetManager: AppWidgetManager?,
         appWidgetID: Int?
     ) {
         if (!requestMode) {
             scopeCreator.getScope().launch {
                 val response = getResponse()
                 parseResponse(response, requestMode, appWidgetManager, appWidgetID)
             }
         } else GlobalScope.launch {
             val response = getResponse()
             parseResponse(response, requestMode, appWidgetManager, appWidgetID)
         }
     }*/

    /**
     * Perform request to server
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getResponse(): List<CurrencyCBjs> {
        val currentRequest: Call<CBjsonResponse> = cBjsonApi.getCBForec()
        return withContext(Dispatchers.IO) { Parser.parseCBResponse(currentRequest.execute()) }
    }

    /**
     * Parse response and according [requestMode] update data for NowViewModel or widget
     */
    /* private suspend fun parseResponse(
         response: Response<CBjsonResponse>, requestMode: Boolean,
         appWidgetManager: AppWidgetManager?,
         appWidgetID: Int?
     ) {
         val cBjsonResponse: CBjsonResponse? = response.body()
         val date: String? = cBjsonResponse?.date
         val dateSplit = date?.split("T")?.toTypedArray()
         val timeSplit = dateSplit?.get(1)?.split("+")?.toTypedArray()
         val dateWas = timeSplit!![0] + " " + dateSplit[0]
         val valuteResponse = cBjsonResponse.valute
         val valUSD: CBjsonValute = valuteResponse.usd
         val valEUR: CBjsonValute = valuteResponse.eur
         val valGBP: CBjsonValute = valuteResponse.gbp
         val valBYN: CBjsonValute = valuteResponse.byn
         val valTRY: CBjsonValute = valuteResponse.`try`
         val valUAH: CBjsonValute = valuteResponse.uah
         val valueUSD = valUSD.value
         val valueEUR = valEUR.value
         val valueGBP = valGBP.value
         val valueBYN = valBYN.value
         val valueTRY = valTRY.value
         val valueUAH = valUAH.value

         val previousUSD = valUSD.previous
         val previousEUR = valEUR.previous
         val previousGBP = valGBP.previous
         val previousBYN = valBYN.previous
         val previousTRY = valTRY.previous
         val previousUAH = valUAH.previous

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
             NowViewModel.setDataCB(cbCurr)
         } else {
             withContext(Dispatchers.Main) {
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
     }*/
}