package com.vadimko.curforeckotlin.moexApi

import com.vadimko.curforeckotlin.cbxmlApi.CurrencyCBarhive
import com.vadimko.curforeckotlin.utils.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Request using Retrofit to https://iss.moex.com/iss/engines/currency/
 */
class MOEXRepository {

    private val moexApi: MOEXApi

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        okHttpClientBuilder.addInterceptor(logging)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(
                "https://iss.moex.com/iss/engines/currency/" +
                        "markets/selt/boards/CETS/securities/"
            )
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
        moexApi = retrofit.create(MOEXApi::class.java)
    }

    /**
     * Perform request to server
     * @return list of [CurrencyCBarhive]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getResponse(
        request: String,
        from: String,
        till: String,
        interval: String,
        todayArch: Boolean
    ): List<CurrencyMOEX> {
        val currentRequest: Call<MOEXResponse> = if (!todayArch) {
            moexApi.getMOEXForec(request, from, till, interval)
        } else {
            moexApi.getMOEXForec(request, from, till, "24")
        }
        return withContext(Dispatchers.IO) { Parser.parseMoexResponse(currentRequest.execute()) }
    }
}