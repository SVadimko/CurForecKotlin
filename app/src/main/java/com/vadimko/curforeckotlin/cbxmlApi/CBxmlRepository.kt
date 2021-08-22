package com.vadimko.curforeckotlin.cbxmlApi


import android.annotation.SuppressLint
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.text.SimpleDateFormat

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

    fun getXMLarchive(date_req1: String, date_req2: String, VAL_NM_RQ: String) {
        val currentRequest: Call<MOEXXMLResponse> = cbxmlApi.getTCSForec(
            date_req1,
            date_req2,
            VAL_NM_RQ
        )
        currentRequest.enqueue(object : Callback<MOEXXMLResponse> {
            /**
             * Get list of [CurrencyCBarhive] and post it to [ArchiveViewModel]
             */
            override fun onResponse(
                call: Call<MOEXXMLResponse>,
                response: Response<MOEXXMLResponse>
            ) {
                val listData: MutableList<CurrencyCBarhive> = mutableListOf()
                val cBXMLResponse = response.body()
                val listRec = cBXMLResponse?.record
                listRec?.forEach { it ->
                    val offCur = it.Value
                    val dateTime = it.Date
                    val dateTimeConv = dateFormat(dateTime)
                    listData.add(CurrencyCBarhive(offCur, dateTime, dateTimeConv))
                }
                ArchiveViewModel.dataCB.postValue(listData)
            }

            override fun onFailure(call: Call<MOEXXMLResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })

    }

    @SuppressLint("SimpleDateFormat")
    fun dateFormat(datesOFF: String): String {
        val dateConvert = SimpleDateFormat("dd.MM.yyyy").parse(datesOFF)
        val jdf = SimpleDateFormat("yyyy-MM-dd")
        return jdf.format(dateConvert!!)
    }
}