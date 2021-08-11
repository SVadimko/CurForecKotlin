package com.vadimko.curforeckotlin.moexApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 *interface for requesting MOEX data with a response in JSON format
 */

interface MOEXApi {
    @GET("{req}/candles.json")
    fun getMOEXForec(
        @Path("req") reques: String,
        @Query("from") from: String,
        @Query("till") till: String,
        @Query("interval") interval: String
    ): Call<MOEXResponse>
}
