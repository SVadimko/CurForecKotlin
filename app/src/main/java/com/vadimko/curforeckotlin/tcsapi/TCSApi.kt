package com.vadimko.curforeckotlin.tcsapi

import retrofit2.Call
import retrofit2.http.GET

//интерфейс запроса к сайту Тиньков
interface TCSApi {

    @GET("currency_rates/")
    fun getTCSForec(): Call<TCSResponse>

}