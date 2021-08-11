package com.vadimko.curforeckotlin.tcsApi

import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * request using Retrofit to https://www.tinkoff.ru/api/v1/
 */

class TCSRepository {
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

    fun getCurrentTCS()  {
        val currentRequest: Call<TCSResponse> = tcsApi.getTCSForec()
        currentRequest.enqueue(object : Callback<TCSResponse> {
            override fun onResponse(call: Call<TCSResponse>, response: Response<TCSResponse>) {
                val tcsResponse: TCSResponse? = response.body()
                val tcsPayload: TCSPayload? = tcsResponse?.payload
                val tcsRates: List<TCSRates>? = tcsPayload?.rates
                val tcsLastUpdate: TCSLastUpdate? = tcsPayload?.lastUpdate
                val flagUSD =R.drawable.usd
                val nameUSD = tcsRates?.get(15)?.fromCurrency?.name
                val buyUSD = tcsRates?.get(15)?.buy
                val sellUSD = tcsRates?.get(15)?.sell
                val dt = tcsLastUpdate?.milliseconds
                val flagEUR =R.drawable.eur
                val nameEUR = tcsRates?.get(18)?.fromCurrency?.name
                val buyEUR = tcsRates?.get(18)?.buy
                val sellEUR = tcsRates?.get(18)?.sell
                val flagGBP = R.drawable.gbp
                val nameGBP = tcsRates?.get(21)?.fromCurrency?.name
                val buyGBP = tcsRates?.get(21)?.buy
                val sellGBP = tcsRates?.get(21)?.sell
                val usdTCS = CurrencyTCS(flagUSD,dt,sellUSD,buyUSD, nameUSD)
                val eurTCS = CurrencyTCS(flagEUR,dt,sellEUR,buyEUR, nameEUR)
                val gbpTCS = CurrencyTCS(flagGBP,dt,sellGBP,buyGBP, nameGBP)
                if(usdTCS.buy==0.0||eurTCS.buy==0.0||gbpTCS.buy==0.0) {
                    getCurrentTCS()
                }
                else{
                    val currentTCS: List<CurrencyTCS> = listOf(usdTCS,eurTCS,gbpTCS)
                    NowViewModel.data.postValue(currentTCS)
                    CalcViewModel.data.postValue(currentTCS)
                }
            }

            override fun onFailure(call: Call<TCSResponse>, t: Throwable) {
            }
        })
    }
}