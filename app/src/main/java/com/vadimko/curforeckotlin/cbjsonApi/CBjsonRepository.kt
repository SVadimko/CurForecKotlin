package com.vadimko.curforeckotlin.cbjsonApi

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
 * Request using Retrofit to https://www.cbr-xml-daily.ru/
 */
class CBjsonRepository : KoinComponent {
    private val cBjsonApi: CBjsonApi


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
     * Perform request to server
     * @return list of [CurrencyCBjs]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getResponse(): List<CurrencyCBjs> {
        val currentRequest: Call<CBjsonResponse> = cBjsonApi.getCBForec()
        return withContext(Dispatchers.IO) { Parser.parseCBResponse(currentRequest.execute()) }
    }
}