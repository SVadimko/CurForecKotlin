package com.vadimko.curforeckotlin.tcsapi

import retrofit2.Call
import retrofit2.http.GET

/**
 *interface for requesting Tinkov data with a response in JSON format
 */

interface TCSApi {

    @GET("currency_rates/")
    fun getTCSForec(): Call<TCSResponse>

}