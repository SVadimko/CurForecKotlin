package com.vadimko.curforeckotlin.tcsApi

import com.vadimko.curforeckotlin.utils.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinComponent
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Request using Retrofit to https://www.tinkoff.ru/api/v1/
 *
 */

class TCSRepository : KoinComponent {
    /* private val scopeCreator: ScopeCreator by inject()
     private val currenciesRepository: CurrenciesRepository by inject()
     private val context: Context by inject()*/
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
     * Perform request to server
     * @return list of [CurrencyTCS]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getResponse(): List<CurrencyTCS> {
        val currentRequest: Call<TCSResponse> = tcsApi.getTCSForec()
        return withContext(Dispatchers.IO) { Parser.parseTcsResponse(currentRequest.execute()) }
    }
}

