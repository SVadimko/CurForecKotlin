package com.vadimko.curforeckotlin.cbxmlApi


import com.vadimko.curforeckotlin.utils.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

/**
 * Request using Retrofit to https://www.cbr.ru/scripts/
 */
class CBxmlRepository {
    private val cbxmlApi: CBxmlApi

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        okHttpClientBuilder.addInterceptor(logging)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/scripts/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
        cbxmlApi = retrofit.create(CBxmlApi::class.java)
    }

    /**
     * Perform request to server
     * @return list of [CurrencyCBarhive]
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getResponse(
        date_req1: String,
        date_req2: String,
        VAL_NM_RQ: String
    ): List<CurrencyCBarhive> {
        val currentRequest: Call<CBXXMLResponse> = cbxmlApi.getCBXmlForec(
            date_req1,
            date_req2,
            VAL_NM_RQ
        )
        return withContext(Dispatchers.IO) { Parser.parseCbXmlResponse(currentRequest.execute()) }
    }
}