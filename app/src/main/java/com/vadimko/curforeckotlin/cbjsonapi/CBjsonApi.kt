package com.vadimko.curforeckotlin.cbjsonapi

import retrofit2.Call
import retrofit2.http.GET

//интерфейс запроса к данным ЦБ в формате JSON
interface CBjsonApi {

    @GET("daily_json.js")
    fun getCBForec(): Call<CBjsonResponse>

}