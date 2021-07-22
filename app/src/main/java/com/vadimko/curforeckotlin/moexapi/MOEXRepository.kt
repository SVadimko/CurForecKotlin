package com.vadimko.curforeckotlin.moexapi

import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel
import com.vadimko.curforeckotlin.ui.today.TodayViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MOEXRepository {

    private val moexApi: MOEXApi

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        okHttpClientBuilder.addInterceptor(logging)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://iss.moex.com/iss/engines/currency/" +
                    "markets/selt/boards/CETS/securities/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
        moexApi = retrofit.create(MOEXApi::class.java)
    }

    fun getMOEX(request: String, from: String, till: String, interval: String, todayArch: Boolean) {
        val currentRequest: Call<MOEXResponse> = if (!todayArch) {
            moexApi.getMOEXForec(request, from, till, interval)
        } else {
            moexApi.getMOEXForec(request, from, till, "24")
        }
        currentRequest.enqueue(object : Callback<MOEXResponse> {
            override fun onResponse(call: Call<MOEXResponse>, response: Response<MOEXResponse>) {
                val moexResponse: MOEXResponse? = response.body()
                val moexCandles: MOEXCandles? = moexResponse?.candles
                //val moexcolumns = moexCandles?.columns
                val moexdata = moexCandles?.data
                val moexcurrency: MutableList<CurrencyMOEX> = mutableListOf()
                moexcurrency.clear()
                moexdata?.forEach { it ->
                    val currencyMoex = CurrencyMOEX(
                        it[6] as String,
                        it[0] as Double,
                        it[3] as Double,
                        it[2] as Double,
                        it[1] as Double,
                        (it[3] as Double + it[2] as Double) / 2
                    )
                    moexcurrency.add(currencyMoex)
                }
                if (!todayArch)
                    TodayViewModel.data.postValue(moexcurrency)
                else
                    ArchiveViewModel.dataMOEX.postValue(moexcurrency)
            }


            override fun onFailure(call: Call<MOEXResponse>, t: Throwable) {
            }
        })
    }
}