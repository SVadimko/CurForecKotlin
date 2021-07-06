package com.vadimko.curforeckotlin.moexapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


//интерфейс запроса к сайту мосбиржи
interface MOEXApi {
    @GET("{req}/candles.json")
    fun getMOEXForec(
        @Path("req") reques: String,
        @Query("from") from: String,
        @Query("till") till: String,
        @Query("interval") interval: String
    ): Call<MOEXResponse>
}
